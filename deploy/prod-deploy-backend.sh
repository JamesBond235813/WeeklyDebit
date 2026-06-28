#!/usr/bin/env bash
# =============================================================
# WeeklyDebit 生产后端部署（类替换法，配置零改动）
# 在 Mac 上运行：bash "/Volumes/littlejiang02/VibeCoding/WeeklyDebit/deploy/prod-deploy-backend.sh"
#
# 做什么：
#   1) 确保本地 jar 是最新（源码有变则用 JDK17 重新打包）
#   2) 抽出本地 jar 的 BOOT-INF/classes，打包上传云端
#   3) 云端：备份现网 jar → 只把“与现网不同的 .class”替换进现网 jar 的副本
#      （bootstrap*.yml 等所有配置一字节不动，自动带上之前 BizConfigServiceImpl 的修复）
#   4) 云端：从现网 jar 读出 prod-mini 的库账号，跑幂等迁移（加 user_source 列）
#   5) 停服务 → 换 jar → 起服务 → 健康检查；任一步失败自动回滚到备份
# 安全：绝不重打含本地库配置的整包覆盖；只动 .class 与新增 DB 列（向后兼容）。
# =============================================================
set -euo pipefail

ROOT="/Volumes/littlejiang02/VibeCoding/WeeklyDebit"
BE="$ROOT/xiaohebao_06/silver-union-all"
OPS="$BE/silver-union-ops"
LOCAL_JAR="$OPS/target/silver-union-ops.jar"
SSH_TARGET="${WD_PROD_SSH_TARGET:-root@66.94.122.149}"
SSH_KEY="${WD_PROD_SSH_KEY:-$HOME/.ssh/vpn_deploy}"
SSH_OPTS=(-i "$SSH_KEY")
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

echo "==> [1/5] 确保本地 jar 最新"
NEWER=""
[ -f "$LOCAL_JAR" ] && NEWER=$(find "$OPS/src" -name '*.java' -newer "$LOCAL_JAR" 2>/dev/null | head -1 || true)
if [ ! -f "$LOCAL_JAR" ] || [ -n "$NEWER" ]; then
  JH=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
  [ -n "$JH" ] || { echo "   !! 未找到 JDK17"; exit 1; }
  export JAVA_HOME="$JH"
  ( cd "$BE" && ./mvnw -pl silver-union-ops -am -q -DskipTests clean package )
fi
[ -f "$LOCAL_JAR" ] || { echo "   !! 本地 jar 不存在"; exit 1; }
echo "   本地 jar: $LOCAL_JAR"

echo "==> [2/5] 抽取本地 classes 并上传云端"
mkdir -p "$TMP/x"
unzip -o -q "$LOCAL_JAR" 'BOOT-INF/classes/*' -d "$TMP/x"
( cd "$TMP/x" && tar -czf "$TMP/local-classes.tgz" BOOT-INF/classes )
scp "${SSH_OPTS[@]}" "$TMP/local-classes.tgz" "$SSH_TARGET:/tmp/wd-local-classes.tgz"
echo "   已上传 /tmp/wd-local-classes.tgz"

echo "==> [3-5] 云端：类替换 + 迁移 + 切换重启（含自动回滚）"
ssh "${SSH_OPTS[@]}" "$SSH_TARGET" 'bash -s' <<'REMOTE'
set -euo pipefail
JAR=/data/servers/weeklydebit/current/silver-union-ops.jar
SERVICE=weeklydebit-backend
HEALTH="http://127.0.0.1:32802/api/public/third/access-check"
WORK=/tmp/wd-prod-deploy
TS=$(date +%Y%m%d-%H%M%S)
BACKUP="${JAR}.bak-${TS}"

command -v unzip >/dev/null || { echo "云端缺 unzip"; exit 1; }
if command -v zip >/dev/null; then UPD=zip; elif command -v jar >/dev/null; then UPD=jar; else echo "云端缺 zip/jar"; exit 1; fi
test -f "$JAR" || { echo "云端 jar 不存在: $JAR"; exit 1; }
test -f /tmp/wd-local-classes.tgz || { echo "未收到 classes 包"; exit 1; }

rm -rf "$WORK"; mkdir -p "$WORK/new"; cd "$WORK/new"
tar -xzf /tmp/wd-local-classes.tgz
echo "   [云] 计算与现网不同的 .class"
CHANGED=()
while IFS= read -r f; do
  rel="${f#./}"
  nm=$(md5sum "$f" | awk '{print $1}')
  om=$(unzip -p "$JAR" "$rel" 2>/dev/null | md5sum | awk '{print $1}')
  [ "$nm" != "$om" ] && CHANGED+=("$rel")
done < <(find BOOT-INF/classes -name '*.class')
echo "   [云] 变动类数量: ${#CHANGED[@]}"
if [ "${#CHANGED[@]}" -eq 0 ]; then echo "   [云] 无类变动，仅做迁移与重启"; fi
printf '       %s\n' "${CHANGED[@]:-（无）}"

cp "$JAR" "$WORK/new.jar"
if [ "${#CHANGED[@]}" -gt 0 ]; then
  if [ "$UPD" = zip ]; then ( cd "$WORK/new" && zip -q "$WORK/new.jar" "${CHANGED[@]}" );
  else ( cd "$WORK/new" && jar uf "$WORK/new.jar" "${CHANGED[@]}" ); fi
fi

echo "   [云] 校验：配置文件保持不变"
for y in bootstrap.yml bootstrap-prod-mini.yml; do
  a=$(unzip -p "$JAR" "BOOT-INF/classes/$y" 2>/dev/null | md5sum | awk '{print $1}')
  b=$(unzip -p "$WORK/new.jar" "BOOT-INF/classes/$y" 2>/dev/null | md5sum | awk '{print $1}')
  [ "$a" = "$b" ] || { echo "   !! 配置 $y 被改动，放弃 ($a != $b)"; exit 1; }
done
echo "   [云] 配置一致 ✓"

echo "   [云] 生产库迁移（幂等加 user_source）"
PM=$(unzip -p "$JAR" BOOT-INF/classes/bootstrap-prod-mini.yml)
gv(){ printf '%s\n' "$PM" | grep -E "^[[:space:]]*$1:" | head -1 | sed -E "s/^[[:space:]]*$1:[[:space:]]*//; s/[[:space:]]*$//"; }
DBN=$(gv db-name); DBU=$(gv username); DBP=$(gv password); DBHP=$(gv db-host)
DBH="${DBHP%%:*}"; DBPT="${DBHP##*:}"; [ "$DBPT" = "$DBH" ] && DBPT=3306
echo "       prod db=$DBN host=$DBH:$DBPT user=$DBU"
if command -v mysql >/dev/null; then
  CNF=$(mktemp); printf '[client]\nhost=%s\nport=%s\nuser=%s\npassword="%s"\n' "$DBH" "$DBPT" "$DBU" "$DBP" > "$CNF"
  mysql --defaults-extra-file="$CNF" "$DBN" <<'SQL'
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='customer_info_item' AND COLUMN_NAME='user_source');
SET @d := IF(@c=0,'ALTER TABLE `customer_info_item` ADD COLUMN `user_source` varchar(64) NOT NULL DEFAULT '''' COMMENT ''用户来源（上游渠道推送的 channel_id）'' AFTER `channel`','SELECT ''user_source exists, skip''');
PREPARE s FROM @d; EXECUTE s; DEALLOCATE PREPARE s;
SQL
  rm -f "$CNF"
  echo "       迁移完成"
else
  echo "   !! 云端无 mysql 客户端，迁移未执行。请手动在生产库执行加列后再重启，或装 mysql-client 重跑。"
  echo "      ALTER TABLE customer_info_item ADD COLUMN user_source varchar(64) NOT NULL DEFAULT '' AFTER channel;"
  exit 1
fi

rollback(){ echo "   [云] !!! 异常，回滚 !!!"; systemctl stop "$SERVICE" || true; cp "$BACKUP" "$JAR"; systemctl start "$SERVICE" || true; echo "   [云] 已回滚: $BACKUP"; }

echo "   [云] 备份现网 jar → $BACKUP"
cp "$JAR" "$BACKUP"
echo "   [云] 停服务 → 换 jar → 起服务"
systemctl stop "$SERVICE"
cp "$WORK/new.jar" "$JAR"
systemctl start "$SERVICE"

echo "   [云] 健康检查（最多 90s）"
ok=0
for i in $(seq 1 30); do
  sleep 3
  body=$(curl -s --max-time 5 -X POST "$HEALTH" -H 'Content-Type: application/json' -d '{}' || true)
  if echo "$body" | grep -q 'appId'; then echo "   [云] 健康检查通过: $body"; ok=1; break; fi
done
if [ "$ok" != 1 ]; then echo "   [云] 健康检查失败，最后响应: ${body:-空}"; rollback; exit 1; fi

echo "   [云] 完成。回滚备份: $BACKUP"
systemctl --no-pager status "$SERVICE" | head -4
REMOTE
echo "==> 后端生产部署完成 ✅"

#!/usr/bin/env bash
# =============================================================
# WeeklyDebit 本地一键起服务（迁移 + 打包 + 重启后端 + 健康检查）
# 用法（在 Mac 上）：
#   bash "/Volumes/littlejiang02/VibeCoding/WeeklyDebit/deploy/local-bringup.sh"
# 说明：
#   * 自动从 bootstrap-local.yml 读取本地 MySQL 账号，跑加列迁移
#   * 自动用 /usr/libexec/java_home -v 17 定位 JDK17，调用 ./mvnw 打包
#   * 杀掉占用 32800 的旧后端，用 local profile 重新起后端
#   * 健康检查 access-check，失败时打印后端日志末尾
#   * 前端 dev(5666) 若已在跑，本脚本不动它；起好后端后刷新页面即可
# =============================================================
set -euo pipefail

ROOT="/Volumes/littlejiang02/VibeCoding/WeeklyDebit"
BE="$ROOT/xiaohebao_06/silver-union-all"
OPS="$BE/silver-union-ops"
BOOT_LOCAL="$OPS/src/main/resources/bootstrap-local.yml"
MIGRATION="$ROOT/database/migrations/2026-06-09_add_user_source.sql"
PORT=32800
FE_PORT=5666
RUN_LOG="$OPS/logs/local-run.log"

echo "==> [1/5] 读取本地 MySQL 配置"
val() { grep -E "^[[:space:]]*$1:" "$BOOT_LOCAL" | head -1 | sed -E "s/^[[:space:]]*$1:[[:space:]]*//; s/[[:space:]]*$//"; }
DBNAME=$(val db-name); DBUSER=$(val username); DBPASS=$(val password); DBHOSTPORT=$(val db-host)
DBHOST="${DBHOSTPORT%%:*}"; DBPORT="${DBHOSTPORT##*:}"; [ "$DBPORT" = "$DBHOST" ] && DBPORT=3306
echo "    db=$DBNAME host=$DBHOST:$DBPORT user=$DBUSER"

echo "==> [2/5] 执行本地迁移（幂等加列 user_source）"
if ! command -v mysql >/dev/null 2>&1; then
  echo "    !! 找不到 mysql 客户端。请先装 mysql-client，或手动执行：$MIGRATION" >&2; exit 1
fi
CNF=$(mktemp); trap 'rm -f "$CNF"' EXIT
printf '[client]\nhost=%s\nport=%s\nuser=%s\npassword="%s"\n' "$DBHOST" "$DBPORT" "$DBUSER" "$DBPASS" > "$CNF"
mysql --defaults-extra-file="$CNF" "$DBNAME" < "$MIGRATION"
echo "    迁移完成；当前 user_source 列："
mysql --defaults-extra-file="$CNF" "$DBNAME" -N -e \
  "SELECT COLUMN_NAME,COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$DBNAME' AND TABLE_NAME='customer_info_item' AND COLUMN_NAME='user_source';"

echo "==> [3/5] 定位 JDK17 并打包"
JH=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
[ -n "$JH" ] || { echo "    !! 未找到 JDK17（/usr/libexec/java_home -v 17）。请确认已装 JDK17。" >&2; exit 1; }
export JAVA_HOME="$JH"; echo "    JAVA_HOME=$JAVA_HOME"
cd "$BE"
JAR="$OPS/target/silver-union-ops.jar"
NEWER_SRC=""
[ -f "$JAR" ] && NEWER_SRC=$(find "$OPS/src" -name '*.java' -newer "$JAR" 2>/dev/null | head -1 || true)
if [ -f "$JAR" ] && [ -z "$NEWER_SRC" ]; then
  echo "    jar 已是最新，跳过打包：$JAR"
else
  ./mvnw -pl silver-union-ops -am -q -DskipTests clean package
fi
[ -f "$JAR" ] || { echo "    !! 打包失败，未生成 jar" >&2; exit 1; }
echo "    使用 jar：$JAR"

echo "==> [4/5] 杀掉端口 ${PORT} 上的旧后端，并以 local profile 启动"
OLDPID=$(lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN -t 2>/dev/null || true)
[ -n "$OLDPID" ] && { echo "    kill 旧进程 $OLDPID"; kill "$OLDPID" 2>/dev/null || true; sleep 3; kill -9 "$OLDPID" 2>/dev/null || true; }
mkdir -p "$OPS/logs"
cd "$OPS"
nohup "$JAVA_HOME/bin/java" -jar "$JAR" --spring.profiles.active=local > "$RUN_LOG" 2>&1 &
echo "    已后台启动，日志：$RUN_LOG"

echo "==> [5/5] 等待启动并健康检查（最多 90s）"
ok=0
for i in $(seq 1 30); do
  sleep 3
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 4 -X POST \
    "http://127.0.0.1:${PORT}/api/public/third/access-check" \
    -H 'Content-Type: application/json' -d '{}' || true)
  if [ "$code" = "200" ] || [ "$code" = "400" ]; then echo "    健康检查通过 (HTTP $code)"; ok=1; break; fi
  echo "    第 ${i} 次：HTTP ${code:-无响应} ..."
done

if [ "$ok" != 1 ]; then
  echo ""
  echo "❌ 后端未通过健康检查，日志末尾如下（多半是编译或连库报错）："
  echo "------------------------------------------------------------"
  grep -nE 'ERROR|Exception|Caused by|APPLICATION FAILED|Unknown column' "$RUN_LOG" | tail -30 || true
  echo "------------------------------------------------------------"
  echo "完整日志：$RUN_LOG"
  exit 1
fi

echo "==> [6/6] 确保前端 dev 运行（端口 ${FE_PORT}）"
FE_DIR="$ROOT/xiaohebao_06/recycle-ops"
FE_LOG="$FE_DIR/.local-dev.log"
if lsof -nP -iTCP:"${FE_PORT}" -sTCP:LISTEN -t >/dev/null 2>&1; then
  echo "    前端已在运行"
elif ! command -v pnpm >/dev/null 2>&1; then
  echo "    !! 未找到 pnpm。请手动起前端： cd \"$FE_DIR\" && pnpm dev:antd"
else
  ( cd "$FE_DIR" && nohup pnpm dev:antd > "$FE_LOG" 2>&1 & )
  echo "    已后台启动前端，日志: ${FE_LOG} (首次启动 vite 预构建要十几秒)"
  feok=0
  for i in $(seq 1 45); do
    sleep 2
    if lsof -nP -iTCP:"${FE_PORT}" -sTCP:LISTEN -t >/dev/null 2>&1; then echo "    前端就绪 (${FE_PORT})"; feok=1; break; fi
  done
  [ "$feok" = 1 ] || echo "    !! 前端 45 次仍未监听 ${FE_PORT}，看日志：$FE_LOG"
fi

echo ""
echo "✅ 完成。打开 http://localhost:${FE_PORT} 登录。"
echo "   登录后进【客户列表】应看到：身份证号码 | 用户来源 | 上游渠道"

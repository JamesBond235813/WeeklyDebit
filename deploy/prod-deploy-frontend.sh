#!/usr/bin/env bash
# =============================================================
# WeeklyDebit 生产前端部署
# 在 Mac 上运行：bash "/Volumes/littlejiang02/VibeCoding/WeeklyDebit/deploy/prod-deploy-frontend.sh"
#
# 做什么：
#   1) 本地用 production 模式构建 web-antd（VITE_GLOB_API_URL=/api）
#   2) 打包 dist 上传云端
#   3) 云端：备份现网 dist → 原子替换为新 dist（nginx 静态目录，无需 reload）
#   失败不动现网（切换前都在临时目录操作）
# =============================================================
set -euo pipefail

ROOT="/Volumes/littlejiang02/VibeCoding/WeeklyDebit"
FE="$ROOT/xiaohebao_06/recycle-ops"
DIST="$FE/apps/web-antd/dist"
SSH_TARGET="${WD_PROD_SSH_TARGET:-root@66.94.122.149}"
SSH_KEY="${WD_PROD_SSH_KEY:-$HOME/.ssh/vpn_deploy}"
SSH_OPTS=(-i "$SSH_KEY")
WEB="/data/www/fuwu.juxin.pro/dist"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

echo "==> [1/3] 本地生产构建（pnpm build:antd）"
command -v pnpm >/dev/null || { echo "   !! 未找到 pnpm"; exit 1; }
( cd "$FE" && pnpm build:antd )
[ -f "$DIST/index.html" ] || { echo "   !! 构建产物缺失: $DIST/index.html"; exit 1; }
echo "   构建完成：$DIST"

echo "==> [2/3] 打包并上传 dist"
tar -czf "$TMP/dist.tgz" -C "$FE/apps/web-antd" dist
scp "${SSH_OPTS[@]}" "$TMP/dist.tgz" "$SSH_TARGET:/tmp/wd-dist.tgz"

echo "==> [3/3] 云端：备份现网 dist 并原子替换"
ssh "${SSH_OPTS[@]}" "$SSH_TARGET" "WEB='$WEB' bash -s" <<'REMOTE'
set -euo pipefail
TS=$(date +%Y%m%d-%H%M%S)
STAGE=/tmp/wd-fe-$TS
rm -rf "$STAGE"; mkdir -p "$STAGE"
tar -xzf /tmp/wd-dist.tgz -C "$STAGE"
test -f "$STAGE/dist/index.html" || { echo "   解包后缺 index.html"; exit 1; }
mkdir -p "$(dirname "$WEB")"
if [ -d "$WEB" ]; then
  mv "$WEB" "${WEB}.bak-${TS}"
  echo "   现网 dist 备份为 ${WEB}.bak-${TS}"
fi
mv "$STAGE/dist" "$WEB"
echo "   新 dist 已就位：$WEB"
ls -l --time-style=full-iso "$WEB/index.html"
REMOTE
echo "==> 前端生产部署完成 ✅（直接刷新 https://fuwu.juxin.pro 验证）"

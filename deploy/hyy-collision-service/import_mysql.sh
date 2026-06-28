#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="${1:-/data/hyy-collision/import}"
RAW_DIR="${BASE_DIR}/raw"
PROCESSED_DIR="${BASE_DIR}/processed"
BATCH_NO="${2:-$(date +%Y%m%d%H%M%S)}"

cd "$(dirname "$0")"
if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

mkdir -p "$RAW_DIR" "$PROCESSED_DIR"
python3 ./prepare_import_tsv.py "$RAW_DIR" "$PROCESSED_DIR" "$BATCH_NO"

docker exec -i hyy-collision-mysql mysql --local-infile=1 -uroot -p"${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD required}" hyy_collision <<SQL
SET SESSION sql_log_bin=0;
LOAD DATA LOCAL INFILE '/import/processed/mobile.tsv'
IGNORE INTO TABLE hyy_collision_mobile
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(phone_prefix8, mobile_md5, batch_no);

LOAD DATA LOCAL INFILE '/import/processed/identity.tsv'
IGNORE INTO TABLE hyy_collision_identity
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(phone_prefix8, name_md5, idno_md5, batch_no);

SELECT 'mobile_rows' AS metric, COUNT(*) AS value FROM hyy_collision_mobile
UNION ALL
SELECT 'identity_rows' AS metric, COUNT(*) AS value FROM hyy_collision_identity;
SQL

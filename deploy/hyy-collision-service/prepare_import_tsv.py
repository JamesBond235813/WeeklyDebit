#!/usr/bin/env python3
import csv
import glob
import hashlib
import os
import re
import sys
from pathlib import Path


PHONE_RE = re.compile(r"^1\d{10}$")
IDNO_RE = re.compile(r"^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$")


def md5_hex(value: str) -> str:
    return hashlib.md5(value.encode("utf-8")).hexdigest()


def clean(value: str) -> str:
    return (value or "").strip().strip("'").strip()


def main():
    if len(sys.argv) < 4:
        print("usage: prepare_import_tsv.py <raw_dir> <output_dir> <batch_no>", file=sys.stderr)
        return 2
    raw_dir = Path(sys.argv[1])
    output_dir = Path(sys.argv[2])
    batch_no = sys.argv[3]
    output_dir.mkdir(parents=True, exist_ok=True)

    files = sorted(glob.glob(str(raw_dir / "*.csv")))
    if not files:
        raise SystemExit(f"no csv files found in {raw_dir}")

    mobile_path = output_dir / "mobile.tsv"
    identity_path = output_dir / "identity.tsv"
    total = valid_phone = with_name = with_idno = 0

    with mobile_path.open("w", encoding="ascii", newline="") as mobile_out, \
            identity_path.open("w", encoding="ascii", newline="") as identity_out:
        for path in files:
            with open(path, newline="", encoding="utf-8", errors="replace") as f:
                reader = csv.reader(f, quotechar="'", skipinitialspace=True)
                for row in reader:
                    total += 1
                    if not row:
                        continue
                    mobile = clean(row[0])
                    if not PHONE_RE.match(mobile):
                        continue
                    valid_phone += 1
                    prefix = mobile[:8]
                    mobile_md5 = md5_hex(mobile)
                    mobile_out.write(f"{prefix}\t{mobile_md5}\t{batch_no}\n")

                    name = clean(row[1]) if len(row) > 1 else ""
                    idno = ""
                    for value in row[2:]:
                        candidate = clean(value)
                        if IDNO_RE.match(candidate):
                            idno = candidate
                            break
                    if name or idno:
                        name_md5 = md5_hex(name) if name else ""
                        idno_md5 = md5_hex(idno.upper()) if idno else ""
                        identity_out.write(f"{prefix}\t{name_md5}\t{idno_md5}\t{batch_no}\n")
                        if name:
                            with_name += 1
                        if idno:
                            with_idno += 1

    print({
        "total": total,
        "valid_phone": valid_phone,
        "with_name": with_name,
        "with_idno": with_idno,
        "mobile_tsv": str(mobile_path),
        "identity_tsv": str(identity_path),
    })
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

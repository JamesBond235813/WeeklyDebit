import os
import queue
from contextlib import contextmanager

import pymysql
from fastapi import FastAPI, Header, HTTPException, Request


DB_CONFIG = {
    "host": os.getenv("DB_HOST", "mysql"),
    "port": int(os.getenv("DB_PORT", "3306")),
    "user": os.getenv("DB_USER", "hyy_collision"),
    "password": os.getenv("DB_PASSWORD", ""),
    "database": os.getenv("DB_NAME", "hyy_collision"),
    "charset": "ascii",
    "autocommit": True,
    "connect_timeout": 2,
    "read_timeout": 2,
    "write_timeout": 2,
    "cursorclass": pymysql.cursors.DictCursor,
}

ACCESS_TOKEN = os.getenv("ACCESS_TOKEN", "")
POOL_SIZE = int(os.getenv("POOL_SIZE", "8"))

app = FastAPI(title="HYY collision service")
pool: queue.LifoQueue = queue.LifoQueue(maxsize=POOL_SIZE)


def new_conn():
    return pymysql.connect(**DB_CONFIG)


@contextmanager
def get_conn():
    try:
        conn = pool.get_nowait()
    except queue.Empty:
        conn = new_conn()
    try:
        conn.ping(reconnect=True)
        yield conn
    finally:
        try:
            pool.put_nowait(conn)
        except queue.Full:
            conn.close()


@app.on_event("startup")
def startup():
    for _ in range(max(1, POOL_SIZE // 2)):
        pool.put_nowait(new_conn())


@app.on_event("shutdown")
def shutdown():
    while not pool.empty():
        try:
            pool.get_nowait().close()
        except Exception:
            pass


def assert_token(token: str | None):
    if ACCESS_TOKEN and token != ACCESS_TOKEN:
        raise HTTPException(status_code=401, detail="unauthorized")


def normalized_md5(value):
    value = (value or "").strip().lower()
    if len(value) == 32 and all(ch in "0123456789abcdef" for ch in value):
        return value
    return ""


@app.get("/health")
def health():
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT 1 AS ok")
            return {"ok": cur.fetchone()["ok"] == 1}


@app.post("/api/hyy-collision/access-check")
async def access_check(request: Request, x_collision_token: str | None = Header(default=None)):
    assert_token(x_collision_token)
    body = await request.json()
    phone_code = str(body.get("phoneCode") or body.get("phone_code") or "").strip()
    if not (len(phone_code) == 8 and phone_code.isdigit()):
        raise HTTPException(status_code=400, detail="phoneCode must be 8 digits")
    name_md5 = normalized_md5(body.get("nameMd5") or body.get("name_md5"))
    idno_md5 = normalized_md5(body.get("idnoMd5") or body.get("idno_md5"))

    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT mobile_md5 FROM hyy_collision_mobile WHERE phone_prefix8=%s",
                (phone_code,),
            )
            md5_list = [row["mobile_md5"] for row in cur.fetchall()]

            name_hit = False
            if name_md5:
                cur.execute(
                    """
                    SELECT 1 AS hit
                    FROM hyy_collision_identity
                    WHERE phone_prefix8=%s AND name_md5=%s
                    LIMIT 1
                    """,
                    (phone_code, name_md5),
                )
                name_hit = cur.fetchone() is not None

            idno_hit = False
            if idno_md5:
                cur.execute(
                    """
                    SELECT 1 AS hit
                    FROM hyy_collision_identity
                    WHERE phone_prefix8=%s AND idno_md5=%s
                    LIMIT 1
                    """,
                    (phone_code, idno_md5),
                )
                idno_hit = cur.fetchone() is not None

    return {
        "success": True,
        "duplicated": name_hit or idno_hit,
        "nameHit": name_hit,
        "idnoHit": idno_hit,
        "md5Count": len(md5_list),
        "md5List": md5_list,
    }

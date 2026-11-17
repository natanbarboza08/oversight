"""Configuration for the ETL service and its PostgreSQL connection."""
import os
from pathlib import Path

from dotenv import load_dotenv
from sqlalchemy.engine import URL

load_dotenv(Path(__file__).resolve().parent / ".env")


def database_url() -> URL:
    """Build a Neon-compatible PostgreSQL URL and fail if required settings are missing."""
    required = ("DB_HOST", "DB_NAME", "DB_USER", "DB_PASSWORD")
    missing = [name for name in required if not os.getenv(name, "").strip()]
    if missing:
        raise RuntimeError(f"Variáveis de banco ausentes: {', '.join(missing)}.")

    return URL.create(
        "postgresql+psycopg",
        username=os.environ["DB_USER"],
        password=os.environ["DB_PASSWORD"],
        host=os.environ["DB_HOST"],
        port=int(os.getenv("DB_PORT", "5432")),
        database=os.environ["DB_NAME"],
        query={"sslmode": os.getenv("DB_SSLMODE", "require")},
    )

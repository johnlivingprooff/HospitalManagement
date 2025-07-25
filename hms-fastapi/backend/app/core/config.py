import os
import secrets
import warnings
from dotenv import load_dotenv
from typing import List
from pydantic_settings import BaseSettings

load_dotenv()

def _get_secure_fallback_key() -> str:
    """Generate a secure fallback key if SECRET_KEY is not provided"""
    fallback_key = secrets.token_hex(32)
    warnings.warn(
        "⚠️  SECRET_KEY not found in environment! Using auto-generated key. "
        "This will change on every restart. Set SECRET_KEY in environment for production.",
        UserWarning
    )
    return fallback_key

class Settings(BaseSettings):
    # Database
    DATABASE_URL: str = os.getenv("DATABASE_URL", "postgresql://postgres:user_password@localhost:5432/hms")

    # Redis Cache
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://localhost:6379")
    CACHE_TTL: int = int(os.getenv("CACHE_TTL", "300"))
    SEARCH_CACHE_TTL: int = int(os.getenv("SEARCH_CACHE_TTL", "300"))
    STATS_CACHE_TTL: int = int(os.getenv("STATS_CACHE_TTL", "600"))

    # Security
    SECRET_KEY: str = os.getenv("SECRET_KEY") or _get_secure_fallback_key()
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    # CORS 
    ALLOWED_ORIGINS: str = ""

    @property
    def allowed_origins_list(self) -> List[str]:
        """Convert comma-separated ALLOWED_ORIGINS string to list"""
        if not self.ALLOWED_ORIGINS:
            return []
        return [origin.strip() for origin in self.ALLOWED_ORIGINS.split(",") if origin.strip()]

    # App
    APP_NAME: str = "HMS FastAPI"
    VERSION: str = "1.0.0"
    DEBUG: bool = os.getenv("DEBUG", "false").lower() == "true"

    # Server
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))

    class Config:
        env_file = ".env"

settings = Settings()

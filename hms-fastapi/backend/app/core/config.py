import os
import secrets
import warnings
from dotenv import load_dotenv
import json
from pydantic_settings import BaseSettings

load_dotenv()

def _get_secure_fallback_key() -> str:
    """Generate a secure fallback key if SECRET_KEY is not provided"""
    # Generate a secure random key
    fallback_key = secrets.token_hex(32)
    
    # Warn about using fallback key
    warnings.warn(
        "⚠️  SECRET_KEY not found in environment! Using auto-generated key. "
        "This will change on every restart. Set SECRET_KEY in environment for production.",
        UserWarning
    )
    
    return fallback_key

class Settings(BaseSettings):
    # Database
    DATABASE_URL: str = os.getenv("DATABASE_URL", "postgresql://hms_usr:MxmDMXGKYDSY7itDOU5TNabHst1r0wq0@dpg-d1trnuc9c44c73ccdm0g-a.oregon-postgres.render.com/hms_oq9k")
    
    # Redis Cache
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://localhost:6379")
    CACHE_TTL: int = int(os.getenv("CACHE_TTL", "300"))  # 5 minutes default
    SEARCH_CACHE_TTL: int = int(os.getenv("SEARCH_CACHE_TTL", "300"))  # 5 minutes for search results
    STATS_CACHE_TTL: int = int(os.getenv("STATS_CACHE_TTL", "600"))  # 10 minutes for statistics
    
    # Security
    SECRET_KEY: str = os.getenv("SECRET_KEY") or _get_secure_fallback_key()
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    # CORS
    ALLOWED_ORIGINS: list = []

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        origins_env = os.getenv("ALLOWED_ORIGINS")
        if origins_env:
            try:
                self.ALLOWED_ORIGINS = json.loads(origins_env)
            except json.JSONDecodeError:
                print("⚠️ Failed to parse ALLOWED_ORIGINS from .env. Falling back to default.")
                self.ALLOWED_ORIGINS = []
    
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

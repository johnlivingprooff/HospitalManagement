from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from app.core.config import settings

# Create SQLAlchemy engine with SSL support for PostgreSQL
connect_args = {}
if settings.DATABASE_URL.startswith("postgresql"):
    # Add SSL configuration for PostgreSQL connections
    # This is required for cloud databases like Render, Railway, etc.
    connect_args = {
        "sslmode": "require",
        "connect_timeout": 10
    }

engine = create_engine(
        settings.DATABASE_URL,
        pool_pre_ping=True,  # Verify connections before use
        pool_recycle=3600,   # Recycle connections every hour
        pool_timeout=30,     # Timeout after 30 seconds
        connect_args=connect_args
    )

# Create SessionLocal class
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Create Base class for models
Base = declarative_base()

# Dependency to get database session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Create tables
def create_tables():
    # Base.metadata.drop_all(bind=engine)  # Optional: drop existing tables
    Base.metadata.create_all(bind=engine)

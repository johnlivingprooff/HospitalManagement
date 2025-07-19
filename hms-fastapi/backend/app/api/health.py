# Add health check endpoint to FastAPI
from fastapi import APIRouter

router = APIRouter()

@router.get("/health")
async def health_check():
    """Health check endpoint for Docker containers"""
    return {
        "status": "healthy",
        "service": "HMS Backend API",
        "version": "1.0.0"
    }

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from contextlib import asynccontextmanager
import os
from app.core.config import settings
from app.core.database import create_tables
from app.core.search_init import initialize_search_optimization
from app.core.seed import initialize_database
from app.services.cache_service import cache_service
from app.api import auth, patients, appointments, users, bills, medical_records, lab_tests, prescriptions, dashboard, wards

# Create tables and initialize search optimization on startup
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    print("🚀 Starting HMS FastAPI application...")
    create_tables()
    
    # Initialize database with admin user and sample data
    await initialize_database()
    
    # Initialize cache service
    await cache_service.initialize()
    
    # Initialize search optimization (indexes, cache, etc.)
    await initialize_search_optimization()
    
    print("✅ HMS FastAPI application started successfully")
    yield
    
    # Shutdown
    print("🔄 Shutting down HMS FastAPI application...")
    await cache_service.close()
    print("✅ HMS FastAPI application shut down successfully")

# Create FastAPI app
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.VERSION,
    debug=settings.DEBUG,
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(auth.router, prefix="/api/auth", tags=["Authentication"])
app.include_router(dashboard.router, prefix="/api/dashboard", tags=["Dashboard"])
app.include_router(users.router, prefix="/api/users", tags=["Users"])
app.include_router(patients.router, prefix="/api/patients", tags=["Patients"])
app.include_router(appointments.router, prefix="/api/appointments", tags=["Appointments"])
app.include_router(bills.router, prefix="/api/bills", tags=["Bills"])
app.include_router(medical_records.router, prefix="/api/medical-records", tags=["Medical Records"])
app.include_router(lab_tests.router, prefix="/api/lab-tests", tags=["Lab Tests"])
app.include_router(prescriptions.router, prefix="/api/prescriptions", tags=["Prescriptions"])
app.include_router(wards.router, prefix="/api/wards", tags=["Wards"])

# Health check endpoint
@app.get("/")
async def root():
    return {
        "message": "Hospital Management System API",
        "version": settings.VERSION,
        "status": "healthy"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy", "version": settings.VERSION}

# Static files for uploads/attachments
if not os.path.exists("static"):
    os.makedirs("static")

app.mount("/static", StaticFiles(directory="static"), name="static")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG
    )

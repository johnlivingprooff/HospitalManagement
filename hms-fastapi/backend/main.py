from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.database import engine
from app.models import Base
from app.api import auth, users, patients, appointments, medical_records, prescriptions, lab_tests, bills, wards
from app.api.health import router as health_router

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Hospital Management System API",
    description="Modern FastAPI backend for HMS",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:80", "http://localhost"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health_router, tags=["Health"])
app.include_router(auth.router, prefix="/api/auth", tags=["Authentication"])
app.include_router(users.router, prefix="/api/users", tags=["Users"])
app.include_router(patients.router, prefix="/api/patients", tags=["Patients"])
app.include_router(appointments.router, prefix="/api/appointments", tags=["Appointments"])
app.include_router(medical_records.router, prefix="/api/medical-records", tags=["Medical Records"])
app.include_router(prescriptions.router, prefix="/api/prescriptions", tags=["Prescriptions"])
app.include_router(lab_tests.router, prefix="/api/lab-tests", tags=["Lab Tests"])
app.include_router(bills.router, prefix="/api/bills", tags=["Bills"])
app.include_router(wards.router, prefix="/api/wards", tags=["Wards"])

@app.get("/")
async def root():
    return {"message": "Hospital Management System API", "status": "running"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

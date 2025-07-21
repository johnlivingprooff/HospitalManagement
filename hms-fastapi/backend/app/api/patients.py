from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import or_
from typing import List, Optional
from app.core.database import get_db
from app.models import Patient, User
from app.schemas import PatientCreate, PatientUpdate, PatientResponse
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[PatientResponse])
@router.get("", response_model=List[PatientResponse])
async def get_patients(
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    search: Optional[str] = Query(None),
    is_active: Optional[bool] = Query(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all patients with optional search and filters"""
    query = db.query(Patient)
    
    # Apply search filter
    if search:
        query = query.filter(
            or_(
                Patient.first_name.ilike(f"%{search}%"),
                Patient.last_name.ilike(f"%{search}%"),
                Patient.email.ilike(f"%{search}%"),
                Patient.phone.ilike(f"%{search}%")
            )
        )
    
    # Apply active filter
    if is_active is not None:
        query = query.filter(Patient.is_active == is_active)
    
    # Apply pagination
    patients = query.offset(skip).limit(limit).all()
    
    return [PatientResponse.from_orm(patient) for patient in patients]

@router.get("/{patient_id}", response_model=PatientResponse)
async def get_patient(
    patient_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific patient by ID"""
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    return PatientResponse.from_orm(patient)

@router.post("/", response_model=PatientResponse)
@router.post("", response_model=PatientResponse)
async def create_patient(
    patient_data: PatientCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new patient"""
    # Check if patient with email already exists
    if patient_data.email:
        existing_patient = db.query(Patient).filter(Patient.email == patient_data.email).first()
        if existing_patient:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Patient with this email already exists"
            )
    
    # Create new patient
    patient = Patient(
        **patient_data.dict(),
        created_by_id=current_user.id
    )
    
    db.add(patient)
    db.commit()
    db.refresh(patient)
    
    return PatientResponse.from_orm(patient)

@router.put("/{patient_id}", response_model=PatientResponse)
async def update_patient(
    patient_id: int,
    patient_update: PatientUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update an existing patient"""
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Check if email is being updated and already exists
    if patient_update.email and patient_update.email != patient.email:
        existing_patient = db.query(Patient).filter(Patient.email == patient_update.email).first()
        if existing_patient:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Patient with this email already exists"
            )
    
    # Update patient fields
    update_data = patient_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(patient, field, value)
    
    db.commit()
    db.refresh(patient)
    
    return PatientResponse.from_orm(patient)

@router.delete("/{patient_id}")
async def delete_patient(
    patient_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Soft delete a patient (set is_active to False)"""
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Soft delete by setting is_active to False
    patient.is_active = False
    db.commit()
    
    return {"message": "Patient deactivated successfully"}

@router.get("/{patient_id}/appointments")
async def get_patient_appointments(
    patient_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all appointments for a specific patient"""
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # This will be implemented when we create the appointment model
    return {"message": "Patient appointments endpoint", "patient_id": patient_id}

@router.get("/{patient_id}/medical-records")
async def get_patient_medical_records(
    patient_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all medical records for a specific patient"""
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # This will be implemented when we create the medical record model
    return {"message": "Patient medical records endpoint", "patient_id": patient_id}

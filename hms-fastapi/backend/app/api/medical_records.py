from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session, selectinload
from app.core.database import get_db
from app.models import MedicalRecord, Patient, User
from app.schemas import (
    MedicalRecordCreate,
    MedicalRecordUpdate,
    MedicalRecordResponse
)
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[MedicalRecordResponse])
def get_medical_records(
    skip: int = 0,
    limit: int = 100,
    patient_id: int = None,
    record_type: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all medical records with optional filtering"""
    query = db.query(MedicalRecord).options(
        selectinload(MedicalRecord.patient),
        selectinload(MedicalRecord.doctor)
    )
    
    # Filter by patient if specified
    if patient_id:
        query = query.filter(MedicalRecord.patient_id == patient_id)
    
    # Filter by record type if specified
    if record_type:
        query = query.filter(MedicalRecord.record_type == record_type)
    
    # Order by most recent first
    query = query.order_by(MedicalRecord.created_at.desc())
    
    medical_records = query.offset(skip).limit(limit).all()
    return medical_records

@router.get("/{record_id}", response_model=MedicalRecordResponse)
def get_medical_record(
    record_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific medical record by ID"""
    medical_record = db.query(MedicalRecord).options(
        selectinload(MedicalRecord.patient),
        selectinload(MedicalRecord.doctor)
    ).filter(MedicalRecord.id == record_id).first()
    
    if not medical_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medical record not found"
        )
    
    return medical_record

@router.post("/", response_model=MedicalRecordResponse)
def create_medical_record(
    medical_record: MedicalRecordCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new medical record"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == medical_record.patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Verify doctor exists
    doctor = db.query(User).filter(User.id == medical_record.doctor_id).first()
    if not doctor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Doctor not found"
        )
    
    # Create the medical record
    db_medical_record = MedicalRecord(**medical_record.dict())
    db.add(db_medical_record)
    db.commit()
    db.refresh(db_medical_record)
    
    return db_medical_record

@router.put("/{record_id}", response_model=MedicalRecordResponse)
def update_medical_record(
    record_id: int,
    medical_record_update: MedicalRecordUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update a medical record"""
    # Get the existing medical record
    db_medical_record = db.query(MedicalRecord).filter(MedicalRecord.id == record_id).first()
    
    if not db_medical_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medical record not found"
        )
    
    # Update the fields
    update_data = medical_record_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_medical_record, field, value)
    
    db.commit()
    db.refresh(db_medical_record)
    
    return db_medical_record

@router.delete("/{record_id}")
def delete_medical_record(
    record_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Delete a medical record"""
    db_medical_record = db.query(MedicalRecord).filter(MedicalRecord.id == record_id).first()
    
    if not db_medical_record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medical record not found"
        )
    
    db.delete(db_medical_record)
    db.commit()
    
    return {"message": "Medical record deleted successfully"}

@router.get("/patient/{patient_id}", response_model=List[MedicalRecordResponse])
def get_patient_medical_records(
    patient_id: int,
    skip: int = 0,
    limit: int = 100,
    record_type: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all medical records for a specific patient"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    query = db.query(MedicalRecord).options(
        selectinload(MedicalRecord.patient),
        selectinload(MedicalRecord.doctor)
    ).filter(MedicalRecord.patient_id == patient_id)
    
    # Filter by record type if specified
    if record_type:
        query = query.filter(MedicalRecord.record_type == record_type)
    
    # Order by most recent first
    query = query.order_by(MedicalRecord.created_at.desc())
    
    medical_records = query.offset(skip).limit(limit).all()
    return medical_records

@router.get("/doctor/{doctor_id}", response_model=List[MedicalRecordResponse])
def get_doctor_medical_records(
    doctor_id: int,
    skip: int = 0,
    limit: int = 100,
    record_type: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all medical records created by a specific doctor"""
    # Verify doctor exists
    doctor = db.query(User).filter(User.id == doctor_id).first()
    if not doctor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Doctor not found"
        )
    
    query = db.query(MedicalRecord).options(
        selectinload(MedicalRecord.patient),
        selectinload(MedicalRecord.doctor)
    ).filter(MedicalRecord.doctor_id == doctor_id)
    
    # Filter by record type if specified
    if record_type:
        query = query.filter(MedicalRecord.record_type == record_type)
    
    # Order by most recent first
    query = query.order_by(MedicalRecord.created_at.desc())
    
    medical_records = query.offset(skip).limit(limit).all()
    return medical_records

@router.get("/types/", response_model=List[str])
def get_record_types(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all available medical record types"""
    # Common medical record types
    record_types = [
        "consultation",
        "lab_result",
        "prescription",
        "diagnosis",
        "treatment_plan",
        "progress_note",
        "discharge_summary",
        "vaccination",
        "surgery_report",
        "referral"
    ]
    return record_types

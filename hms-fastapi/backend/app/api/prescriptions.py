from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session, selectinload
from app.core.database import get_db
from app.models import Prescription, Patient, User
from app.schemas import (
    PrescriptionCreate,
    PrescriptionUpdate,
    PrescriptionResponse
)
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[PrescriptionResponse])
def get_prescriptions(
    skip: int = 0,
    limit: int = 100,
    patient_id: int = None,
    status: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all prescriptions with optional filtering"""
    query = db.query(Prescription).options(
        selectinload(Prescription.patient),
        selectinload(Prescription.doctor)
    )
    
    # Filter by patient if specified
    if patient_id:
        query = query.filter(Prescription.patient_id == patient_id)
    
    # Filter by status if specified
    if status:
        query = query.filter(Prescription.status == status)
    
    # Order by most recent first
    query = query.order_by(Prescription.created_at.desc())
    
    prescriptions = query.offset(skip).limit(limit).all()
    return prescriptions

@router.get("/{prescription_id}", response_model=PrescriptionResponse)
def get_prescription(
    prescription_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific prescription by ID"""
    prescription = db.query(Prescription).options(
        selectinload(Prescription.patient),
        selectinload(Prescription.doctor)
    ).filter(Prescription.id == prescription_id).first()
    
    if not prescription:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Prescription not found"
        )
    
    return prescription

@router.post("/", response_model=PrescriptionResponse)
def create_prescription(
    prescription: PrescriptionCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new prescription"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == prescription.patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Verify doctor exists
    doctor = db.query(User).filter(User.id == prescription.doctor_id).first()
    if not doctor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Doctor not found"
        )
    
    # Create the prescription
    db_prescription = Prescription(**prescription.dict())
    db.add(db_prescription)
    db.commit()
    db.refresh(db_prescription)
    
    return db_prescription

@router.put("/{prescription_id}", response_model=PrescriptionResponse)
def update_prescription(
    prescription_id: int,
    prescription_update: PrescriptionUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update a prescription"""
    # Get the existing prescription
    db_prescription = db.query(Prescription).filter(Prescription.id == prescription_id).first()
    
    if not db_prescription:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Prescription not found"
        )
    
    # Update the fields
    update_data = prescription_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_prescription, field, value)
    
    db.commit()
    db.refresh(db_prescription)
    
    return db_prescription

@router.delete("/{prescription_id}")
def delete_prescription(
    prescription_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Delete a prescription"""
    db_prescription = db.query(Prescription).filter(Prescription.id == prescription_id).first()
    
    if not db_prescription:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Prescription not found"
        )
    
    db.delete(db_prescription)
    db.commit()
    
    return {"message": "Prescription deleted successfully"}

@router.get("/patient/{patient_id}", response_model=List[PrescriptionResponse])
def get_patient_prescriptions(
    patient_id: int,
    skip: int = 0,
    limit: int = 100,
    status: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all prescriptions for a specific patient"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    query = db.query(Prescription).options(
        selectinload(Prescription.patient),
        selectinload(Prescription.doctor)
    ).filter(Prescription.patient_id == patient_id)
    
    # Filter by status if specified
    if status:
        query = query.filter(Prescription.status == status)
    
    # Order by most recent first
    query = query.order_by(Prescription.created_at.desc())
    
    prescriptions = query.offset(skip).limit(limit).all()
    return prescriptions

@router.put("/{prescription_id}/dispense", response_model=PrescriptionResponse)
def dispense_prescription(
    prescription_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Mark a prescription as dispensed"""
    db_prescription = db.query(Prescription).filter(Prescription.id == prescription_id).first()
    
    if not db_prescription:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Prescription not found"
        )
    
    if db_prescription.status == "dispensed":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Prescription already dispensed"
        )
    
    db_prescription.status = "dispensed"
    db.commit()
    db.refresh(db_prescription)
    
    return db_prescription

from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session, selectinload
from app.core.database import get_db
from app.models import LabTest, Patient, User
from app.schemas import (
    LabTestCreate,
    LabTestUpdate,
    LabTestResponse
)
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[LabTestResponse])
@router.get("", response_model=List[LabTestResponse])
def get_lab_tests(
    skip: int = 0,
    limit: int = 100,
    patient_id: int = None,
    status: str = None,
    test_type: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all lab tests with optional filtering"""
    query = db.query(LabTest).options(
        selectinload(LabTest.patient),
        selectinload(LabTest.doctor)
    )
    
    # Filter by patient if specified
    if patient_id:
        query = query.filter(LabTest.patient_id == patient_id)
    
    # Filter by status if specified
    if status:
        query = query.filter(LabTest.status == status)
    
    # Filter by test type if specified
    if test_type:
        query = query.filter(LabTest.test_type == test_type)
    
    # Order by most recent first
    query = query.order_by(LabTest.ordered_date.desc())
    
    lab_tests = query.offset(skip).limit(limit).all()
    return lab_tests

@router.get("/{test_id}", response_model=LabTestResponse)
def get_lab_test(
    test_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific lab test by ID"""
    lab_test = db.query(LabTest).options(
        selectinload(LabTest.patient),
        selectinload(LabTest.doctor)
    ).filter(LabTest.id == test_id).first()
    
    if not lab_test:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lab test not found"
        )
    
    return lab_test

@router.post("/", response_model=LabTestResponse)
@router.post("", response_model=LabTestResponse)
def create_lab_test(
    lab_test: LabTestCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new lab test"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == lab_test.patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Verify doctor exists
    doctor = db.query(User).filter(User.id == lab_test.doctor_id).first()
    if not doctor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Doctor not found"
        )
    
    # Create the lab test
    db_lab_test = LabTest(**lab_test.dict())
    db.add(db_lab_test)
    db.commit()
    db.refresh(db_lab_test)
    
    return db_lab_test

@router.put("/{test_id}", response_model=LabTestResponse)
def update_lab_test(
    test_id: int,
    lab_test_update: LabTestUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update a lab test"""
    # Get the existing lab test
    db_lab_test = db.query(LabTest).filter(LabTest.id == test_id).first()
    
    if not db_lab_test:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lab test not found"
        )
    
    # Update the fields
    update_data = lab_test_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_lab_test, field, value)
    
    db.commit()
    db.refresh(db_lab_test)
    
    return db_lab_test

@router.delete("/{test_id}")
def delete_lab_test(
    test_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Delete a lab test"""
    db_lab_test = db.query(LabTest).filter(LabTest.id == test_id).first()
    
    if not db_lab_test:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lab test not found"
        )
    
    db.delete(db_lab_test)
    db.commit()
    
    return {"message": "Lab test deleted successfully"}

@router.get("/patient/{patient_id}", response_model=List[LabTestResponse])
def get_patient_lab_tests(
    patient_id: int,
    skip: int = 0,
    limit: int = 100,
    status: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all lab tests for a specific patient"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    query = db.query(LabTest).options(
        selectinload(LabTest.patient),
        selectinload(LabTest.doctor)
    ).filter(LabTest.patient_id == patient_id)
    
    # Filter by status if specified
    if status:
        query = query.filter(LabTest.status == status)
    
    # Order by most recent first
    query = query.order_by(LabTest.ordered_date.desc())
    
    lab_tests = query.offset(skip).limit(limit).all()
    return lab_tests

@router.get("/types/", response_model=List[str])
def get_test_types(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all available lab test types"""
    test_types = [
        "blood_test",
        "urine_test",
        "x_ray",
        "mri",
        "ct_scan",
        "ultrasound",
        "ecg",
        "blood_sugar",
        "cholesterol",
        "liver_function",
        "kidney_function",
        "thyroid_function"
    ]
    return test_types

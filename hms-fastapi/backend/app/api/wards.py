from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import or_, and_, func
from typing import List, Optional
from app.core.database import get_db
from app.models import Ward, WardPatient, Patient, User
from app.schemas import (
    WardCreate, WardUpdate, WardResponse, 
    WardPatientCreate, WardPatientUpdate, WardPatientResponse
)
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[WardResponse])
@router.get("", response_model=List[WardResponse])
async def get_wards(
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    search: Optional[str] = Query(None),
    ward_type: Optional[str] = Query(None),
    is_active: Optional[bool] = Query(None),
    floor: Optional[int] = Query(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all wards with optional search and filters"""
    # Build query with eager loading
    query = db.query(Ward).options(
        joinedload(Ward.patients).joinedload(WardPatient.patient),
        joinedload(Ward.patients).joinedload(WardPatient.doctor)
    )
    
    # Apply search filter
    if search:
        query = query.filter(
            or_(
                Ward.name.ilike(f"%{search}%"),
                Ward.type.ilike(f"%{search}%"),
                Ward.description.ilike(f"%{search}%")
            )
        )
    
    # Apply filters
    if ward_type:
        query = query.filter(Ward.type == ward_type)
    
    if is_active is not None:
        query = query.filter(Ward.is_active == is_active)
        
    if floor is not None:
        query = query.filter(Ward.floor == floor)
    
    # Apply pagination
    wards = query.offset(skip).limit(limit).all()
    
    # Update current occupancy for each ward
    for ward in wards:
        current_count = db.query(WardPatient).filter(
            and_(
                WardPatient.ward_id == ward.id,
                WardPatient.discharge_date.is_(None)
            )
        ).count()
        
        if ward.current_occupancy != current_count:
            ward.current_occupancy = current_count
            db.commit()
    
    return wards

@router.get("/{ward_id}", response_model=WardResponse)
async def get_ward(
    ward_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific ward by ID with all current patients"""
    ward = db.query(Ward).options(
        joinedload(Ward.patients).joinedload(WardPatient.patient),
        joinedload(Ward.patients).joinedload(WardPatient.doctor)
    ).filter(Ward.id == ward_id).first()
    
    if not ward:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward not found"
        )
    
    # Update current occupancy
    current_count = db.query(WardPatient).filter(
        and_(
            WardPatient.ward_id == ward.id,
            WardPatient.discharge_date.is_(None)
        )
    ).count()
    
    if ward.current_occupancy != current_count:
        ward.current_occupancy = current_count
        db.commit()
    
    # Filter out discharged patients for the response
    ward.patients = [wp for wp in ward.patients if wp.discharge_date is None]
    
    return ward

@router.post("/", response_model=WardResponse)
async def create_ward(
    ward_data: WardCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new ward"""
    # Check if user has permission (admin or doctor)
    if current_user.role not in ["admin", "doctor"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    # Check if ward name already exists
    existing_ward = db.query(Ward).filter(Ward.name == ward_data.name).first()
    if existing_ward:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ward name already exists"
        )
    
    # Create new ward
    db_ward = Ward(**ward_data.dict())
    db.add(db_ward)
    db.commit()
    db.refresh(db_ward)
    
    return db_ward

@router.put("/{ward_id}", response_model=WardResponse)
async def update_ward(
    ward_id: int,
    ward_data: WardUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update a ward"""
    # Check if user has permission (admin or doctor)
    if current_user.role not in ["admin", "doctor"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    # Get the ward
    ward = db.query(Ward).filter(Ward.id == ward_id).first()
    if not ward:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward not found"
        )
    
    # Check if new name already exists (if name is being changed)
    if ward_data.name and ward_data.name != ward.name:
        existing_ward = db.query(Ward).filter(Ward.name == ward_data.name).first()
        if existing_ward:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Ward name already exists"
            )
    
    # Update ward fields
    update_data = ward_data.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(ward, field, value)
    
    db.commit()
    db.refresh(ward)
    
    return ward

@router.delete("/{ward_id}")
async def delete_ward(
    ward_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Delete a ward"""
    # Check if user has permission (admin only)
    if current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    # Get the ward
    ward = db.query(Ward).filter(Ward.id == ward_id).first()
    if not ward:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward not found"
        )
    
    # Check if ward has current patients
    current_patients = db.query(WardPatient).filter(
        and_(
            WardPatient.ward_id == ward_id,
            WardPatient.discharge_date.is_(None)
        )
    ).count()
    
    if current_patients > 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot delete ward with current patients. Please discharge all patients first."
        )
    
    db.delete(ward)
    db.commit()
    
    return {"message": "Ward deleted successfully"}

# Ward Patient Endpoints

@router.post("/{ward_id}/patients", response_model=WardPatientResponse)
async def admit_patient_to_ward(
    ward_id: int,
    patient_data: WardPatientCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Admit a patient to a ward"""
    # Check if user has permission (admin, doctor, or nurse)
    if current_user.role not in ["admin", "doctor", "nurse"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    # Check if ward exists
    ward = db.query(Ward).filter(Ward.id == ward_id).first()
    if not ward:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward not found"
        )
    
    # Check if ward is active
    if not ward.is_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ward is inactive"
        )
    
    # Check ward capacity
    current_occupancy = db.query(WardPatient).filter(
        and_(
            WardPatient.ward_id == ward_id,
            WardPatient.discharge_date.is_(None)
        )
    ).count()
    
    if current_occupancy >= ward.capacity:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ward is at full capacity"
        )
    
    # Check if patient exists
    patient = db.query(Patient).filter(Patient.id == patient_data.patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Check if patient is already admitted to any ward
    existing_admission = db.query(WardPatient).filter(
        and_(
            WardPatient.patient_id == patient_data.patient_id,
            WardPatient.discharge_date.is_(None)
        )
    ).first()
    
    if existing_admission:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Patient is already admitted to a ward"
        )
    
    # Check if bed number is available in this ward
    existing_bed = db.query(WardPatient).filter(
        and_(
            WardPatient.ward_id == ward_id,
            WardPatient.bed_number == patient_data.bed_number,
            WardPatient.discharge_date.is_(None)
        )
    ).first()
    
    if existing_bed:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Bed number is already occupied"
        )
    
    # Check if doctor exists (if provided)
    if patient_data.doctor_id:
        doctor = db.query(User).filter(
            and_(
                User.id == patient_data.doctor_id,
                User.role.in_(["admin", "doctor"])
            )
        ).first()
        if not doctor:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Doctor not found"
            )
    
    # Create ward patient admission
    db_ward_patient = WardPatient(
        ward_id=ward_id,
        **patient_data.dict()
    )
    
    db.add(db_ward_patient)
    
    # Update ward occupancy
    ward.current_occupancy = current_occupancy + 1
    
    db.commit()
    db.refresh(db_ward_patient)
    
    # Load relationships
    db_ward_patient = db.query(WardPatient).options(
        joinedload(WardPatient.patient),
        joinedload(WardPatient.doctor)
    ).filter(WardPatient.id == db_ward_patient.id).first()
    
    return db_ward_patient

@router.put("/patients/{ward_patient_id}", response_model=WardPatientResponse)
async def update_ward_patient(
    ward_patient_id: int,
    patient_data: WardPatientUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update ward patient information"""
    # Check if user has permission
    if current_user.role not in ["admin", "doctor", "nurse"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    # Get the ward patient
    ward_patient = db.query(WardPatient).filter(WardPatient.id == ward_patient_id).first()
    if not ward_patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward patient record not found"
        )
    
    # Check if bed number is changing and if it's available
    if patient_data.bed_number and patient_data.bed_number != ward_patient.bed_number:
        existing_bed = db.query(WardPatient).filter(
            and_(
                WardPatient.ward_id == ward_patient.ward_id,
                WardPatient.bed_number == patient_data.bed_number,
                WardPatient.discharge_date.is_(None),
                WardPatient.id != ward_patient_id
            )
        ).first()
        
        if existing_bed:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Bed number is already occupied"
            )
    
    # Check if doctor exists (if provided)
    if patient_data.doctor_id:
        doctor = db.query(User).filter(
            and_(
                User.id == patient_data.doctor_id,
                User.role.in_(["admin", "doctor"])
            )
        ).first()
        if not doctor:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Doctor not found"
            )
    
    # Update ward patient fields
    update_data = patient_data.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(ward_patient, field, value)
    
    db.commit()
    db.refresh(ward_patient)
    
    # Load relationships
    ward_patient = db.query(WardPatient).options(
        joinedload(WardPatient.patient),
        joinedload(WardPatient.doctor)
    ).filter(WardPatient.id == ward_patient_id).first()
    
    return ward_patient

@router.put("/patients/{ward_patient_id}/discharge", response_model=WardPatientResponse)
async def discharge_patient(
    ward_patient_id: int,
    discharge_data: dict,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Discharge a patient from ward"""
    # Check if user has permission
    if current_user.role not in ["admin", "doctor", "nurse"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    # Get the ward patient
    ward_patient = db.query(WardPatient).filter(WardPatient.id == ward_patient_id).first()
    if not ward_patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward patient record not found"
        )
    
    # Check if patient is already discharged
    if ward_patient.discharge_date is not None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Patient is already discharged"
        )
    
    # Update discharge information
    from datetime import datetime
    ward_patient.discharge_date = datetime.fromisoformat(discharge_data.get("discharge_date", datetime.now().isoformat()))
    ward_patient.status = "discharged"
    if discharge_data.get("notes"):
        ward_patient.notes = discharge_data.get("notes")
    
    # Update ward occupancy
    ward = db.query(Ward).filter(Ward.id == ward_patient.ward_id).first()
    if ward:
        ward.current_occupancy = max(0, ward.current_occupancy - 1)
    
    db.commit()
    db.refresh(ward_patient)
    
    # Load relationships
    ward_patient = db.query(WardPatient).options(
        joinedload(WardPatient.patient),
        joinedload(WardPatient.doctor)
    ).filter(WardPatient.id == ward_patient_id).first()
    
    return ward_patient

@router.get("/patients/{ward_patient_id}", response_model=WardPatientResponse)
async def get_ward_patient(
    ward_patient_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific ward patient record"""
    ward_patient = db.query(WardPatient).options(
        joinedload(WardPatient.patient),
        joinedload(WardPatient.doctor)
    ).filter(WardPatient.id == ward_patient_id).first()
    
    if not ward_patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ward patient record not found"
        )
    
    return ward_patient

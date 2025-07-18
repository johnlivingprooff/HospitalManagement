from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_
from typing import List, Optional
from datetime import datetime, date
from app.core.database import get_db
from app.models import Appointment, Patient, User
from app.schemas import AppointmentCreate, AppointmentUpdate, AppointmentResponse
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[AppointmentResponse])
async def get_appointments(
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    patient_id: Optional[int] = Query(None),
    doctor_id: Optional[int] = Query(None),
    date: Optional[date] = Query(None),
    status: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all appointments with optional filters"""
    query = db.query(Appointment)
    
    # Apply filters
    if patient_id:
        query = query.filter(Appointment.patient_id == patient_id)
    
    if doctor_id:
        query = query.filter(Appointment.doctor_id == doctor_id)
    
    if date:
        # Filter by date (regardless of time)
        start_of_day = datetime.combine(date, datetime.min.time())
        end_of_day = datetime.combine(date, datetime.max.time())
        query = query.filter(
            and_(
                Appointment.appointment_date >= start_of_day,
                Appointment.appointment_date <= end_of_day
            )
        )
    
    if status:
        query = query.filter(Appointment.status == status)
    
    # Apply pagination
    appointments = query.offset(skip).limit(limit).all()
    
    return [AppointmentResponse.from_orm(appointment) for appointment in appointments]

@router.get("/{appointment_id}", response_model=AppointmentResponse)
async def get_appointment(
    appointment_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific appointment by ID"""
    appointment = db.query(Appointment).filter(Appointment.id == appointment_id).first()
    
    if not appointment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Appointment not found"
        )
    
    return AppointmentResponse.from_orm(appointment)

@router.post("/", response_model=AppointmentResponse)
async def create_appointment(
    appointment_data: AppointmentCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new appointment"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == appointment_data.patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    # Verify doctor exists
    doctor = db.query(User).filter(User.id == appointment_data.doctor_id).first()
    if not doctor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Doctor not found"
        )
    
    # Check for scheduling conflicts
    existing_appointment = db.query(Appointment).filter(
        and_(
            Appointment.doctor_id == appointment_data.doctor_id,
            Appointment.appointment_date == appointment_data.appointment_date,
            Appointment.status.in_(["scheduled", "confirmed", "in_progress"])
        )
    ).first()
    
    if existing_appointment:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Doctor already has an appointment at this time"
        )
    
    # Create new appointment
    appointment = Appointment(**appointment_data.dict())
    
    db.add(appointment)
    db.commit()
    db.refresh(appointment)
    
    return AppointmentResponse.from_orm(appointment)

@router.put("/{appointment_id}", response_model=AppointmentResponse)
async def update_appointment(
    appointment_id: int,
    appointment_update: AppointmentUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update an existing appointment"""
    appointment = db.query(Appointment).filter(Appointment.id == appointment_id).first()
    
    if not appointment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Appointment not found"
        )
    
    # Check for scheduling conflicts if date is being updated
    if appointment_update.appointment_date and appointment_update.appointment_date != appointment.appointment_date:
        existing_appointment = db.query(Appointment).filter(
            and_(
                Appointment.doctor_id == appointment.doctor_id,
                Appointment.appointment_date == appointment_update.appointment_date,
                Appointment.status.in_(["scheduled", "confirmed", "in_progress"]),
                Appointment.id != appointment_id
            )
        ).first()
        
        if existing_appointment:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Doctor already has an appointment at this time"
            )
    
    # Update appointment fields
    update_data = appointment_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(appointment, field, value)
    
    db.commit()
    db.refresh(appointment)
    
    return AppointmentResponse.from_orm(appointment)

@router.delete("/{appointment_id}")
async def cancel_appointment(
    appointment_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Cancel an appointment"""
    appointment = db.query(Appointment).filter(Appointment.id == appointment_id).first()
    
    if not appointment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Appointment not found"
        )
    
    # Set status to cancelled
    appointment.status = "cancelled"
    db.commit()
    
    return {"message": "Appointment cancelled successfully"}

@router.get("/doctor/{doctor_id}/schedule")
async def get_doctor_schedule(
    doctor_id: int,
    date: Optional[date] = Query(None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get doctor's schedule for a specific date"""
    # Verify doctor exists
    doctor = db.query(User).filter(User.id == doctor_id).first()
    if not doctor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Doctor not found"
        )
    
    query = db.query(Appointment).filter(Appointment.doctor_id == doctor_id)
    
    if date:
        # Filter by date
        start_of_day = datetime.combine(date, datetime.min.time())
        end_of_day = datetime.combine(date, datetime.max.time())
        query = query.filter(
            and_(
                Appointment.appointment_date >= start_of_day,
                Appointment.appointment_date <= end_of_day
            )
        )
    
    appointments = query.all()
    
    return [AppointmentResponse.from_orm(appointment) for appointment in appointments]

@router.get("/patient/{patient_id}/history")
async def get_patient_appointment_history(
    patient_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get patient's appointment history"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == patient_id).first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient not found"
        )
    
    appointments = db.query(Appointment).filter(Appointment.patient_id == patient_id).all()
    
    return [AppointmentResponse.from_orm(appointment) for appointment in appointments]

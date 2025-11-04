from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, date
from app.core.database import get_db
from app.models import Bill, Patient, User
from app.schemas import BillResponse, BillCreate, BillUpdate, BillPayment
from app.api.auth import get_current_user_dependency

router = APIRouter()

@router.get("/", response_model=List[BillResponse])
@router.get("", response_model=List[BillResponse])
async def get_bills(
    skip: int = Query(0, ge=0, description="Number of records to skip"),
    limit: int = Query(100, ge=1, le=1000, description="Number of records to return"),
    status: Optional[str] = Query(None, description="Filter by bill status"),
    patient_id: Optional[int] = Query(None, description="Filter by patient ID"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get all bills with optional filtering"""
    query = db.query(Bill)
    
    # Filter by status if provided
    if status:
        query = query.filter(Bill.status == status)
    
    # Filter by patient if provided
    if patient_id:
        query = query.filter(Bill.patient_id == patient_id)
    
    bills = query.offset(skip).limit(limit).all()
    return [BillResponse.from_orm(bill) for bill in bills]

@router.get("/{bill_id}", response_model=BillResponse)
async def get_bill(
    bill_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Get a specific bill by ID"""
    bill = db.query(Bill).filter(Bill.id == bill_id).first()
    if not bill:
        raise HTTPException(status_code=404, detail="Bill not found")
    return BillResponse.from_orm(bill)

@router.post("/", response_model=BillResponse)
@router.post("", response_model=BillResponse)
async def create_bill(
    bill_data: BillCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Create a new bill"""
    # Verify patient exists
    patient = db.query(Patient).filter(Patient.id == bill_data.patient_id).first()
    if not patient:
        raise HTTPException(status_code=404, detail="Patient not found")
    
    # Generate bill number
    bill_count = db.query(Bill).count()
    bill_number = f"BILL-{datetime.now().year}-{bill_count + 1:06d}"
    
    # Create new bill
    db_bill = Bill(
        patient_id=bill_data.patient_id,
        bill_number=bill_number,
        total_amount=bill_data.total_amount,
        paid_amount=0,
        status="pending",
        due_date=bill_data.due_date,
        description=bill_data.description,
        items=bill_data.items,
        created_at=datetime.utcnow(),
        updated_at=datetime.utcnow()
    )
    
    db.add(db_bill)
    db.commit()
    db.refresh(db_bill)
    
    return BillResponse.from_orm(db_bill)

@router.put("/{bill_id}", response_model=BillResponse)
async def update_bill(
    bill_id: int,
    bill_data: BillUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Update a bill"""
    bill = db.query(Bill).filter(Bill.id == bill_id).first()
    if not bill:
        raise HTTPException(status_code=404, detail="Bill not found")
    
    # Update fields
    update_data = bill_data.dict(exclude_unset=True)
    for field, value in update_data.items():
        if hasattr(bill, field):
            setattr(bill, field, value)
    
    bill.updated_at = datetime.utcnow()
    
    db.commit()
    db.refresh(bill)
    
    return BillResponse.from_orm(bill)

@router.post("/{bill_id}/payment")
async def record_payment(
    bill_id: int,
    payment_data: BillPayment,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Record a payment for a bill"""
    bill = db.query(Bill).filter(Bill.id == bill_id).first()
    if not bill:
        raise HTTPException(status_code=404, detail="Bill not found")
    
    # Update paid amount
    bill.paid_amount += payment_data.payment_amount
    
    # Update status based on payment
    if bill.paid_amount >= bill.total_amount:
        bill.status = "paid"
    elif bill.paid_amount > 0:
        bill.status = "partially_paid"
    
    bill.updated_at = datetime.utcnow()
    
    db.commit()
    db.refresh(bill)
    
    return {"message": "Payment recorded successfully", "bill": BillResponse.from_orm(bill)}

@router.delete("/{bill_id}")
async def delete_bill(
    bill_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_dependency)
):
    """Delete a bill"""
    bill = db.query(Bill).filter(Bill.id == bill_id).first()
    if not bill:
        raise HTTPException(status_code=404, detail="Bill not found")
    
    db.delete(bill)
    db.commit()
    
    return {"message": "Bill deleted successfully"}

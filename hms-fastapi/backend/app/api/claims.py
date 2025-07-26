from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models import Claim, ClaimStatus
from app.models import Patient
from app.schemas import ClaimCreate, ClaimUpdate, ClaimOut
from datetime import datetime

router = APIRouter()

@router.post("/", response_model=ClaimOut, status_code=status.HTTP_201_CREATED)
@router.post("", response_model=ClaimOut, status_code=status.HTTP_201_CREATED)
def create_claim(claim: ClaimCreate, db: Session = Depends(get_db)):
    """Create a new claim"""
    patient = db.query(Patient).filter(Patient.id == claim.patient_id).first()
    if not patient:
        raise HTTPException(status_code=404, detail="Patient not found")
    db_claim = Claim(**claim.dict())
    db.add(db_claim)
    db.commit()
    db.refresh(db_claim)
    return db_claim



@router.get("/", response_model=List[ClaimOut])
@router.get("", response_model=List[ClaimOut])
def list_claims(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """List all claims with pagination"""
    return db.query(Claim).offset(skip).limit(limit).all()

@router.get("/{claim_id}", response_model=ClaimOut)
@router.get("/{claim_id}", response_model=ClaimOut)
def get_claim(
    claim_id: int,
    db: Session = Depends(get_db)
):
    claim = db.query(Claim).filter(Claim.id == claim_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="Claim not found")
    return claim

@router.patch("/{claim_id}", response_model=ClaimOut)
@router.patch("/{claim_id}", response_model=ClaimOut)
def update_claim(
    claim_id: int,
    update: ClaimUpdate,
    db: Session = Depends(get_db)
):
    claim = db.query(Claim).filter(Claim.id == claim_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="Claim not found")
    for key, value in update.dict(exclude_unset=True).items():
        setattr(claim, key, value)
    if update.status:
        if update.status == ClaimStatus.processing:
            claim.processed_at = datetime.utcnow()
    db.commit()
    db.refresh(claim)
    return claim

@router.delete("/{claim_id}", status_code=status.HTTP_204_NO_CONTENT)
@router.delete("/{claim_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_claim(
    claim_id: int,
    db: Session = Depends(get_db)
):
    claim = db.query(Claim).filter(Claim.id == claim_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="Claim not found")
    db.delete(claim)
    db.commit()
    return None

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models.claim import Claim
from typing import List

router = APIRouter()

@router.get("/", response_model=List[str])
@router.get("", response_model=List[str])
def get_schemes(db: Session = Depends(get_db)):
    # For now, get unique schemes from claims table (or hardcode if empty)
    schemes = db.query(Claim.scheme).distinct().all()
    scheme_list = [s[0] for s in schemes if s[0]]
    # Fallback to hardcoded if none found
    if not scheme_list:
        scheme_list = ["NHIF", "Private Insurance"]
    return scheme_list

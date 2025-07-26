from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models import Scheme
from app.schemas import SchemeCreate, SchemeUpdate, SchemeOut

router = APIRouter()

@router.get("/", response_model=List[SchemeOut])
def list_schemes(db: Session = Depends(get_db)):
    return db.query(Scheme).all()

@router.post("/", response_model=SchemeOut, status_code=status.HTTP_201_CREATED)
def create_scheme(scheme: SchemeCreate, db: Session = Depends(get_db)):
    if db.query(Scheme).filter(Scheme.name == scheme.name).first():
        raise HTTPException(status_code=400, detail="Scheme with this name already exists")
    db_scheme = Scheme(**scheme.dict())
    db.add(db_scheme)
    db.commit()
    db.refresh(db_scheme)
    return db_scheme

@router.get("/{scheme_id}", response_model=SchemeOut)
def get_scheme(scheme_id: int, db: Session = Depends(get_db)):
    scheme = db.query(Scheme).filter(Scheme.id == scheme_id).first()
    if not scheme:
        raise HTTPException(status_code=404, detail="Scheme not found")
    return scheme

@router.put("/{scheme_id}", response_model=SchemeOut)
def update_scheme(scheme_id: int, update: SchemeUpdate, db: Session = Depends(get_db)):
    scheme = db.query(Scheme).filter(Scheme.id == scheme_id).first()
    if not scheme:
        raise HTTPException(status_code=404, detail="Scheme not found")
    for key, value in update.dict(exclude_unset=True).items():
        setattr(scheme, key, value)
    db.commit()
    db.refresh(scheme)
    return scheme

@router.delete("/{scheme_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_scheme(scheme_id: int, db: Session = Depends(get_db)):
    scheme = db.query(Scheme).filter(Scheme.id == scheme_id).first()
    if not scheme:
        raise HTTPException(status_code=404, detail="Scheme not found")
    db.delete(scheme)
    db.commit()
    return None

from sqlalchemy.orm import Session
from app.models.schemes import Scheme
from app.schemas.schemes import SchemeCreate, SchemeUpdate
from typing import List, Optional

def get_scheme(db: Session, scheme_id: int) -> Optional[Scheme]:
    return db.query(Scheme).filter(Scheme.id == scheme_id).first()

def get_scheme_by_code(db: Session, code: str) -> Optional[Scheme]:
    return db.query(Scheme).filter(Scheme.code == code).first()

def get_schemes(db: Session, skip: int = 0, limit: int = 100) -> List[Scheme]:
    return db.query(Scheme).offset(skip).limit(limit).all()

def create_scheme(db: Session, scheme: SchemeCreate) -> Scheme:
    db_scheme = Scheme(**scheme.dict())
    db.add(db_scheme)
    db.commit()
    db.refresh(db_scheme)
    return db_scheme

def update_scheme(db: Session, db_scheme: Scheme, update: SchemeUpdate) -> Scheme:
    for key, value in update.dict(exclude_unset=True).items():
        setattr(db_scheme, key, value)
    db.commit()
    db.refresh(db_scheme)
    return db_scheme

def delete_scheme(db: Session, db_scheme: Scheme):
    db.delete(db_scheme)
    db.commit()

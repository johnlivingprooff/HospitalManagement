from pydantic import BaseModel, Field
from typing import Optional, Literal
from datetime import datetime

class ClaimBase(BaseModel):
    patient_id: int
    scheme: str
    amount: float
    description: Optional[str] = None

class ClaimCreate(ClaimBase):
    pass

class ClaimUpdate(BaseModel):
    status: Optional[Literal['pending', 'approved', 'rejected', 'processing', 'paid']] = None
    processed_at: Optional[datetime] = None
    outcome: Optional[str] = None
    description: Optional[str] = None

class ClaimOut(ClaimBase):
    id: int
    status: str
    submitted_at: datetime
    processed_at: Optional[datetime]
    outcome: Optional[str]

    class Config:
        orm_mode = True

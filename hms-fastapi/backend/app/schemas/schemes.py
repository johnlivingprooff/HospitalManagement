from typing import Optional, Any, Dict
from datetime import datetime
from pydantic import BaseModel, Field

class SchemeBase(BaseModel):
    name: str = Field(..., max_length=128)
    type: str = Field(..., max_length=64)
    description: Optional[str] = None
    code: Optional[str] = Field(None, max_length=64)
    active: Optional[bool] = True
    coverage_details: Optional[Dict[str, Any]] = None
    contact_info: Optional[Dict[str, Any]] = None

class SchemeCreate(SchemeBase):
    pass

class SchemeUpdate(SchemeBase):
    pass

class SchemeOut(SchemeBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True

from typing import Optional, List
from pydantic import BaseModel, EmailStr, Field
from datetime import datetime, date
from typing import Literal
from typing import Optional, Any, Dict


# Base schemas
class BaseSchema(BaseModel):
    class Config:
        from_attributes = True

# --- Scheme Schemas ---
class SchemeBase(BaseSchema):
    name: str = Field(..., max_length=128)
    type: str = Field(..., max_length=64)
    description: Optional[str] = None
    code: Optional[str] = Field(None, max_length=64)
    is_active: Optional[bool] = True
    coverage_details: Optional[Dict[str, Any]] = None
    contact_info: Optional[Dict[str, Any]] = None

class SchemeCreate(SchemeBase):
    pass

class SchemeUpdate(BaseModel):
    name: Optional[str] = Field(None, max_length=128)
    type: Optional[str] = Field(None, max_length=64)
    description: Optional[str] = None
    code: Optional[str] = Field(None, max_length=64)
    is_active: Optional[bool] = None
    coverage_details: Optional[Dict[str, Any]] = None
    contact_info: Optional[Dict[str, Any]] = None


class SchemeOut(SchemeBase):
    id: int
    is_active: bool
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True

# --- Claim Schemas ---
class ClaimBase(BaseSchema):
    patient_id: int
    scheme_id: int
    amount_claimed: float
    description: Optional[str] = None

class ClaimCreate(ClaimBase):
    pass

class ClaimUpdate(BaseSchema):
    status: Optional[Literal['pending', 'approved', 'rejected', 'processing', 'paid']] = None
    processed_at: Optional[datetime] = None
    outcome: Optional[str] = None
    description: Optional[str] = None

class ClaimOut(ClaimBase):
    id: int
    status: str
    created_at: datetime
    updated_at: datetime
    processed_at: Optional[datetime] = None
    outcome: Optional[str] = None

    class Config:
        orm_mode = True

# User schemas
class UserBase(BaseSchema):
    email: EmailStr
    first_name: str
    last_name: str
    role: str = "user"

class UserCreate(UserBase):
    password: str = Field(..., min_length=6)

class UserLogin(BaseSchema):
    email: EmailStr
    password: str

class UserUpdate(BaseSchema):
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    role: Optional[str] = None
    is_active: Optional[bool] = None

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime
    updated_at: datetime

# Patient schemas
class PatientBase(BaseSchema):
    first_name: str
    last_name: str
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    date_of_birth: Optional[date] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    emergency_contact: Optional[str] = None
    emergency_phone: Optional[str] = None
    medical_history: Optional[str] = None
    allergies: Optional[str] = None
    current_medications: Optional[str] = None
    insurance_info: Optional[str] = None

class PatientCreate(PatientBase):
    pass

class PatientUpdate(BaseSchema):
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    date_of_birth: Optional[date] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    emergency_contact: Optional[str] = None
    emergency_phone: Optional[str] = None
    medical_history: Optional[str] = None
    allergies: Optional[str] = None
    current_medications: Optional[str] = None
    insurance_info: Optional[str] = None
    is_active: Optional[bool] = None

class PatientResponse(PatientBase):
    id: int
    is_active: bool
    created_by_id: Optional[int]
    created_at: datetime
    updated_at: datetime

# Appointment schemas
class AppointmentBase(BaseSchema):
    patient_id: int
    doctor_id: int
    appointment_date: datetime
    duration_minutes: int = 30
    appointment_type: str
    notes: Optional[str] = None
    symptoms: Optional[str] = None

class AppointmentCreate(AppointmentBase):
    pass

class AppointmentUpdate(BaseSchema):
    appointment_date: Optional[datetime] = None
    duration_minutes: Optional[int] = None
    status: Optional[str] = None
    appointment_type: Optional[str] = None
    notes: Optional[str] = None
    symptoms: Optional[str] = None
    diagnosis: Optional[str] = None
    treatment_plan: Optional[str] = None

class AppointmentResponse(AppointmentBase):
    id: int
    status: str
    diagnosis: Optional[str]
    treatment_plan: Optional[str]
    created_at: datetime
    updated_at: datetime
    # Include related patient and doctor information
    patient: Optional['PatientResponse'] = None
    doctor: Optional['UserResponse'] = None

# Medical Record schemas
class MedicalRecordBase(BaseSchema):
    patient_id: int
    doctor_id: int
    appointment_id: Optional[int] = None
    record_type: str
    title: str
    description: Optional[str] = None
    diagnosis: Optional[str] = None
    treatment: Optional[str] = None
    medications: Optional[str] = None
    lab_results: Optional[str] = None
    file_attachments: Optional[str] = None

class MedicalRecordCreate(MedicalRecordBase):
    pass

class MedicalRecordUpdate(BaseSchema):
    record_type: Optional[str] = None
    title: Optional[str] = None
    description: Optional[str] = None
    diagnosis: Optional[str] = None
    treatment: Optional[str] = None
    medications: Optional[str] = None
    lab_results: Optional[str] = None
    file_attachments: Optional[str] = None

# Nested schemas for medical record responses
class PatientInfo(BaseSchema):
    first_name: str
    last_name: str

class DoctorInfo(BaseSchema):
    first_name: str
    last_name: str

class MedicalRecordResponse(MedicalRecordBase):
    id: int
    created_at: datetime
    updated_at: datetime
    patient: Optional[PatientInfo] = None
    doctor: Optional[DoctorInfo] = None

# Department schemas
class DepartmentBase(BaseSchema):
    name: str
    description: Optional[str] = None
    head_doctor_id: Optional[int] = None

class DepartmentCreate(DepartmentBase):
    pass

class DepartmentUpdate(BaseSchema):
    name: Optional[str] = None
    description: Optional[str] = None
    head_doctor_id: Optional[int] = None
    is_active: Optional[bool] = None

class DepartmentResponse(DepartmentBase):
    id: int
    is_active: bool
    created_at: datetime

# Bed schemas
class BedBase(BaseSchema):
    bed_number: str
    room_number: str
    department_id: Optional[int] = None
    bed_type: str
    daily_rate: int = 0

class BedCreate(BedBase):
    pass

class BedUpdate(BaseSchema):
    bed_number: Optional[str] = None
    room_number: Optional[str] = None
    department_id: Optional[int] = None
    bed_type: Optional[str] = None
    is_occupied: Optional[bool] = None
    current_patient_id: Optional[int] = None
    daily_rate: Optional[int] = None

class BedResponse(BedBase):
    id: int
    is_occupied: bool
    current_patient_id: Optional[int]
    created_at: datetime

# Bill schemas
class BillBase(BaseSchema):
    patient_id: int
    bill_number: str
    total_amount: int
    description: Optional[str] = None
    items: Optional[str] = None
    due_date: Optional[date] = None

class BillCreate(BillBase):
    pass

class BillUpdate(BaseSchema):
    total_amount: Optional[int] = None
    paid_amount: Optional[int] = None
    status: Optional[str] = None
    due_date: Optional[date] = None
    description: Optional[str] = None
    items: Optional[str] = None

class BillResponse(BillBase):
    id: int
    paid_amount: int
    status: str
    created_at: datetime
    updated_at: datetime

class BillPayment(BaseSchema):
    """Schema for recording a payment on a bill"""
    payment_amount: int = Field(..., gt=0, description="Payment amount in cents")

# Authentication schemas
class Token(BaseSchema):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse

class TokenData(BaseSchema):
    email: Optional[str] = None

# Lab Test schemas
class LabTestBase(BaseSchema):
    patient_id: int
    doctor_id: int
    test_name: str
    test_type: str
    normal_range: Optional[str] = None
    notes: Optional[str] = None

class LabTestCreate(LabTestBase):
    pass

class LabTestUpdate(BaseSchema):
    test_name: Optional[str] = None
    test_type: Optional[str] = None
    status: Optional[str] = None
    result: Optional[str] = None
    normal_range: Optional[str] = None
    notes: Optional[str] = None
    completed_date: Optional[datetime] = None

class LabTestResponse(LabTestBase):
    id: int
    status: str
    result: Optional[str]
    ordered_date: datetime
    completed_date: Optional[datetime]
    created_at: datetime
    updated_at: datetime
    patient: Optional[PatientInfo] = None
    doctor: Optional[DoctorInfo] = None

# Prescription schemas
class PrescriptionBase(BaseSchema):
    patient_id: int
    doctor_id: int
    medication_name: str
    dosage: str
    frequency: str
    duration: str
    quantity: int
    instructions: Optional[str] = None

class PrescriptionCreate(PrescriptionBase):
    pass

class PrescriptionUpdate(BaseSchema):
    medication_name: Optional[str] = None
    dosage: Optional[str] = None
    frequency: Optional[str] = None
    duration: Optional[str] = None
    quantity: Optional[int] = None
    status: Optional[str] = None
    instructions: Optional[str] = None

class PrescriptionResponse(PrescriptionBase):
    id: int
    status: str
    created_at: datetime
    updated_at: datetime
    patient: Optional[PatientInfo] = None
    doctor: Optional[DoctorInfo] = None

# Ward schemas
class WardBase(BaseSchema):
    name: str
    type: str = Field(..., pattern="^(general|icu|emergency|surgery|maternity|pediatric)$")
    capacity: int = Field(..., gt=0)
    floor: int = Field(..., gt=0)
    description: Optional[str] = None

class WardCreate(WardBase):
    pass

class WardUpdate(BaseSchema):
    name: Optional[str] = None
    type: Optional[str] = Field(None, pattern="^(general|icu|emergency|surgery|maternity|pediatric)$")
    capacity: Optional[int] = Field(None, gt=0)
    floor: Optional[int] = Field(None, gt=0)
    description: Optional[str] = None
    is_active: Optional[bool] = None

class WardPatientInfo(BaseSchema):
    id: int
    bed_number: str
    admission_date: datetime
    discharge_date: Optional[datetime]
    status: str
    notes: Optional[str]
    patient: PatientInfo
    doctor: Optional[DoctorInfo] = None

class WardResponse(WardBase):
    id: int
    current_occupancy: int
    is_active: bool
    created_at: datetime
    updated_at: datetime
    patients: Optional[List[WardPatientInfo]] = []

# Ward Patient schemas
class WardPatientBase(BaseSchema):
    patient_id: int
    doctor_id: Optional[int] = None
    bed_number: str
    status: str = Field(default="admitted", pattern="^(admitted|stable|critical|recovering|discharged)$")
    notes: Optional[str] = None

class WardPatientCreate(WardPatientBase):
    pass

class WardPatientUpdate(BaseSchema):
    doctor_id: Optional[int] = None
    bed_number: Optional[str] = None
    status: Optional[str] = Field(None, pattern="^(admitted|stable|critical|recovering|discharged)$")
    notes: Optional[str] = None
    discharge_date: Optional[datetime] = None

class DischargePatient(BaseSchema):
    """Schema for discharging a patient from a ward"""
    discharge_date: Optional[datetime] = Field(None, description="Discharge date, defaults to now if not provided")
    notes: Optional[str] = Field(None, description="Discharge notes")

class WardPatientResponse(WardPatientBase):
    id: int
    ward_id: int
    admission_date: datetime
    discharge_date: Optional[datetime]
    created_at: datetime
    updated_at: datetime
    patient: PatientInfo
    doctor: Optional[DoctorInfo] = None

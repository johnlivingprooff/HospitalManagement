from sqlalchemy import Column, Integer, String, Boolean, DateTime, Text, ForeignKey, Date
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base
from enum import Enum
from sqlalchemy.dialects.postgresql import JSONB


# Claim status enum
class ClaimStatus(str, Enum):
    pending = "pending"
    processing = "processing"
    approved = "approved"
    rejected = "rejected"
    paid = "paid"

class Claim(Base):
    __tablename__ = "claims"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    scheme_id = Column(Integer, ForeignKey("schemes.id"), nullable=False)
    claim_number = Column(String, unique=True, nullable=True)
    amount_claimed = Column(Integer, nullable=False)  # in cents
    amount_approved = Column(Integer)  # in cents
    status = Column(String, default=ClaimStatus.pending.value)
    description = Column(Text)
    date_of_service = Column(Date)
    outcome = Column(Text)
    processed_at = Column(DateTime)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship(
        "Patient",
        back_populates="claims",
        foreign_keys=[patient_id]
    )
    scheme = relationship(
        "Scheme",
        back_populates="claims",
        foreign_keys=[scheme_id]
    )

class Scheme(Base):
    __tablename__ = "schemes"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(128), nullable=False, unique=True)
    type = Column(String(64), nullable=False)
    description = Column(Text, nullable=True)
    code = Column(String(64), nullable=True, unique=True)
    is_active = Column(Boolean, default=True, nullable=False)
    coverage_details = Column(JSONB, nullable=True)
    contact_info = Column(JSONB, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    
    # Relationships
    claims = relationship("Claim", back_populates="scheme")

class Patient(Base):
    __tablename__ = "patients"
    
    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String, nullable=False)
    last_name = Column(String, nullable=False)
    email = Column(String, unique=True, index=True)
    phone = Column(String)
    date_of_birth = Column(Date)
    gender = Column(String)
    address = Column(Text)
    emergency_contact = Column(String)
    emergency_phone = Column(String)
    medical_history = Column(Text)
    allergies = Column(Text)
    current_medications = Column(Text)
    insurance_info = Column(Text)
    scheme_id = Column(Integer, ForeignKey("schemes.id"), nullable=True)
    is_active = Column(Boolean, default=True)
    created_by_id = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    created_by = relationship(
        "User",
        back_populates="created_patients",
        foreign_keys=[created_by_id]
    )
    appointments = relationship("Appointment", back_populates="patient")
    medical_records = relationship("MedicalRecord", back_populates="patient")
    claims = relationship("Claim", back_populates="patient")
    ward_patients = relationship("WardPatient", back_populates="patient")
    scheme = relationship("Scheme", foreign_keys=[scheme_id])



class Appointment(Base):
    __tablename__ = "appointments"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    doctor_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    appointment_date = Column(DateTime, nullable=False)
    duration_minutes = Column(Integer, default=30)
    status = Column(String, default="scheduled")  # scheduled, confirmed, in_progress, completed, cancelled
    appointment_type = Column(String, nullable=False)  # consultation, follow_up, emergency, etc.
    notes = Column(Text)
    symptoms = Column(Text)
    diagnosis = Column(Text)
    treatment_plan = Column(Text)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship("Patient", back_populates="appointments")
    doctor = relationship("User", back_populates="appointments_as_doctor", foreign_keys=[doctor_id])

class MedicalRecord(Base):
    __tablename__ = "medical_records"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    doctor_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    appointment_id = Column(Integer, ForeignKey("appointments.id"))
    record_type = Column(String, nullable=False)  # consultation, lab_result, prescription, etc.
    title = Column(String, nullable=False)
    description = Column(Text)
    diagnosis = Column(Text)
    treatment = Column(Text)
    medications = Column(Text)
    lab_results = Column(Text)
    file_attachments = Column(Text)  # JSON array of file paths
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship("Patient", back_populates="medical_records")
    doctor = relationship("User", foreign_keys=[doctor_id])
    appointment = relationship("Appointment", foreign_keys=[appointment_id])

class Department(Base):
    __tablename__ = "departments"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(Text)
    head_doctor_id = Column(Integer, ForeignKey("users.id"))
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, server_default=func.now())
    
    # Relationships
    head_doctor = relationship("User", foreign_keys=[head_doctor_id])

class Bed(Base):
    __tablename__ = "beds"
    
    id = Column(Integer, primary_key=True, index=True)
    bed_number = Column(String, nullable=False)
    room_number = Column(String, nullable=False)
    department_id = Column(Integer, ForeignKey("departments.id"))
    bed_type = Column(String, nullable=False)  # general, icu, private, etc.
    is_occupied = Column(Boolean, default=False)
    current_patient_id = Column(Integer, ForeignKey("patients.id"))
    daily_rate = Column(Integer, default=0)  # in cents
    created_at = Column(DateTime, server_default=func.now())
    
    # Relationships
    department = relationship("Department", foreign_keys=[department_id])
    current_patient = relationship("Patient", foreign_keys=[current_patient_id])

class Bill(Base):
    __tablename__ = "bills"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    bill_number = Column(String, unique=True, nullable=False)
    total_amount = Column(Integer, nullable=False)  # in cents
    paid_amount = Column(Integer, default=0)  # in cents
    status = Column(String, default="pending")  # pending, paid, partially_paid, overdue
    due_date = Column(Date)
    description = Column(Text)
    items = Column(Text)  # JSON array of bill items
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship("Patient", foreign_keys=[patient_id])

class LabTest(Base):
    __tablename__ = "lab_tests"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    doctor_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    test_name = Column(String, nullable=False)
    test_type = Column(String, nullable=False)
    status = Column(String, default="pending")  # pending, in_progress, completed, cancelled
    result = Column(Text)
    normal_range = Column(String)
    notes = Column(Text)
    ordered_date = Column(DateTime, server_default=func.now())
    completed_date = Column(DateTime)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship("Patient", foreign_keys=[patient_id])
    doctor = relationship("User", foreign_keys=[doctor_id])

class Prescription(Base):
    __tablename__ = "prescriptions"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    doctor_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    medication_name = Column(String, nullable=False)
    dosage = Column(String, nullable=False)
    frequency = Column(String, nullable=False)
    duration = Column(String, nullable=False)
    quantity = Column(Integer, nullable=False)
    status = Column(String, default="pending")  # pending, dispensed, cancelled
    instructions = Column(Text)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship("Patient", foreign_keys=[patient_id])
    doctor = relationship("User", foreign_keys=[doctor_id])

class Ward(Base):
    __tablename__ = "wards"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False, unique=True)
    type = Column(String, nullable=False)  # general, icu, emergency, surgery, maternity, pediatric
    capacity = Column(Integer, nullable=False)
    current_occupancy = Column(Integer, default=0)
    floor = Column(Integer, nullable=False)
    description = Column(Text)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patients = relationship("WardPatient", back_populates="ward")

class WardPatient(Base):
    __tablename__ = "ward_patients"
    
    id = Column(Integer, primary_key=True, index=True)
    patient_id = Column(Integer, ForeignKey("patients.id"), nullable=False)
    ward_id = Column(Integer, ForeignKey("wards.id"), nullable=False)
    doctor_id = Column(Integer, ForeignKey("users.id"))
    bed_number = Column(String, nullable=False)
    admission_date = Column(DateTime, server_default=func.now())
    discharge_date = Column(DateTime)
    status = Column(String, default="admitted")  # admitted, stable, critical, recovering, discharged
    notes = Column(Text)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    patient = relationship("Patient", foreign_keys=[patient_id])
    ward = relationship("Ward", back_populates="patients", foreign_keys=[ward_id])
    doctor = relationship("User", foreign_keys=[doctor_id])

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    password = Column(String, nullable=False)
    first_name = Column(String, nullable=False)
    last_name = Column(String, nullable=False)
    role = Column(String, nullable=False, default="user")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    
    # Relationships
    appointments_as_doctor = relationship(
        "Appointment",
        back_populates="doctor",
        foreign_keys=[Appointment.doctor_id]
    )
    created_patients = relationship(
        "Patient",
        back_populates="created_by",
        foreign_keys=[Patient.created_by_id]
    )
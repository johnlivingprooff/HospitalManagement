import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from datetime import datetime, timedelta, date
from sqlalchemy.orm import Session
from app.core.database import get_db, create_tables
from app.models import User, Patient, Appointment, MedicalRecord, Bill, LabTest, Prescription
from app.core.security import get_password_hash

def seed_data():
    """Seed the database with sample data"""
    
    # Create tables first
    create_tables()
    
    db = next(get_db())
    
    try:
        # Check if data already exists
        if db.query(User).count() > 1:  # More than just admin user
            print("Data already exists. Skipping seeding.")
            return
        
        # Create sample doctors
        doctors = []
        doctor_data = [
            {"email": "dr.smith@hospital.com", "first_name": "John", "last_name": "Smith", "role": "doctor"},
            {"email": "dr.johnson@hospital.com", "first_name": "Sarah", "last_name": "Johnson", "role": "doctor"},
            {"email": "dr.brown@hospital.com", "first_name": "Michael", "last_name": "Brown", "role": "doctor"},
        ]
        
        for doc_data in doctor_data:
            doctor = User(
                email=doc_data["email"],
                password=get_password_hash("password123"),
                first_name=doc_data["first_name"],
                last_name=doc_data["last_name"],
                role=doc_data["role"],
                is_active=True
            )
            db.add(doctor)
            doctors.append(doctor)
        
        db.commit()
        
        # Create sample patients
        patients = []
        patient_data = [
            {"first_name": "Alice", "last_name": "Wilson", "email": "alice.wilson@email.com", "phone": "555-0101"},
            {"first_name": "Bob", "last_name": "Davis", "email": "bob.davis@email.com", "phone": "555-0102"},
            {"first_name": "Carol", "last_name": "Miller", "email": "carol.miller@email.com", "phone": "555-0103"},
            {"first_name": "David", "last_name": "Garcia", "email": "david.garcia@email.com", "phone": "555-0104"},
            {"first_name": "Emma", "last_name": "Rodriguez", "email": "emma.rodriguez@email.com", "phone": "555-0105"},
            {"first_name": "Frank", "last_name": "Martinez", "email": "frank.martinez@email.com", "phone": "555-0106"},
            {"first_name": "Grace", "last_name": "Anderson", "email": "grace.anderson@email.com", "phone": "555-0107"},
            {"first_name": "Henry", "last_name": "Taylor", "email": "henry.taylor@email.com", "phone": "555-0108"},
        ]
        
        for patient_data in patient_data:
            patient = Patient(
                first_name=patient_data["first_name"],
                last_name=patient_data["last_name"],
                email=patient_data["email"],
                phone=patient_data["phone"],
                date_of_birth=date(1980, 1, 1),
                gender="M" if patient_data["first_name"] in ["Bob", "David", "Frank", "Henry"] else "F",
                address="123 Main St, City, State 12345",
                created_by_id=1,  # Admin user
                is_active=True
            )
            db.add(patient)
            patients.append(patient)
        
        db.commit()
        
        # Create appointments
        appointments = []
        for i in range(20):
            appointment = Appointment(
                patient_id=patients[i % len(patients)].id,
                doctor_id=doctors[i % len(doctors)].id,
                appointment_date=datetime.now() + timedelta(days=(i % 30) - 15),
                duration_minutes=30,
                appointment_type=["consultation", "follow_up", "emergency"][i % 3],
                status=["scheduled", "completed", "cancelled"][i % 3],
                notes=f"Sample appointment {i+1}"
            )
            db.add(appointment)
            appointments.append(appointment)
        
        db.commit()
        
        # Create medical records
        for i in range(15):
            record = MedicalRecord(
                patient_id=patients[i % len(patients)].id,
                doctor_id=doctors[i % len(doctors)].id,
                record_type=["consultation", "lab_result", "prescription", "diagnosis"][i % 4],
                title=f"Medical Record {i+1}",
                description=f"Sample medical record description {i+1}",
                diagnosis="Sample diagnosis",
                treatment="Sample treatment plan"
            )
            db.add(record)
        
        # Create lab tests
        for i in range(12):
            lab_test = LabTest(
                patient_id=patients[i % len(patients)].id,
                doctor_id=doctors[i % len(doctors)].id,
                test_name=["Blood Test", "Urine Test", "X-Ray", "MRI"][i % 4],
                test_type=["blood_test", "urine_test", "x_ray", "mri"][i % 4],
                status=["pending", "in_progress", "completed"][i % 3],
                result="Normal" if i % 2 == 0 else None,
                normal_range="Normal range info"
            )
            db.add(lab_test)
        
        # Create prescriptions
        for i in range(10):
            prescription = Prescription(
                patient_id=patients[i % len(patients)].id,
                doctor_id=doctors[i % len(doctors)].id,
                medication_name=["Aspirin", "Ibuprofen", "Amoxicillin", "Metformin"][i % 4],
                dosage="500mg",
                frequency="Twice daily",
                duration="7 days",
                quantity=14,
                status=["pending", "dispensed"][i % 2],
                instructions="Take with food"
            )
            db.add(prescription)
        
        # Create bills
        for i in range(8):
            bill = Bill(
                patient_id=patients[i % len(patients)].id,
                bill_number=f"BILL-{2025}{str(i+1).zfill(4)}",
                total_amount=(100 + i * 50) * 100,  # Convert to cents
                paid_amount=(50 + i * 25) * 100 if i % 2 == 0 else 0,
                status="paid" if i % 2 == 0 else "pending",
                due_date=date.today() + timedelta(days=30),
                description=f"Medical services bill {i+1}"
            )
            db.add(bill)
        
        db.commit()
        print("Sample data seeded successfully!")
        
    except Exception as e:
        db.rollback()
        print(f"Error seeding data: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    seed_data()

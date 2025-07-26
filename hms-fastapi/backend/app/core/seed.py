"""
Database seeding utilities
"""
from sqlalchemy.orm import Session
from sqlalchemy.exc import OperationalError, IntegrityError
from sqlalchemy import text, create_engine
from app.core.database import get_db
from app.models import User, Patient, Claim, ClaimStatus, Scheme
from app.core.security import get_password_hash
import asyncio
import time
import datetime

async def wait_for_database(max_retries: int = 30, retry_interval: int = 2):
    """
    Wait for database to become available
    """
    for attempt in range(max_retries):
        try:
            print(f"🔄 Attempting database connection (attempt {attempt + 1}/{max_retries})...")
            db_gen = get_db()
            db: Session = next(db_gen)
            
            # Test the connection - Fixed: Use text() wrapper for SQLAlchemy 2.0+
            db.execute(text("SELECT 1"))
            db.close()
            
            print("✅ Database connection successful!")
            return True
            
        except OperationalError as e:
            if "could not translate host name" in str(e):
                print(f"❌ DNS resolution failed: {str(e)}")
                print("💡 Possible issues:")
                print("   - Internet connectivity problem")
                print("   - Render database not ready yet")
                print("   - Incorrect DATABASE_URL")
            elif "could not connect to server" in str(e):
                print(f"❌ Connection failed: {str(e)}")
                print("💡 Database server might still be starting up...")
            else:
                print(f"❌ Database error: {str(e)}")
            
            if attempt < max_retries - 1:
                print(f"⏳ Retrying in {retry_interval} seconds...")
                time.sleep(retry_interval)
            else:
                print("❌ Max retries reached. Database unavailable.")
                return False
                
        except Exception as e:
            print(f"❌ Unexpected error: {str(e)}")
            if attempt < max_retries - 1:
                time.sleep(retry_interval)
            else:
                return False
    
    return False

async def seed_sample_schemes():
    """
    Seed sample schemes first, before creating claims
    """
    try:
        db_gen = get_db()
        db: Session = next(db_gen)
        
        # Check if schemes already exist
        scheme_count = db.query(Scheme).count()
        if scheme_count > 0:
            print("✅ Sample schemes already exist")
            db.close()
            return
            
        print("🌱 Seeding sample schemes...")
        
        sample_schemes = [
            Scheme(
                name="NHIF",
                description="National Health Insurance Fund",
                coverage_limit=500000,  # 5000.00 in cents
                is_active=True
            ),
            Scheme(
                name="Private Insurance",
                description="Private Health Insurance",
                coverage_limit=1000000,  # 10000.00 in cents
                is_active=True
            ),
            Scheme(
                name="Corporate Insurance",
                description="Corporate Health Insurance",
                coverage_limit=750000,  # 7500.00 in cents
                is_active=True
            )
        ]
        
        for scheme in sample_schemes:
            db.add(scheme)
        
        db.commit()
        print("✅ Sample schemes seeded successfully!")
        db.close()
        
    except Exception as e:
        print(f"❌ Error seeding sample schemes: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()

async def seed_sample_patients():
    """
    Seed sample patients for demo/development
    """
    try:
        db_gen = get_db()
        db: Session = next(db_gen)
        
        # Check if patients already exist
        patient_count = db.query(Patient).count()
        if patient_count > 0:
            print("✅ Sample patients already exist")
            db.close()
            return
            
        print("🌱 Seeding sample patients...")
        
        # Get admin user to assign as creator
        admin_user = db.query(User).filter(User.email == "admin@hospital.com").first()
        
        sample_patients = [
            Patient(
                first_name="John",
                last_name="Doe",
                email="john.doe@example.com",
                phone="+1234567890",
                date_of_birth=datetime.date(1990, 5, 15),
                gender="Male",
                address="123 Main Street, City",
                emergency_contact="Jane Doe",
                emergency_phone="+1234567891",
                created_by_id=admin_user.id if admin_user else None
            ),
            Patient(
                first_name="Mary",
                last_name="Smith",
                email="mary.smith@example.com",
                phone="+1234567892",
                date_of_birth=datetime.date(1985, 8, 22),
                gender="Female",
                address="456 Oak Avenue, City",
                emergency_contact="Bob Smith",
                emergency_phone="+1234567893",
                created_by_id=admin_user.id if admin_user else None
            )
        ]
        
        for patient in sample_patients:
            db.add(patient)
        
        db.commit()
        print("✅ Sample patients seeded successfully!")
        db.close()
        
    except Exception as e:
        print(f"❌ Error seeding sample patients: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()

async def seed_sample_claims():
    """
    Seed sample claims for demo/development, linked to sample patients and schemes.
    """
    try:
        db_gen = get_db()
        db: Session = next(db_gen)
        
        # Find sample patients
        patients = db.query(Patient).order_by(Patient.id).limit(2).all()
        if not patients:
            print("⚠️  No patients found, skipping claim seeding.")
            db.close()
            return
            
        # Find schemes
        schemes = db.query(Scheme).all()
        if not schemes:
            print("⚠️  No schemes found, skipping claim seeding.")
            db.close()
            return
            
        # Check if claims already exist
        claim_count = db.query(Claim).count()
        if claim_count > 0:
            print("✅ Sample claims already exist")
            db.close()
            return
            
        print("🌱 Seeding sample claims...")
        
        # Get scheme IDs
        nhif_scheme = next((s for s in schemes if s.name == "NHIF"), schemes[0])
        private_scheme = next((s for s in schemes if s.name == "Private Insurance"), schemes[0])
        
        sample_claims = [
            Claim(
                patient_id=patients[0].id,
                scheme_id=nhif_scheme.id,  # Use scheme_id now
                claim_number="CLM001",
                amount_claimed=120000,  # 1200.00 in cents
                status=ClaimStatus.pending.value,
                description="NHIF claim for outpatient visit",
                date_of_service=datetime.date.today()
            ),
            Claim(
                patient_id=patients[0].id,
                scheme_id=private_scheme.id,  # Use scheme_id now
                claim_number="CLM002", 
                amount_claimed=350000,  # 3500.00 in cents
                status=ClaimStatus.processing.value,
                description="Private insurance claim for surgery",
                date_of_service=datetime.date.today()
            ),
            Claim(
                patient_id=patients[1].id if len(patients) > 1 else patients[0].id,
                scheme_id=nhif_scheme.id,  # Use scheme_id now
                claim_number="CLM003",
                amount_claimed=80000,  # 800.00 in cents
                status=ClaimStatus.approved.value,
                description="NHIF claim for lab tests",
                date_of_service=datetime.date.today()
            )
        ]
        
        for claim in sample_claims:
            db.add(claim)
            
        db.commit()
        print("✅ Sample claims seeded successfully!")
        db.close()
        
    except Exception as e:
        print(f"❌ Error seeding sample claims: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()

async def seed_admin_user():
    """
    Create a default admin user if it doesn't exist
    """
    try:
        # Get database session
        db_gen = get_db()
        db: Session = next(db_gen)
        
        # Check if admin user already exists
        admin_user = db.query(User).filter(User.email == "admin@hospital.com").first()
        
        if not admin_user:
            print("🌱 Seeding admin user...")
            
            # Create admin user
            admin_user = User(
                username="admin",
                email="admin@hospital.com",
                first_name="System",
                last_name="Administrator",
                role="admin",
                is_active=True,
                password=get_password_hash("admin123")  # Default password
            )
            
            db.add(admin_user)
            db.commit()
            db.refresh(admin_user)
            
            print("✅ Admin user created successfully!")
            print("📧 Email: admin@hospital.com")
            print("🔑 Password: admin123")
            print("⚠️  Please change the password after first login!")
            
        else:
            print("✅ Admin user already exists")
            
        # Close database session
        db.close()
        return True
        
    except Exception as e:
        print(f"❌ Error seeding admin user: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()
        return False

async def seed_sample_data():
    """
    Seed sample data for development/demo purposes
    """
    try:
        db_gen = get_db()
        db: Session = next(db_gen)
        
        # Check if we need to seed sample data
        user_count = db.query(User).count()
        
        if user_count <= 1:  # Only admin exists
            print("🌱 Seeding sample data...")
            
            # Create sample users
            sample_users = [
                User(
                    username="dr.smith",
                    email="dr.smith@hospital.com",
                    first_name="John",
                    last_name="Smith",
                    role="doctor",
                    is_active=True,
                    password=get_password_hash("doctor123"),
                    specialization="Cardiology"
                ),
                User(
                    username="nurse.jane",
                    email="nurse.jane@hospital.com", 
                    first_name="Jane",
                    last_name="Doe",
                    role="nurse",
                    is_active=True,
                    password=get_password_hash("nurse123")
                ),
                User(
                    username="reception.mary",
                    email="reception.mary@hospital.com",
                    first_name="Mary",
                    last_name="Johnson", 
                    role="receptionist",
                    is_active=True,
                    password=get_password_hash("reception123")
                )
            ]
            
            for user in sample_users:
                db.add(user)
            
            db.commit()
            print("✅ Sample data seeded successfully!")
            
        else:
            print("✅ Sample data already exists")
            
        db.close()
        
    except Exception as e:
        print(f"❌ Error seeding sample data: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()

async def initialize_database():
    """
    Initialize database with tables and essential data
    """
    print("🗄️  Initializing database...")
    
    # First, create all tables
    # done in the database.py file
    
    # Wait for database to be available
    if not await wait_for_database():
        print("❌ Cannot connect to database. Skipping data seeding.")
        return False
    
    # Seed admin user
    admin_success = await seed_admin_user()
    if not admin_success:
        print("❌ Admin user seeding failed. Aborting further seeding.")
        return False

    # Seed sample data (users)
    await seed_sample_data()
    
    # Seed schemes first (required for claims)
    await seed_sample_schemes()
    
    # Seed sample patients (required for claims)
    await seed_sample_patients()

    # Seed sample claims (depends on schemes and patients)
    await seed_sample_claims()

    print("✅ Database initialization complete!")
    return True
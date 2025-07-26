"""
Database seeding utilities
"""
from sqlalchemy.orm import Session
from sqlalchemy.exc import OperationalError, IntegrityError
from sqlalchemy import text
from app.core.database import get_db
from app.models import User, Patient
from app.models.claim import Claim, ClaimStatus
from app.core.security import get_password_hash
import asyncio
import time

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

async def seed_admin_user():
    """
    Create a default admin user if it doesn't exist
    This ensures the app is accessible immediately after deployment
    """
    try:
        # Wait for database to be available
        if not await wait_for_database():
            print("❌ Cannot connect to database. Skipping admin user seeding.")
            return False
        
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
        
    except IntegrityError as e:
        print(f"⚠️  Admin user might already exist: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()
        return True  # Not a critical error
        
    except OperationalError as e:
        print(f"❌ Database connection error during seeding: {str(e)}")
        print("💡 Troubleshooting tips:")
        print("   1. Check if your internet connection is working")
        print("   2. Verify DATABASE_URL in .env file")
        print("   3. Ensure Render database is running")
        print("   4. Try running locally with local database first")
        if 'db' in locals():
            db.rollback()
            db.close()
        return False
        
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
    Initialize database with essential data
    """
    print("🗄️  Initializing database...")
    

async def seed_sample_claims():
    """
    Seed sample claims for demo/development, linked to sample patients.
    """
    try:
        db_gen = get_db()
        db: Session = next(db_gen)
        # Find sample patients (by email or just first 2 patients)
        patients = db.query(Patient).order_by(Patient.id).limit(2).all()
        if not patients:
            print("⚠️  No patients found, skipping claim seeding.")
            db.close()
            return
        # Check if claims already exist
        claim_count = db.query(Claim).count()
        if claim_count > 0:
            print("✅ Sample claims already exist")
            db.close()
            return
        print("🌱 Seeding sample claims...")
        sample_claims = [
            Claim(
                patient_id=patients[0].id,
                scheme="NHIF",
                amount=1200.0,
                status=ClaimStatus.pending,
                description="NHIF claim for outpatient visit"
            ),
            Claim(
                patient_id=patients[0].id,
                scheme="Private Insurance",
                amount=3500.0,
                status=ClaimStatus.processing,
                description="Private insurance claim for surgery"
            ),
            Claim(
                patient_id=patients[1].id if len(patients) > 1 else patients[0].id,
                scheme="NHIF",
                amount=800.0,
                status=ClaimStatus.approved,
                description="NHIF claim for lab tests"
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
    # Try to seed admin user
    admin_success = await seed_admin_user()
    
    if not admin_success:
        print("⚠️  Database initialization failed - continuing without seeding")
        print("💡 You can:")
        print("   1. Fix the database connection and restart the app")
        print("   2. Use local database for development")
        print("   3. Manually create admin user later")
        return False
    
    # Only seed sample data in development and if admin seeding succeeded
    from app.core.config import settings
    if settings.DEBUG and admin_success:
        await seed_sample_data()
        await seed_sample_claims()

    print("✅ Database initialization complete!")
    return True

"""
Database seeding utilities
"""
from sqlalchemy.orm import Session
from sqlalchemy.exc import OperationalError, IntegrityError
from sqlalchemy import text
from app.core.database import get_db
from app.models import User
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
    
    print("✅ Database initialization complete!")
    return True

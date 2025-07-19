"""
Database seeding utilities
"""
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models import User
from app.core.security import get_password_hash
import asyncio

async def seed_admin_user():
    """
    Create a default admin user if it doesn't exist
    This ensures the app is accessible immediately after deployment
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
        
    except Exception as e:
        print(f"❌ Error seeding admin user: {str(e)}")
        if 'db' in locals():
            db.rollback()
            db.close()

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
    await seed_admin_user()
    
    # Only seed sample data in development
    from app.core.config import settings
    if settings.DEBUG:
        await seed_sample_data()
    
    print("✅ Database initialization complete!")

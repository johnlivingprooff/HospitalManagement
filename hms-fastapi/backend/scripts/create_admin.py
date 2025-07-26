#!/usr/bin/env python3
"""
Script to create an admin user for the HMS FastAPI application
"""

from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models import User
from datetime import datetime

def create_admin_user():
    db = SessionLocal()
    try:
        # Check if admin user already exists
        existing_user = db.query(User).filter(User.email == "admin@hospital.com").first()
        if existing_user:
            print("Admin user already exists!")
            print(f"Email: {existing_user.email}")
            print(f"Active: {existing_user.is_active}")
            return

        # Create admin user
        admin_user = User(
            email="admin@hospital.com",
            password=get_password_hash("admin123"),
            first_name="Admin",
            last_name="User",
            role="admin",
            is_active=True,
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow()
        )
        
        db.add(admin_user)
        db.commit()
        db.refresh(admin_user)
        
        print("Admin user created successfully!")
        print(f"Email: {admin_user.email}")
        print(f"Password: admin123")
        print(f"Role: {admin_user.role}")
        
    except Exception as e:
        print(f"Error creating admin user: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    create_admin_user()

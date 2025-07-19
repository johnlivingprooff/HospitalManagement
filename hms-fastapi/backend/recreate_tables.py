#!/usr/bin/env python3
"""
Script to recreate the database tables for HMS FastAPI
"""

from app.core.database import engine, Base
from app.models import User, Patient, Appointment, MedicalRecord, Department
from sqlalchemy import text

def recreate_tables():
    print("Recreating database tables...")
    
    # Drop all tables with CASCADE to handle dependencies
    print("Dropping existing tables and all dependencies...")
    with engine.connect() as conn:
        # Drop all tables, views, and constraints with CASCADE
        conn.execute(text("DROP SCHEMA public CASCADE"))
        conn.execute(text("CREATE SCHEMA public"))
        conn.commit()
    
    # Create all tables
    print("Creating new tables...")
    Base.metadata.create_all(bind=engine)
    
    print("Tables recreated successfully!")
    print("\nCreated tables:")
    print("- users")
    print("- patients") 
    print("- appointments")
    print("- medical_records")
    print("- departments")
    
    # Show table structure for appointments
    with engine.connect() as conn:
        result = conn.execute(text("""
            SELECT column_name, data_type, is_nullable 
            FROM information_schema.columns 
            WHERE table_name = 'appointments' 
            ORDER BY ordinal_position;
        """))
        
        print("\nAppointments table structure:")
        for row in result:
            print(f"  {row[0]}: {row[1]} (nullable: {row[2]})")

if __name__ == "__main__":
    recreate_tables()

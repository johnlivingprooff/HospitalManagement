#!/usr/bin/env python3
"""
Script to add Ward and WardPatient tables to existing HMS database
"""

from app.core.database import engine, Base
from app.models import Ward, WardPatient
from sqlalchemy import text, inspect

def add_ward_tables():
    print("Adding Ward and WardPatient tables to existing database...")
    
    inspector = inspect(engine)
    existing_tables = inspector.get_table_names()
    
    # Check if ward tables already exist
    if 'wards' in existing_tables:
        print("Ward tables already exist. Skipping creation.")
        return
    
    # Create only the Ward and WardPatient tables
    Ward.__table__.create(engine, checkfirst=True)
    WardPatient.__table__.create(engine, checkfirst=True)
    
    print("Ward tables created successfully!")
    print("Created tables:")
    print("- wards")
    print("- ward_patients")

if __name__ == "__main__":
    add_ward_tables()
    print("\nMigration completed! You can now:")
    print("1. Run the seed_data.py script to add sample wards")
    print("2. Start the backend server to test the Ward API endpoints")

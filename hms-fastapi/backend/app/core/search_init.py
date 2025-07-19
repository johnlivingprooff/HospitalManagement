"""
Database initialization with search optimization
"""
from sqlalchemy import text
from app.core.database import get_db
from app.services.search_service import get_search_service
from app.services.cache_service import get_cache_service

async def initialize_search_optimization():
    """
    Initialize database indexes and cache service for optimal search performance
    """
    print("🚀 Initializing search optimization...")
    
    # Initialize cache service
    cache_service = await get_cache_service()
    
    # Get database session
    db = next(get_db())
    
    try:
        # Get search service and create indexes
        search_service = await get_search_service(cache_service)
        await search_service.create_database_indexes(db)
        
        print("✅ Search optimization initialized successfully")
        
    except Exception as e:
        print(f"❌ Error initializing search optimization: {e}")
    finally:
        db.close()

def create_full_text_search_function():
    """
    Create PostgreSQL full-text search functions for better search performance
    """
    return """
    -- Create a function for patient search
    CREATE OR REPLACE FUNCTION search_patients(search_text TEXT)
    RETURNS TABLE(
        id INTEGER,
        first_name VARCHAR,
        last_name VARCHAR,
        email VARCHAR,
        phone VARCHAR,
        rank REAL
    )
    LANGUAGE plpgsql
    AS $$
    BEGIN
        RETURN QUERY
        SELECT 
            p.id,
            p.first_name,
            p.last_name,
            p.email,
            p.phone,
            ts_rank(
                to_tsvector('english', p.first_name || ' ' || p.last_name || ' ' || p.email || ' ' || COALESCE(p.phone, '')),
                plainto_tsquery('english', search_text)
            ) as rank
        FROM patients p
        WHERE to_tsvector('english', p.first_name || ' ' || p.last_name || ' ' || p.email || ' ' || COALESCE(p.phone, ''))
              @@ plainto_tsquery('english', search_text)
        ORDER BY rank DESC;
    END;
    $$;

    -- Create a function for prescription search
    CREATE OR REPLACE FUNCTION search_prescriptions(search_text TEXT)
    RETURNS TABLE(
        id INTEGER,
        medication_name VARCHAR,
        patient_name TEXT,
        doctor_name TEXT,
        status VARCHAR,
        rank REAL
    )
    LANGUAGE plpgsql
    AS $$
    BEGIN
        RETURN QUERY
        SELECT 
            pr.id,
            pr.medication_name,
            (p.first_name || ' ' || p.last_name) as patient_name,
            ('Dr. ' || d.first_name || ' ' || d.last_name) as doctor_name,
            pr.status,
            ts_rank(
                to_tsvector('english', pr.medication_name || ' ' || p.first_name || ' ' || p.last_name),
                plainto_tsquery('english', search_text)
            ) as rank
        FROM prescriptions pr
        LEFT JOIN patients p ON pr.patient_id = p.id
        LEFT JOIN users d ON pr.doctor_id = d.id
        WHERE to_tsvector('english', pr.medication_name || ' ' || COALESCE(p.first_name, '') || ' ' || COALESCE(p.last_name, ''))
              @@ plainto_tsquery('english', search_text)
        ORDER BY rank DESC;
    END;
    $$;

    -- Create a function for lab test search
    CREATE OR REPLACE FUNCTION search_lab_tests(search_text TEXT)
    RETURNS TABLE(
        id INTEGER,
        test_name VARCHAR,
        test_type VARCHAR,
        patient_name TEXT,
        status VARCHAR,
        rank REAL
    )
    LANGUAGE plpgsql
    AS $$
    BEGIN
        RETURN QUERY
        SELECT 
            lt.id,
            lt.test_name,
            lt.test_type,
            (p.first_name || ' ' || p.last_name) as patient_name,
            lt.status,
            ts_rank(
                to_tsvector('english', lt.test_name || ' ' || lt.test_type || ' ' || p.first_name || ' ' || p.last_name),
                plainto_tsquery('english', search_text)
            ) as rank
        FROM lab_tests lt
        LEFT JOIN patients p ON lt.patient_id = p.id
        WHERE to_tsvector('english', lt.test_name || ' ' || lt.test_type || ' ' || COALESCE(p.first_name, '') || ' ' || COALESCE(p.last_name, ''))
              @@ plainto_tsquery('english', search_text)
        ORDER BY rank DESC;
    END;
    $$;
    """

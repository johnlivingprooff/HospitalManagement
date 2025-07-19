"""
Optimized search service with database indexing and caching
"""
from typing import List, Dict, Optional, Any, Type
from sqlalchemy.orm import Session
from sqlalchemy import text, or_, and_
from app.services.cache_service import CacheService
from app.core.config import settings

class OptimizedSearchService:
    def __init__(self, cache_service: CacheService):
        self.cache = cache_service
    
    async def search_with_cache(
        self,
        db: Session,
        model: Type,
        entity_type: str,
        search_term: Optional[str] = None,
        search_fields: List[str] = [],
        filters: Dict[str, Any] = {},
        page: int = 1,
        page_size: int = 50,
        order_by: Optional[str] = None,
        include_relations: List[str] = []
    ) -> Dict[str, Any]:
        """
        Optimized search with caching support
        """
        # Try to get from cache first
        cache_key_params = {
            "search": search_term,
            "page": page,
            "page_size": page_size,
            "order_by": order_by,
            **filters
        }
        
        cached_result = await self.cache.get_search_results(
            entity_type, 
            search_term or "", 
            **cache_key_params
        )
        
        if cached_result:
            return {
                "data": cached_result,
                "cached": True,
                "total": len(cached_result)
            }
        
        # Build query
        query = db.query(model)
        
        # Add eager loading for relationships
        for relation in include_relations:
            if hasattr(model, relation):
                query = query.joinedload(getattr(model, relation))
        
        # Apply search conditions
        if search_term and search_fields:
            search_conditions = []
            for field in search_fields:
                if "." in field:  # Handle nested field searches
                    # For now, handle simple nested fields like "patient.first_name"
                    relation, attr = field.split(".", 1)
                    if hasattr(model, relation):
                        related_model = getattr(model, relation).property.mapper.class_
                        if hasattr(related_model, attr):
                            search_conditions.append(
                                getattr(related_model, attr).ilike(f"%{search_term}%")
                            )
                else:
                    if hasattr(model, field):
                        search_conditions.append(
                            getattr(model, field).ilike(f"%{search_term}%")
                        )
            
            if search_conditions:
                query = query.filter(or_(*search_conditions))
        
        # Apply additional filters
        for key, value in filters.items():
            if hasattr(model, key) and value is not None:
                if isinstance(value, str) and value != "all":
                    query = query.filter(getattr(model, key) == value)
                elif isinstance(value, (int, float, bool)):
                    query = query.filter(getattr(model, key) == value)
        
        # Apply ordering
        if order_by and hasattr(model, order_by):
            query = query.order_by(getattr(model, order_by).desc())
        elif hasattr(model, 'created_at'):
            query = query.order_by(model.created_at.desc())
        elif hasattr(model, 'id'):
            query = query.order_by(model.id.desc())
        
        # Get total count before pagination
        total_count = query.count()
        
        # Apply pagination
        offset = (page - 1) * page_size
        results = query.offset(offset).limit(page_size).all()
        
        # Convert to dictionaries for caching
        result_data = []
        for item in results:
            item_dict = {}
            for column in item.__table__.columns:
                value = getattr(item, column.name)
                # Handle datetime serialization
                if hasattr(value, 'isoformat'):
                    value = value.isoformat()
                item_dict[column.name] = value
            
            # Add related data if requested
            for relation in include_relations:
                if hasattr(item, relation):
                    related_obj = getattr(item, relation)
                    if related_obj:
                        related_dict = {}
                        for column in related_obj.__table__.columns:
                            rel_value = getattr(related_obj, column.name)
                            if hasattr(rel_value, 'isoformat'):
                                rel_value = rel_value.isoformat()
                            related_dict[column.name] = rel_value
                        item_dict[relation] = related_dict
                    else:
                        item_dict[relation] = None
            
            result_data.append(item_dict)
        
        # Cache the results
        await self.cache.cache_search_results(
            entity_type,
            result_data,
            search_term or "",
            ttl=settings.SEARCH_CACHE_TTL,
            **cache_key_params
        )
        
        return {
            "data": result_data,
            "cached": False,
            "total": total_count,
            "page": page,
            "page_size": page_size,
            "total_pages": (total_count + page_size - 1) // page_size
        }
    
    async def create_database_indexes(self, db: Session):
        """
        Create database indexes for commonly searched fields
        """
        index_queries = [
            # Patient indexes
            "CREATE INDEX IF NOT EXISTS idx_patients_name ON patients (first_name, last_name);",
            "CREATE INDEX IF NOT EXISTS idx_patients_email ON patients (email);",
            "CREATE INDEX IF NOT EXISTS idx_patients_phone ON patients (phone);",
            "CREATE INDEX IF NOT EXISTS idx_patients_search ON patients USING gin(to_tsvector('english', first_name || ' ' || last_name || ' ' || email));",
            
            # Medical Records indexes
            "CREATE INDEX IF NOT EXISTS idx_medical_records_patient ON medical_records (patient_id);",
            "CREATE INDEX IF NOT EXISTS idx_medical_records_doctor ON medical_records (doctor_id);",
            "CREATE INDEX IF NOT EXISTS idx_medical_records_date ON medical_records (created_at);",
            "CREATE INDEX IF NOT EXISTS idx_medical_records_search ON medical_records USING gin(to_tsvector('english', title || ' ' || description || ' ' || COALESCE(diagnosis, '')));",
            
            # Prescriptions indexes
            "CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions (patient_id);",
            "CREATE INDEX IF NOT EXISTS idx_prescriptions_doctor ON prescriptions (doctor_id);",
            "CREATE INDEX IF NOT EXISTS idx_prescriptions_status ON prescriptions (status);",
            "CREATE INDEX IF NOT EXISTS idx_prescriptions_medication ON prescriptions (medication_name);",
            "CREATE INDEX IF NOT EXISTS idx_prescriptions_search ON prescriptions USING gin(to_tsvector('english', medication_name));",
            
            # Lab Tests indexes
            "CREATE INDEX IF NOT EXISTS idx_lab_tests_patient ON lab_tests (patient_id);",
            "CREATE INDEX IF NOT EXISTS idx_lab_tests_doctor ON lab_tests (doctor_id);",
            "CREATE INDEX IF NOT EXISTS idx_lab_tests_status ON lab_tests (status);",
            "CREATE INDEX IF NOT EXISTS idx_lab_tests_search ON lab_tests USING gin(to_tsvector('english', test_name || ' ' || test_type));",
            
            # Appointments indexes
            "CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments (patient_id);",
            "CREATE INDEX IF NOT EXISTS idx_appointments_doctor ON appointments (doctor_id);",
            "CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments (appointment_date);",
            "CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments (status);",
            
            # Bills indexes
            "CREATE INDEX IF NOT EXISTS idx_bills_patient ON bills (patient_id);",
            "CREATE INDEX IF NOT EXISTS idx_bills_status ON bills (status);",
            "CREATE INDEX IF NOT EXISTS idx_bills_date ON bills (created_at);",
        ]
        
        try:
            for query in index_queries:
                db.execute(text(query))
            db.commit()
            print("✅ Database search indexes created successfully")
        except Exception as e:
            print(f"❌ Error creating database indexes: {e}")
            db.rollback()
    
    async def invalidate_cache_on_update(self, entity_type: str):
        """
        Invalidate related cache entries when data is updated
        """
        await self.cache.invalidate_entity_cache(entity_type)
        
        # Also invalidate related caches
        related_caches = {
            "patients": ["medical_records", "prescriptions", "lab_tests", "appointments", "bills"],
            "prescriptions": ["patients"],
            "lab_tests": ["patients"],
            "medical_records": ["patients"],
            "appointments": ["patients"],
            "bills": ["patients"]
        }
        
        if entity_type in related_caches:
            for related_type in related_caches[entity_type]:
                await self.cache.invalidate_entity_cache(related_type)

# Global search service instance
search_service = None

async def get_search_service(cache: CacheService) -> OptimizedSearchService:
    """Get or create search service instance"""
    global search_service
    if not search_service:
        search_service = OptimizedSearchService(cache)
    return search_service

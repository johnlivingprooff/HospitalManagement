"""
Redis caching service for optimized search functionality
"""
import json
import hashlib
from typing import Optional, Any, List, Dict
from datetime import datetime, timedelta
import redis.asyncio as redis
from app.core.config import settings

class CacheService:
    def __init__(self):
        self.redis_client: Optional[redis.Redis] = None
        self._initialized = False
    
    async def initialize(self):
        """Initialize Redis connection"""
        if not self._initialized:
            try:
                self.redis_client = redis.from_url(
                    settings.REDIS_URL or "redis://localhost:6379",
                    encoding="utf-8",
                    decode_responses=True
                )
                # Test connection
                await self.redis_client.ping()
                self._initialized = True
                print("✅ Redis cache service initialized")
            except Exception as e:
                print(f"❌ Redis connection failed: {e}")
                self.redis_client = None
                self._initialized = False
    
    async def close(self):
        """Close Redis connection"""
        if self.redis_client:
            await self.redis_client.close()
    
    def _generate_cache_key(self, prefix: str, **kwargs) -> str:
        """Generate a consistent cache key from parameters"""
        # Sort kwargs for consistent key generation
        sorted_params = sorted(kwargs.items())
        params_str = "&".join([f"{k}={v}" for k, v in sorted_params if v is not None])
        
        # Create hash of parameters for shorter keys
        params_hash = hashlib.md5(params_str.encode()).hexdigest()[:8]
        return f"{settings.APP_NAME}:{prefix}:{params_hash}"
    
    async def get(self, key: str) -> Optional[Dict]:
        """Get cached data"""
        if not self.redis_client:
            return None
        
        try:
            cached_data = await self.redis_client.get(key)
            if cached_data:
                return json.loads(cached_data)
        except Exception as e:
            print(f"Cache get error: {e}")
        
        return None
    
    async def set(self, key: str, data: Any, ttl: int = 300) -> bool:
        """Set cached data with TTL (time to live) in seconds"""
        if not self.redis_client:
            return False
        
        try:
            serialized_data = json.dumps(data, default=str)  # default=str handles datetime objects
            await self.redis_client.setex(key, ttl, serialized_data)
            return True
        except Exception as e:
            print(f"Cache set error: {e}")
            return False
    
    async def delete(self, pattern: str) -> bool:
        """Delete cache entries matching pattern"""
        if not self.redis_client:
            return False
        
        try:
            keys = await self.redis_client.keys(pattern)
            if keys:
                await self.redis_client.delete(*keys)
            return True
        except Exception as e:
            print(f"Cache delete error: {e}")
            return False
    
    async def get_search_results(self, entity_type: str, search_term: str = "", **filters) -> Optional[List]:
        """Get cached search results"""
        cache_key = self._generate_cache_key(
            f"search:{entity_type}", 
            search=search_term, 
            **filters
        )
        return await self.get(cache_key)
    
    async def cache_search_results(self, entity_type: str, results: List, search_term: str = "", ttl: int = 300, **filters) -> bool:
        """Cache search results"""
        cache_key = self._generate_cache_key(
            f"search:{entity_type}", 
            search=search_term, 
            **filters
        )
        return await self.set(cache_key, results, ttl)
    
    async def invalidate_entity_cache(self, entity_type: str):
        """Invalidate all cached data for an entity type"""
        pattern = f"{settings.APP_NAME}:search:{entity_type}:*"
        return await self.delete(pattern)
    
    async def get_aggregated_stats(self, entity_type: str) -> Optional[Dict]:
        """Get cached aggregated statistics"""
        cache_key = self._generate_cache_key(f"stats:{entity_type}")
        return await self.get(cache_key)
    
    async def cache_aggregated_stats(self, entity_type: str, stats: Dict, ttl: int = 600) -> bool:
        """Cache aggregated statistics with longer TTL"""
        cache_key = self._generate_cache_key(f"stats:{entity_type}")
        return await self.set(cache_key, stats, ttl)

# Global cache service instance
cache_service = CacheService()

async def get_cache_service() -> CacheService:
    """Dependency function to get cache service"""
    if not cache_service._initialized:
        await cache_service.initialize()
    return cache_service

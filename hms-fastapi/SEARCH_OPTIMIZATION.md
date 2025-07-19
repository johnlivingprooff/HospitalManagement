# HMS Search Optimization Guide

## Overview
This guide covers the implementation of optimized search functionality with database indexing and Redis caching for the Hospital Management System (HMS).

## Features Implemented

### 🔍 Frontend Search Enhancements
- **Real-time Search**: Debounced input (300ms) to reduce API calls
- **Enhanced Search Input**: Loading states, clear buttons, visual feedback
- **Client-side Filtering**: Optimized filtering for large datasets
- **Consistent Search UI**: Standardized search components across all pages

### 🚀 Backend Optimizations
- **Redis Caching**: Search results cached for 5 minutes
- **Database Indexing**: Optimized indexes for common search queries
- **Full-text Search**: PostgreSQL full-text search for better relevance
- **Intelligent Cache Invalidation**: Auto-invalidate cache on data updates

## Setup Instructions

### 1. Redis Installation

#### Windows (using Docker)
```powershell
# Install Docker Desktop first, then:
docker run --name hms-redis -p 6379:6379 -d redis:7-alpine

# Or using Chocolatey:
choco install redis-64
```

#### Linux/macOS
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install redis-server

# macOS with Homebrew
brew install redis
brew services start redis
```

### 2. Environment Configuration

Add these variables to your `.env` file:

```env
# Redis Configuration
REDIS_URL=redis://localhost:6379
CACHE_TTL=300
SEARCH_CACHE_TTL=300
STATS_CACHE_TTL=600

# Database (ensure PostgreSQL is running)
DATABASE_URL=postgresql://username:password@localhost:5432/hms
```

### 3. Backend Setup

```bash
# Navigate to backend directory
cd hms-fastapi/backend

# Install new dependencies
pip install redis==5.0.1 hiredis==2.3.2

# Or install from requirements
pip install -r requirements.txt

# Start the backend
uvicorn app.main:app --reload --port 8000
```

### 4. Frontend Setup

```bash
# Navigate to frontend directory
cd hms-fastapi/frontend

# Install dependencies (if not already done)
npm install

# Start the frontend
npm run dev
```

## Search Features by Page

### 🏥 Patients Page
- **Search Fields**: Name, email, phone number
- **Real-time Results**: Debounced search with loading states
- **Caching**: 5-minute cache for search results
- **Database Index**: Full-text search on name and email

### 💊 Pharmacy Page
- **Search Fields**: Medication name, patient name, doctor name
- **Client-side Filtering**: Status filters combined with search
- **Performance**: Client-side filtering for prescriptions

### 📋 Medical Records Page
- **Search Fields**: Record title, description, diagnosis, patient name
- **Advanced Search**: Multi-field search with relevance ranking
- **Cache Strategy**: Smart cache invalidation on record updates

### 🧪 Lab Tests Page
- **Search Fields**: Test name, test type, patient name
- **Status Filtering**: Combined search and status filtering
- **Real-time Updates**: Live search with status changes

## Performance Improvements

### Before Optimization
- ❌ Search on every keystroke
- ❌ No caching - repeated database queries
- ❌ Basic string matching only
- ❌ No database indexes

### After Optimization
- ✅ Debounced search (300ms delay)
- ✅ Redis cache (5-minute TTL)
- ✅ Database indexes for fast queries
- ✅ Full-text search with ranking
- ✅ Smart cache invalidation
- ✅ Loading states and user feedback

## Database Indexes Created

```sql
-- Patient search optimization
CREATE INDEX idx_patients_search ON patients 
USING gin(to_tsvector('english', first_name || ' ' || last_name || ' ' || email));

-- Prescription search optimization
CREATE INDEX idx_prescriptions_search ON prescriptions 
USING gin(to_tsvector('english', medication_name));

-- Lab test search optimization
CREATE INDEX idx_lab_tests_search ON lab_tests 
USING gin(to_tsvector('english', test_name || ' ' || test_type));

-- Medical records search optimization
CREATE INDEX idx_medical_records_search ON medical_records 
USING gin(to_tsvector('english', title || ' ' || description || ' ' || COALESCE(diagnosis, '')));
```

## Redis Cache Keys Structure

```
HMS:search:patients:a1b2c3d4  # Cached patient search results
HMS:search:prescriptions:e5f6g7h8  # Cached prescription results
HMS:stats:patients:i9j0k1l2  # Cached patient statistics
```

## Monitoring and Maintenance

### Cache Performance
```bash
# Connect to Redis CLI
redis-cli

# Check memory usage
INFO memory

# View all HMS cache keys
KEYS HMS:*

# Clear all cache (if needed)
FLUSHDB
```

### Database Performance
```sql
-- Check index usage
SELECT schemaname, tablename, indexname, idx_tup_read, idx_tup_fetch 
FROM pg_stat_user_indexes 
WHERE schemaname = 'public';

-- Analyze search query performance
EXPLAIN ANALYZE SELECT * FROM patients 
WHERE to_tsvector('english', first_name || ' ' || last_name) 
@@ plainto_tsquery('english', 'search term');
```

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   ```bash
   # Check if Redis is running
   redis-cli ping
   # Should return: PONG
   ```

2. **Search Not Working**
   - Check if database indexes were created
   - Verify Redis is accessible
   - Check browser console for JavaScript errors

3. **Slow Search Performance**
   - Monitor Redis memory usage
   - Check database index usage
   - Verify cache hit rates

### Performance Metrics

Expected performance improvements:
- **Search Response Time**: 50-80% faster
- **Database Load**: 70% reduction in search queries
- **User Experience**: Real-time search with visual feedback
- **Cache Hit Rate**: 60-80% for repeated searches

## Future Enhancements

- [ ] Elasticsearch integration for advanced search
- [ ] Search analytics and user behavior tracking
- [ ] Autocomplete suggestions
- [ ] Search result highlighting
- [ ] Advanced filtering combinations
- [ ] Export search results functionality

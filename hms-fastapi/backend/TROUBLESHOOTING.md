# HMS Backend Troubleshooting Guide

## PostgreSQL SSL Connection Issues

### Problem: "SSL connection has been closed unexpectedly"

**Error Message:**
```
psycopg2.OperationalError: connection to server at "dpg-xxx.oregon-postgres.render.com" 
(35.227.164.209), port 5432 failed: SSL connection has been closed unexpectedly
```

**Root Cause:**
Cloud-hosted PostgreSQL databases (Render, Railway, Supabase, etc.) require SSL connections for security. 
The error occurs when the database client doesn't properly configure SSL mode.

**Solution:**
This has been fixed in `app/core/database.py` by automatically adding SSL configuration for PostgreSQL connections:

```python
# SSL configuration is now automatically applied for PostgreSQL
# Default: sslmode=require
# Can be customized via POSTGRES_SSL_MODE environment variable
connect_args = {
    "sslmode": os.getenv("POSTGRES_SSL_MODE", "require"),
    "connect_timeout": 10
}
```

**Custom SSL Mode:**
You can override the default SSL mode by setting the `POSTGRES_SSL_MODE` environment variable:

```bash
# In your deployment environment or .env file
POSTGRES_SSL_MODE=verify-ca
```

### Verification

After deploying the fix, your logs should show:
```
🚀 Starting HMS FastAPI application...
✅ Database tables created (or already exist)
✅ HMS FastAPI application started successfully
```

Instead of the SSL error.

### Additional SSL Modes (if needed)

The default SSL mode is `require`, which works for most cloud databases. You can customize this using the `POSTGRES_SSL_MODE` environment variable:

| SSL Mode | Description | When to Use | How to Set |
|----------|-------------|-------------|------------|
| `require` | Requires SSL, but doesn't verify certificate | Most cloud databases (Render, Railway) | Default, no action needed |
| `verify-ca` | Requires SSL and verifies certificate against CA | When you have CA certificate | `POSTGRES_SSL_MODE=verify-ca` |
| `verify-full` | Requires SSL and verifies hostname matches certificate | Maximum security, requires full cert chain | `POSTGRES_SSL_MODE=verify-full` |
| `prefer` | Tries SSL first, falls back to non-SSL | Local development | `POSTGRES_SSL_MODE=prefer` |
| `disable` | No SSL (insecure) | Only for local testing | `POSTGRES_SSL_MODE=disable` |

**Setting via Environment Variable (Recommended):**
```bash
# In your .env file or deployment environment
POSTGRES_SSL_MODE=verify-ca
```

**Alternative: Modify code directly:**
If you need more control (e.g., adding certificate paths), you can modify `app/core/database.py`:
```python
connect_args = {
    "sslmode": "verify-ca",  # Change this if needed
    "sslrootcert": "/path/to/ca-cert.pem",  # Add if using verify-ca
    "connect_timeout": 10
}
```

### Environment Variables

Ensure your `DATABASE_URL` is correctly set:

**Correct formats:**
```bash
# Render PostgreSQL
DATABASE_URL=postgresql://user:password@dpg-xxx.oregon-postgres.render.com:5432/dbname

# Railway PostgreSQL
DATABASE_URL=postgresql://postgres:password@containers-us-west-xxx.railway.app:5432/railway

# Supabase PostgreSQL
DATABASE_URL=postgresql://postgres:password@db.xxx.supabase.co:5432/postgres

# Local PostgreSQL (no SSL required)
DATABASE_URL=postgresql://postgres:password@localhost:5432/hms
```

**Note:** The SSL configuration is automatically applied for all URLs starting with `postgresql://` or `postgresql+psycopg2://`

### Testing Database Connection

Use the provided test script:

```bash
cd /path/to/backend
python scripts/test_database.py
```

This will:
- ✅ Test DNS resolution
- ✅ Test port connectivity
- ✅ Test database authentication
- ✅ Provide troubleshooting suggestions

## Other Common Issues

### Issue: "Module not found: psycopg2"

**Solution:**
```bash
pip install psycopg2-binary
# or install all dependencies
pip install -r requirements.txt
```

### Issue: Database connection timeout

**Symptoms:**
- Application hangs on startup
- Timeout errors after 30 seconds

**Solutions:**
1. Check if database is accessible (firewall, security groups)
2. Verify DATABASE_URL is correct
3. Ensure database is not paused/suspended (free tier databases)
4. Check if your IP is whitelisted (some providers require this)

### Issue: "Too many connections"

**Symptoms:**
```
psycopg2.OperationalError: FATAL: remaining connection slots are reserved
```

**Solution:**
Adjust connection pool settings in `app/core/database.py`:

```python
engine = create_engine(
    settings.DATABASE_URL,
    pool_size=5,          # Reduce from default 10
    max_overflow=10,      # Maximum connections beyond pool_size
    pool_pre_ping=True,
    pool_recycle=3600,
    pool_timeout=30,
    connect_args=connect_args
)
```

### Issue: Migration/Alembic errors

**Symptoms:**
- Tables not created
- Schema mismatch errors

**Solution:**
```bash
# Drop all tables and recreate (CAUTION: Data loss!)
# Only for development/testing
cd backend
python -c "from app.core.database import Base, engine; Base.metadata.drop_all(engine); Base.metadata.create_all(engine)"
```

## Getting Help

If issues persist:

1. **Check the logs:**
   - On Render: View logs in dashboard
   - On Railway: `railway logs`
   - Locally: Check terminal output

2. **Verify environment variables:**
   ```bash
   # Check if DATABASE_URL is set
   echo $DATABASE_URL
   ```

3. **Test database directly:**
   ```bash
   # Using psql
   psql $DATABASE_URL
   
   # Using Python
   python -c "import psycopg2; conn = psycopg2.connect('$DATABASE_URL', sslmode='require'); print('✅ Connected')"
   ```

4. **Common checklist:**
   - [ ] Database is running and accessible
   - [ ] DATABASE_URL environment variable is set
   - [ ] DATABASE_URL format is correct
   - [ ] Database credentials are valid
   - [ ] Network/firewall allows connections
   - [ ] SSL is properly configured (for cloud databases)
   - [ ] Dependencies are installed (`requirements.txt`)

## Contact

For additional support, please refer to:
- [PostgreSQL SSL Documentation](https://www.postgresql.org/docs/current/libpq-ssl.html)
- [SQLAlchemy Connection Guide](https://docs.sqlalchemy.org/en/20/core/engines.html)
- [Render PostgreSQL Docs](https://render.com/docs/databases)

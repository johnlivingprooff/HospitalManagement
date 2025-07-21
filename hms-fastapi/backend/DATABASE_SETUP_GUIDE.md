# 🗄️ HMS Database Setup Guide

This folder contains multiple database setup scripts to help you get HMS running with different PostgreSQL configurations.

## 📋 Available Setup Scripts

### 🎯 **Main Menu** (Recommended)
- **`setup_db_menu.bat`** - Interactive menu to choose setup option
- Run this first if you're unsure which option to choose

### 🐳 **Docker Setup** (Easiest)
- **`setup_local_db.bat`** - Sets up PostgreSQL using Docker
- **Pros**: Quick setup, isolated environment, easy cleanup
- **Cons**: Requires Docker Desktop
- **Best for**: Development, testing, quick prototyping

### 🏠 **Native Setup** (Best Performance)
- **`setup_native_db.bat`** - Sets up native PostgreSQL on Windows
- **`setup_native_db.ps1`** - PowerShell version with better error handling
- **Pros**: Better performance, persistent across restarts, no Docker needed
- **Cons**: More complex setup, affects system globally
- **Best for**: Long-term development, production-like environment

### 🔧 **Troubleshooting**
- **`test_database.py`** - Diagnoses database connection issues
- Provides detailed error information and solutions

## 🚀 Quick Start

### Option 1: Use the Menu (Recommended)
```bash
# Run the interactive menu
setup_db_menu.bat

# Follow the prompts to choose your preferred setup
```

### Option 2: Direct Setup

#### Docker Setup:
```bash
# Requires Docker Desktop
setup_local_db.bat
```

#### Native Setup:
```bash
# Batch version
setup_native_db.bat

# PowerShell version (better error handling)
powershell -ExecutionPolicy Bypass -File setup_native_db.ps1
```

## 📊 Comparison Table

| Feature | Docker | Native | Cloud |
|---------|--------|--------|-------|
| **Setup Difficulty** | Easy | Medium | Medium |
| **Performance** | Good | Excellent | Good |
| **Resource Usage** | Higher | Lower | N/A |
| **Persistence** | Container-based | System-wide | Always |
| **Internet Required** | No | No | Yes |
| **Cleanup** | Very Easy | Manual | N/A |
| **Best For** | Development | Production-like | Production |

## 🔧 Database Configuration

After running any setup script, you'll get these connection details:

### Docker Configuration:
```env
DATABASE_URL=postgresql://postgres:password@localhost:5432/hms
```

### Native Configuration:
```env
DATABASE_URL=postgresql://hms_user:hms_password_123@localhost:5432/hms
```

### Cloud Configuration:
```env
DATABASE_URL=postgresql://user:password@host:port/database
```

## 📁 Generated Files

Each setup creates configuration files:

- **`.env`** - Active environment configuration
- **`.env.local`** - Docker PostgreSQL settings (from Docker setup)
- **`.env.native`** - Native PostgreSQL settings (from Native setup)
- **`.env.cloud`** - Cloud database settings (manual creation)

### Switching Between Configurations:
```bash
# Switch to Docker
copy .env.local .env

# Switch to Native
copy .env.native .env

# Switch to Cloud
copy .env.cloud .env
```

## 🚀 Starting HMS After Setup

Once your database is set up, start the HMS application:

```bash
# Navigate to backend directory
cd backend

# Install dependencies (if not done yet)
pip install -r requirements.txt

# Start the application
python -m uvicorn app.main:app --reload

# Application will be available at:
# http://localhost:8000
```

## 🔍 Troubleshooting

### Common Issues:

1. **"Connection refused"**
   - Database service not running
   - Check if PostgreSQL service is started
   - For Docker: `docker ps` to see running containers

2. **"Authentication failed"**
   - Incorrect password in .env file
   - Check PostgreSQL user credentials

3. **"Database does not exist"**
   - HMS database not created
   - Re-run the setup script

4. **"Permission denied"**
   - User doesn't have database privileges
   - Check user permissions in PostgreSQL

### Diagnostic Commands:

```bash
# Test database connection
python test_database.py

# Check PostgreSQL service (Windows)
sc query postgresql-x64-15

# Check Docker containers
docker ps

# Connect to database manually
psql -h localhost -U hms_user -d hms

# List all databases
psql -h localhost -U postgres -l
```

## 🔧 PostgreSQL Service Management

### Windows Service Commands:
```bash
# Start PostgreSQL
net start postgresql-x64-15

# Stop PostgreSQL
net stop postgresql-x64-15

# Check status
sc query postgresql-x64-15
```

### Docker Commands:
```bash
# Start container
docker start hms-postgres

# Stop container
docker stop hms-postgres

# Remove container
docker rm hms-postgres

# View logs
docker logs hms-postgres
```

## 📋 Default Credentials

### Admin User (Auto-created):
- **Email**: admin@hospital.com
- **Password**: admin123
- **⚠️ Change password after first login!**

### Database Users:

#### Docker:
- **User**: postgres
- **Password**: password
- **Database**: hms

#### Native:
- **User**: hms_user
- **Password**: hms_password_123
- **Database**: hms

## 🌐 Cloud Deployment

For production deployment, see:
- **`CLOUD_DOCKER_DEPLOYMENT.md`** - Complete cloud deployment guide
- Supports Railway, Render, DigitalOcean, and other platforms

## 💡 Tips

1. **Development**: Use Docker for quick setup and testing
2. **Performance**: Use Native for better database performance
3. **Production**: Use Cloud databases for production deployments
4. **Backup**: Regular database backups are recommended
5. **Security**: Change default passwords in production

## 📞 Support

If you encounter issues:

1. Run the troubleshooter: `python test_database.py`
2. Check the logs in your terminal
3. Verify your .env file configuration
4. Ensure PostgreSQL service is running
5. Check firewall settings if connection fails

---

**Happy coding with HMS! 🏥✨**

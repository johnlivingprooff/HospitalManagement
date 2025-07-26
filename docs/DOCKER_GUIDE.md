# 🐳 HMS Docker Deployment Guide

## 🚀 Quick Start

### Prerequisites
- Docker Desktop installed and running
- 4GB+ free RAM
- 10GB+ free disk space

### One-Command Deployment

**Windows:**
```cmd
deploy.bat
```

**Linux/Mac:**
```bash
chmod +x deploy.sh
./deploy.sh
```

## 📋 What Gets Deployed

### Services Included
- **PostgreSQL 15** - Database server
- **Redis 7** - Caching and session storage
- **FastAPI Backend** - Python REST API
- **React Frontend** - TypeScript web application
- **Nginx** - Production web server

### Network Architecture
```
Internet → Frontend (Port 80) → Backend (Port 8000) → Database (Port 5432)
                              ↘ Redis (Port 6379)
```

## ⚙️ Configuration

### Environment Variables (.env)
```env
# Database
DB_PASSWORD=hms123

# Backend Security
SECRET_KEY=your-secret-key-here
DEBUG=false
ALLOWED_ORIGINS=http://localhost:80

# Frontend
VITE_API_URL=http://localhost:8000

# Optional Production Settings
FRONTEND_URL=https://your-domain.com
BACKEND_URL=https://api.your-domain.com
```

### Generate Secure Secret Key
```bash
# Method 1: OpenSSL
openssl rand -hex 32

# Method 2: Python
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

## 🏥 Accessing HMS

Once deployed, access HMS at:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8000
- **API Documentation**: http://localhost:8000/docs
- **Database**: localhost:5432

### Default Login
- **Email**: admin@hms.local
- **Password**: admin123

## 📊 Docker Management

### View Service Status
```bash
docker-compose ps
```

### View Real-time Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f database
```

### Start/Stop Services
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart specific service
docker-compose restart backend
```

### Update Services
```bash
# Pull latest images and restart
docker-compose pull
docker-compose up -d
```

## 🗄️ Database Management

### Backup Database
```bash
# Create backup
docker exec hms_database pg_dump -U postgres hms > hms_backup_$(date +%Y%m%d_%H%M%S).sql

# Scheduled backup (Linux/Mac)
0 2 * * * docker exec hms_database pg_dump -U postgres hms > /backups/hms_backup_$(date +\%Y\%m\%d_\%H\%M\%S).sql
```

### Restore Database
```bash
# Stop backend to prevent connections
docker-compose stop backend

# Restore from backup
docker exec -i hms_database psql -U postgres hms < your_backup.sql

# Start backend
docker-compose start backend
```

### Access Database Directly
```bash
# PostgreSQL CLI
docker exec -it hms_database psql -U postgres hms

# View tables
\dt

# Exit
\q
```

## 🔍 Troubleshooting

### Common Issues

#### Services Won't Start
```bash
# Check Docker status
docker --version
docker-compose --version

# Check for port conflicts
netstat -tulpn | grep :80
netstat -tulpn | grep :8000
netstat -tulpn | grep :5432
```

#### Database Connection Issues
```bash
# Check database health
docker exec hms_database pg_isready -U postgres

# Check database logs
docker-compose logs database
```

#### Frontend Not Loading
```bash
# Check frontend logs
docker-compose logs frontend

# Rebuild frontend
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

#### Backend API Issues
```bash
# Check backend health
curl http://localhost:8000/health

# Check backend logs
docker-compose logs backend

# Restart backend
docker-compose restart backend
```

### Performance Optimization

#### Increase Resources
```bash
# Stop services
docker-compose down

# Edit docker-compose.yml to add resource limits
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

#### Clean Up Resources
```bash
# Remove unused containers
docker container prune

# Remove unused images
docker image prune

# Remove unused volumes (⚠️  Will delete data)
docker volume prune

# Complete cleanup (⚠️  Will delete everything)
docker system prune -a
```

## 🔒 Security Considerations

### Production Security
```bash
# 1. Change default passwords
# Edit .env file with strong passwords

# 2. Use HTTPS in production
# Configure SSL certificates in nginx/certs/

# 3. Restrict database access
# Remove database port mapping in production
```

### Firewall Configuration
```bash
# Allow only necessary ports
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 5432/tcp  # Block direct database access
sudo ufw deny 6379/tcp  # Block direct Redis access
```

## 📈 Monitoring

### Health Checks
```bash
# Check all service health
docker-compose ps

# Detailed health status
docker inspect --format='{{.State.Health.Status}}' hms_backend
docker inspect --format='{{.State.Health.Status}}' hms_database
```

### Resource Usage
```bash
# View resource usage
docker stats

# Disk usage
docker system df
```

## 🚀 Production Deployment

### Production Configuration
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  nginx:
    profiles: []  # Always start in production
  backend:
    environment:
      DEBUG: false
      ALLOWED_ORIGINS: https://your-domain.com
  database:
    ports: []  # Remove port mapping for security
```

### Deploy to Production
```bash
# Use production compose file
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 📞 Support

### Get Help
- **Documentation**: Check `/docs` folder
- **Logs**: Always check `docker-compose logs` first
- **Health**: Use health check endpoints
- **Community**: Create GitHub issue

### Useful Links
- **Docker Documentation**: https://docs.docker.com/
- **FastAPI Docs**: https://fastapi.tiangolo.com/
- **React Docs**: https://reactjs.org/docs/

---

**🎉 Your HMS is now running in Docker containers! Enjoy the modern, containerized hospital management experience.**

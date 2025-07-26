#!/bin/bash

# HMS Docker Deployment Script
set -e

echo "🏥 Starting HMS Docker Deployment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Create .env file from example if it doesn't exist
if [ ! -f .env ]; then
    echo "📄 Creating .env file from example..."
    cp .env.example .env
    echo "⚠️  Please edit .env file with your configuration before running again."
    echo "🔐 Generate a secure SECRET_KEY using: openssl rand -hex 32"
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p database
mkdir -p backend/localdata/logs
mkdir -p backend/localdata/attachments
mkdir -p backend/localdata/profile-images

# Pull latest images
echo "📦 Pulling latest Docker images..."
docker-compose pull

# Build custom images
echo "🔨 Building custom images..."
docker-compose build

# Start services
echo "🚀 Starting HMS services..."
docker-compose up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo "🔍 Checking service health..."
docker-compose ps

# Show logs
echo "📋 Recent logs:"
docker-compose logs --tail=20

echo ""
echo "✅ HMS is now running!"
echo ""
echo "🌐 Frontend: http://localhost"
echo "🔧 Backend API: http://localhost:8000"
echo "📚 API Docs: http://localhost:8000/docs"
echo "🗄️  Database: localhost:5432"
echo ""
echo "🔑 Default Login:"
echo "   Email: admin@hms.local"
echo "   Password: admin123"
echo ""
echo "📝 Useful commands:"
echo "   View logs: docker-compose logs -f"
echo "   Stop services: docker-compose down"
echo "   Update services: docker-compose pull && docker-compose up -d"
echo "   Backup database: docker exec hms_database pg_dump -U postgres hms > backup.sql"

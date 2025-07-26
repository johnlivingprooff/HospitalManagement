# Hospital Management System Architecture

## Overview
This project is a modern, cloud-native Hospital Management System (HMS) designed for extensibility, maintainability, and ease of deployment. It consists of a FastAPI backend, a React frontend, and a legacy Java implementation for reference and migration.

## Major Components
- **Backend (hms-fastapi/backend/app/):** FastAPI app with modular structure (api, core, models, schemas)
- **Frontend (hms-fastapi/frontend/src/):** React app with components, pages, contexts, and utilities
- **Database:** PostgreSQL, managed via SQLAlchemy ORM
- **Authentication:** JWT tokens, BCrypt password hashing, role-based access
- **Legacy:** Java Spark-based HMS in `legacy-java-hms/` (read-only)

## Data Flow
- All API endpoints are under `/api/` and protected by JWT authentication
- Frontend communicates with backend via RESTful API calls
- Database migrations are handled by SQLAlchemy models on startup

## Deployment
- Backend: Render.com (see README for build/start commands)
- Frontend: Netlify/Vercel (build with `npm run build`, publish `dist/`)

## Key Files/Directories
- `hms-fastapi/backend/app/main.py`: FastAPI app entrypoint
- `hms-fastapi/frontend/src/pages/`: Main React pages
- `.env` files: Environment configuration for backend/frontend

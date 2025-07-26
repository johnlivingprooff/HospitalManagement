# Integration & Deployment

## Cloud Providers
- **Backend:** Render.com
  - Build: `pip install -r requirements.txt`
  - Start: `uvicorn main:app --host 0.0.0.0 --port $PORT`
- **Frontend:** Netlify/Vercel
  - Build: `npm run build`
  - Publish: `dist/`

## Environment Variables
- **Backend:**
  - `DATABASE_URL`, `SECRET_KEY`, `ALLOWED_ORIGINS`, `REDIS_URL`, etc.
- **Frontend:**
  - `VITE_API_URL`

## Database
- PostgreSQL
- Tables auto-created by SQLAlchemy models on backend startup

## API Docs
- **Swagger UI:** `/docs`
- **ReDoc:** `/redoc`

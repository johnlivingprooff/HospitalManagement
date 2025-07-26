# Developer Workflows

## Backend (FastAPI)
- **Install dependencies:**
  ```bash
  cd hms-fastapi/backend
  pip install -r requirements.txt
  ```
- **Run server (dev):**
  ```bash
  uvicorn app.main:app --reload --port 8001
  ```
- **Environment:**
  - Use `.env` in `backend/` (see example in repo)
- **Database:**
  - PostgreSQL, tables auto-created by SQLAlchemy models on startup
  - No Alembic scripts by default

## Frontend (React)
- **Install dependencies:**
  ```bash
  cd hms-fastapi/frontend
  npm install
  ```
- **Run dev server:**
  ```bash
  npm run dev
  ```
- **API URL:**
  - Set `VITE_API_URL` in frontend `.env`

## Testing
- No formal test suite
- Manual testing via Swagger UI (`/docs`) and frontend

## Deployment
- **Backend:** Render.com (see README for build/start commands)
- **Frontend:** Netlify/Vercel (build with `npm run build`, publish `dist/`)

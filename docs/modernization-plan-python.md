# HMS Modernization Plan - Python Stack

## Frontend (React + Vite)
- **Framework**: React with Vite
- **UI Library**: Tailwind CSS + React Hook Form
- **State Management**: React Query + Zustand
- **Deployment**: Netlify or Vercel

## Backend (Python FastAPI)
- **Framework**: FastAPI
- **Database**: PostgreSQL (Supabase, Neon, or Railway)
- **ORM**: SQLAlchemy or Tortoise ORM
- **Authentication**: JWT + passlib (bcrypt)
- **Deployment**: Render, Railway, or Fly.io

## Migration Strategy
```python
# Example FastAPI structure
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from passlib.context import CryptContext

app = FastAPI(title="HMS API", version="2.0.0")

# Password hashing (compatible with current BCrypt)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

@app.post("/auth/login")
async def login(credentials: UserCredentials, db: Session = Depends(get_db)):
    user = authenticate_user(db, credentials.email, credentials.password)
    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return {"access_token": create_access_token(user.id)}

@app.get("/patients/")
async def get_patients(db: Session = Depends(get_db)):
    return db.query(Patient).all()
```

## File Structure
```
hms-python/
├── frontend/                # React + Vite
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── services/
│   │   └── types/
│   ├── public/
│   └── package.json
├── backend/                 # FastAPI
│   ├── app/
│   │   ├── api/
│   │   │   └── v1/
│   │   │       ├── auth.py
│   │   │       ├── patients.py
│   │   │       └── appointments.py
│   │   ├── core/
│   │   │   ├── config.py
│   │   │   ├── security.py
│   │   │   └── database.py
│   │   ├── models/
│   │   ├── schemas/
│   │   └── main.py
│   ├── alembic/             # Database migrations
│   └── requirements.txt
└── docker-compose.yml       # For local development
```

## Key Benefits
- **Performance**: FastAPI is extremely fast
- **Type Safety**: Pydantic models with automatic validation
- **Documentation**: Auto-generated OpenAPI/Swagger docs
- **Familiar**: Similar to current Python-style logic
- **Deployment**: Easy on Render, Railway, Fly.io
```

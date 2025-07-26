# Project Conventions & Patterns

## API
- RESTful, JWT-protected, all endpoints under `/api/`
- Error responses are JSON

## Authentication
- JWT tokens, BCrypt password hashing
- Role-based access (see `useRole` in frontend)

## Frontend
- Uses React Query for data fetching/caching
- Skeleton loading states: see `components/loading/`
- All modals use `components/Modal.tsx`
- Toast notifications: `components/Toast.tsx`
- Error handling: errors shown as toast popups, not blocking UI

## CORS
- Origins set via comma-separated string in `.env`, parsed in `Settings` class

## Adding Features
- **API route:** Place in `hms-fastapi/backend/app/api/`, register in `main.py`
- **Page:** Place in `hms-fastapi/frontend/src/pages/`, use skeleton loading and toast for errors

## Legacy
- `legacy-java-hms/` is read-only, for reference/migration only

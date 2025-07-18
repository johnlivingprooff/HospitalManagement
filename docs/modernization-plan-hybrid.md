# HMS Modernization Plan - Hybrid Approach

## Frontend Modernization Only
- **Keep**: Current Java backend with Spark Framework
- **Modernize**: Frontend with React/Next.js
- **Deploy**: Frontend on Vercel/Netlify, Backend on Railway/Render

## Architecture
```
┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   Backend       │
│   (Next.js)     │     │   (Java/Spark)  │
│   Vercel        │     │   Railway       │
└─────────────────┘     └─────────────────┘
                               │
                        ┌─────────────────┐
                        │   Database      │
                        │   PostgreSQL    │
                        │   Railway       │
                        └─────────────────┘
```

## Implementation Steps

### 1. Create API Layer in Current Java App
```java
// Add CORS and JSON API endpoints
public class ApiController {
    
    @GetMapping("/api/patients")
    public String getPatients(Request req, Response res) {
        res.type("application/json");
        res.header("Access-Control-Allow-Origin", "https://your-frontend.vercel.app");
        
        List<Patient> patients = patientService.getAllPatients();
        return gson.toJson(patients);
    }
    
    @PostMapping("/api/auth/login")
    public String login(Request req, Response res) {
        res.type("application/json");
        res.header("Access-Control-Allow-Origin", "https://your-frontend.vercel.app");
        
        // Existing auth logic
        return gson.toJson(authResponse);
    }
}
```

### 2. Create React Frontend
```typescript
// services/api.ts
const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';

export const patientService = {
  async getPatients() {
    const response = await fetch(`${API_BASE}/api/patients`);
    return response.json();
  },
  
  async login(credentials: LoginCredentials) {
    const response = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    return response.json();
  }
};
```

### 3. Deployment Configuration
```dockerfile
# Dockerfile for Java backend
FROM openjdk:11-jre-slim
COPY target/hms-1.22.3-jar-with-dependencies.jar app.jar
COPY .ini .ini
EXPOSE 8000
CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "--add-opens", "java.base/java.net=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED", "-jar", "app.jar"]
```

## Benefits
- **Minimal Backend Changes**: Keep existing business logic
- **Modern Frontend**: React/Next.js with modern UX
- **Gradual Migration**: Can modernize piece by piece
- **Cost Effective**: Less rewrite effort
```

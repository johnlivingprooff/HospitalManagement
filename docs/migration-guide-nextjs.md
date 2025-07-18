# HMS Migration to Next.js - Step by Step Guide

## Phase 1: Setup Modern Development Environment

### 1. Initialize Next.js Project
```bash
npx create-next-app@latest hms-modern --typescript --tailwind --app --import-alias "@/*"
cd hms-modern
npm install @prisma/client prisma bcryptjs jsonwebtoken
npm install -D @types/bcryptjs @types/jsonwebtoken
```

### 2. Database Setup (Prisma)
```bash
npx prisma init
```

### 3. Environment Configuration
```env
# .env.local
DATABASE_URL="postgresql://username:password@localhost:5432/hms"
NEXTAUTH_SECRET="your-secret-key"
NEXTAUTH_URL="http://localhost:3000"
```

## Phase 2: Database Migration

### 1. Export Current Schema
```sql
-- Run this in your current PostgreSQL
pg_dump -h localhost -U postgres -d hms --schema-only > schema.sql
```

### 2. Create Prisma Schema
```prisma
// prisma/schema.prisma
generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

model User {
  id          Int      @id @default(autoincrement())
  email       String   @unique
  password    String
  firstName   String
  lastName    String
  role        String
  isActive    Boolean  @default(true)
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt
  
  @@map("users")
}

model Patient {
  id          Int      @id @default(autoincrement())
  firstName   String
  lastName    String
  email       String?
  phone       String?
  dateOfBirth DateTime?
  address     String?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt
  
  appointments Appointment[]
  
  @@map("patients")
}

model Appointment {
  id          Int      @id @default(autoincrement())
  patientId   Int
  doctorId    Int
  date        DateTime
  time        String
  status      String
  notes       String?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt
  
  patient     Patient  @relation(fields: [patientId], references: [id])
  doctor      User     @relation(fields: [doctorId], references: [id])
  
  @@map("appointments")
}
```

## Phase 3: Authentication System

### 1. Auth Configuration
```typescript
// lib/auth.ts
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

export async function hashPassword(password: string): Promise<string> {
  return await bcrypt.hash(password, 12);
}

export async function verifyPassword(password: string, hashedPassword: string): Promise<boolean> {
  return await bcrypt.compare(password, hashedPassword);
}

export function generateToken(userId: number): string {
  return jwt.sign({ userId }, process.env.JWT_SECRET!, { expiresIn: '7d' });
}
```

### 2. Login API Route
```typescript
// app/api/auth/login/route.ts
import { NextRequest, NextResponse } from 'next/server';
import { PrismaClient } from '@prisma/client';
import { verifyPassword, generateToken } from '@/lib/auth';

const prisma = new PrismaClient();

export async function POST(request: NextRequest) {
  try {
    const { email, password } = await request.json();
    
    const user = await prisma.user.findUnique({
      where: { email },
    });
    
    if (!user || !await verifyPassword(password, user.password)) {
      return NextResponse.json({ error: 'Invalid credentials' }, { status: 401 });
    }
    
    const token = generateToken(user.id);
    
    return NextResponse.json({
      token,
      user: {
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      },
    });
  } catch (error) {
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
```

## Phase 4: Frontend Components

### 1. Login Component
```typescript
// app/login/page.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        router.push('/dashboard');
      } else {
        alert('Invalid credentials');
      }
    } catch (error) {
      alert('Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            HMS Login
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email"
              className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>
          <div>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
      </div>
    </div>
  );
}
```

## Phase 5: Deployment

### 1. Database (Supabase)
```bash
# Create Supabase project
# Copy connection string to .env.local
npx prisma db push
npx prisma generate
```

### 2. Frontend (Vercel)
```bash
# Deploy to Vercel
npm run build
vercel --prod
```

### 3. Environment Variables in Vercel
```
DATABASE_URL=postgresql://...
NEXTAUTH_SECRET=your-secret
JWT_SECRET=your-jwt-secret
```

## Benefits of This Approach

1. **Modern Stack**: Latest Next.js, TypeScript, Tailwind CSS
2. **Zero Config Deployment**: Vercel handles everything
3. **Serverless**: Scales automatically
4. **Full Stack**: Frontend + API in one project
5. **Type Safety**: End-to-end TypeScript
6. **Performance**: Server-side rendering + static generation
7. **Developer Experience**: Hot reload, great debugging

## Migration Timeline

- **Week 1**: Setup Next.js, migrate database schema
- **Week 2**: Implement authentication system
- **Week 3**: Build core components (patients, appointments)
- **Week 4**: Deploy and test production environment
- **Week 5**: Data migration and final testing

This approach gives you a modern, scalable, and easily deployable Hospital Management System that can compete with any modern web application.
```

# HMS Native PostgreSQL Setup (PowerShell Version)
# This script sets up PostgreSQL natively on Windows without Docker

Write-Host "🏥 HMS Local Database Setup (Native PostgreSQL)" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔍 This script will help you set up PostgreSQL natively on Windows" -ForegroundColor Yellow
Write-Host "without using Docker. This is useful if:" -ForegroundColor Yellow
Write-Host "  1. You don't have Docker installed" -ForegroundColor White
Write-Host "  2. You prefer native database installation" -ForegroundColor White
Write-Host "  3. You want better performance for development" -ForegroundColor White
Write-Host ""

function Test-PostgreSQLInstallation {
    try {
        $version = & psql --version 2>$null
        if ($version) {
            Write-Host "✅ PostgreSQL is installed: $version" -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Host "❌ PostgreSQL is not installed or not in PATH" -ForegroundColor Red
        return $false
    }
    return $false
}

function Install-PostgreSQL {
    Write-Host ""
    Write-Host "📥 INSTALLATION OPTIONS:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Option 1 - Download Installer:" -ForegroundColor White
    Write-Host "  1. Go to: https://www.postgresql.org/download/windows/" -ForegroundColor Gray
    Write-Host "  2. Download PostgreSQL 15 or later" -ForegroundColor Gray
    Write-Host "  3. Run installer and follow setup wizard" -ForegroundColor Gray
    Write-Host "  4. Remember the password you set for 'postgres' user" -ForegroundColor Gray
    Write-Host "  5. Make sure to add PostgreSQL to PATH" -ForegroundColor Gray
    Write-Host ""
    
    # Check if winget is available
    try {
        $wingetVersion = & winget --version 2>$null
        Write-Host "Option 2 - Using Winget (Windows Package Manager):" -ForegroundColor White
        Write-Host "  winget install PostgreSQL.PostgreSQL" -ForegroundColor Gray
        Write-Host ""
        
        $installChoice = Read-Host "Would you like to install with Winget? [y/N]"
        if ($installChoice -eq 'y' -or $installChoice -eq 'Y') {
            Write-Host "🚀 Installing PostgreSQL with Winget..." -ForegroundColor Blue
            try {
                & winget install PostgreSQL.PostgreSQL
                Write-Host "✅ Installation completed! Please restart PowerShell and run this script again." -ForegroundColor Green
                Read-Host "Press Enter to exit"
                exit
            }
            catch {
                Write-Host "❌ Installation failed. Please install manually." -ForegroundColor Red
                Write-Host "Error: $_" -ForegroundColor Red
            }
        }
    }
    catch {
        Write-Host "Option 2 - Winget not available" -ForegroundColor Gray
    }
    
    # Check if chocolatey is available
    try {
        $chocoVersion = & choco --version 2>$null
        Write-Host "Option 3 - Using Chocolatey:" -ForegroundColor White
        Write-Host "  choco install postgresql" -ForegroundColor Gray
        Write-Host ""
    }
    catch {
        Write-Host "Option 3 - Chocolatey not available" -ForegroundColor Gray
    }
    
    Write-Host "⚠️  Please install PostgreSQL manually and run this script again." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit
}

function Setup-HMSDatabase {
    Write-Host ""
    Write-Host "🗄️  Setting up HMS Database..." -ForegroundColor Blue
    Write-Host ""
    
    # Get postgres password
    $pgPassword = Read-Host "Enter the password for PostgreSQL 'postgres' user (default: postgres)"
    if (-not $pgPassword) { $pgPassword = "postgres" }
    
    Write-Host ""
    Write-Host "🔍 Testing PostgreSQL connection..." -ForegroundColor Blue
    
    # Test connection
    $env:PGPASSWORD = $pgPassword
    try {
        $testResult = & psql -h localhost -U postgres -d postgres -c "SELECT version();" 2>$null
        if (-not $testResult) {
            throw "Connection failed"
        }
    }
    catch {
        Write-Host "❌ Could not connect to PostgreSQL" -ForegroundColor Red
        Write-Host ""
        Write-Host "💡 Troubleshooting:" -ForegroundColor Yellow
        Write-Host "  1. Make sure PostgreSQL service is running" -ForegroundColor White
        Write-Host "  2. Check if the password is correct" -ForegroundColor White
        Write-Host "  3. Verify PostgreSQL is listening on port 5432" -ForegroundColor White
        Write-Host ""
        Write-Host "To start PostgreSQL service:" -ForegroundColor White
        Write-Host "  net start postgresql-x64-15  (or similar service name)" -ForegroundColor Gray
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit
    }
    
    Write-Host "✅ PostgreSQL connection successful!" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "🏗️  Creating HMS database and user..." -ForegroundColor Blue
    
    # Create database and user
    $sqlCommands = @"
DROP DATABASE IF EXISTS hms;
DROP USER IF EXISTS hms_user;
CREATE DATABASE hms;
CREATE USER hms_user WITH PASSWORD 'hms_password_123';
GRANT ALL PRIVILEGES ON DATABASE hms TO hms_user;
ALTER USER hms_user CREATEDB;
"@
    
    try {
        $sqlCommands | & psql -h localhost -U postgres -d postgres 2>$null
        Write-Host "✅ HMS database created successfully!" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Failed to create HMS database" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit
    }
    
    Write-Host ""
    Write-Host "📋 Creating local environment configuration..." -ForegroundColor Blue
    
    # Create .env.native file
    $envContent = @"
# HMS Environment Configuration - Native PostgreSQL
# Local native PostgreSQL database
DATABASE_URL=postgresql://hms_user:hms_password_123@localhost:5432/hms

# Security
SECRET_KEY=your-super-secure-secret-key-here-generate-with-openssl-rand-hex-32

# Development settings
DEBUG=true
HOST=0.0.0.0
PORT=8000

# Redis Configuration (optional - comment out if not using Redis)
# REDIS_URL=redis://localhost:6379
CACHE_TTL=300
SEARCH_CACHE_TTL=300
STATS_CACHE_TTL=600

# CORS (add your frontend URLs)
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost

# Email (optional - for notifications)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=noreply@hospital.com
"@
    
    $envContent | Out-File -FilePath ".env.native" -Encoding utf8
    Copy-Item ".env.native" ".env" -Force
    
    Write-Host ""
    Write-Host "✅ Native PostgreSQL setup complete!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 Database Details:" -ForegroundColor Cyan
    Write-Host "   Host: localhost" -ForegroundColor White
    Write-Host "   Port: 5432" -ForegroundColor White
    Write-Host "   Database: hms" -ForegroundColor White
    Write-Host "   Username: hms_user" -ForegroundColor White
    Write-Host "   Password: hms_password_123" -ForegroundColor White
    Write-Host "   Connection: postgresql://hms_user:hms_password_123@localhost:5432/hms" -ForegroundColor White
    Write-Host ""
    Write-Host "🔧 PostgreSQL Service Commands:" -ForegroundColor Cyan
    Write-Host "   Start service:  net start postgresql-x64-15" -ForegroundColor White
    Write-Host "   Stop service:   net stop postgresql-x64-15" -ForegroundColor White
    Write-Host "   Status check:   sc query postgresql-x64-15" -ForegroundColor White
    Write-Host ""
    Write-Host "🚀 Now you can run your HMS application:" -ForegroundColor Green
    Write-Host "   python -m uvicorn app.main:app --reload" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 Useful PostgreSQL commands:" -ForegroundColor Cyan
    Write-Host "   Connect to DB:  psql -h localhost -U hms_user -d hms" -ForegroundColor White
    Write-Host "   List databases: psql -h localhost -U postgres -l" -ForegroundColor White
    Write-Host "   Backup DB:      pg_dump -h localhost -U hms_user hms > backup.sql" -ForegroundColor White
    Write-Host ""
}

# Main execution
Write-Host "🔍 Checking if PostgreSQL is installed..." -ForegroundColor Blue

if (Test-PostgreSQLInstallation) {
    Setup-HMSDatabase
} else {
    Install-PostgreSQL
}

Write-Host "👋 HMS Native PostgreSQL Setup Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Configuration files created:" -ForegroundColor Cyan
Write-Host "   .env.native - Native PostgreSQL configuration" -ForegroundColor White
Write-Host "   .env        - Active environment file" -ForegroundColor White
Write-Host ""
Write-Host "🔄 To switch between configurations:" -ForegroundColor Cyan
Write-Host "   For native:  Copy-Item '.env.native' '.env'" -ForegroundColor White
Write-Host "   For docker:  Copy-Item '.env.local' '.env'" -ForegroundColor White
Write-Host "   For cloud:   Copy-Item '.env.cloud' '.env' (if exists)" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"

# Legacy Java HMS - Hospital Management System

## 📁 **Legacy Java Implementation**

This folder contains the original Java-based Hospital Management System that was built using:

- **Framework**: Spark Java (lightweight web framework)
- **Database**: PostgreSQL with raw SQL queries  
- **Templates**: Twig templating engine
- **Build System**: Maven
- **JVM Version**: Java 11+ (with module system compatibility flags)

## 🗂️ **Folder Structure**

```
legacy-java-hms/
├── src/                          # Java source code
│   ├── main/
│   │   ├── java/
│   │   │   ├── app/              # Main application code
│   │   │   │   ├── controllers/  # HTTP request handlers
│   │   │   │   ├── models/       # Data models
│   │   │   │   ├── services/     # Business logic
│   │   │   │   ├── daos/         # Data access objects
│   │   │   │   └── util/         # Utility classes
│   │   │   └── validators/       # Input validation
│   │   └── resources/
│   │       ├── templates/        # Twig HTML templates
│   │       ├── public/           # Static assets (CSS, JS, images)
│   │       └── schemas/          # JSON validation schemas
│   └── test/                     # Unit tests
├── target/                       # Maven build output
│   └── hms-1.22.3-jar-with-dependencies.jar  # Executable JAR
├── localdata/                    # Local application data
│   ├── attachments/              # File uploads
│   ├── profile-images/           # User profile pictures
│   ├── logs/                     # Application logs
│   └── email-trap/               # Email testing
├── .vscode/                      # VS Code configuration
├── pom.xml                       # Maven project configuration
├── dependency-reduced-pom.xml    # Maven shade plugin output
├── .ini                          # Application configuration
├── fakeSMTP-2.0.jar             # Email testing server
├── create_admin.py              # Admin user creation script
├── verify_admin.py              # Admin password verification
└── view_users.py                # Database user viewing script
```

## ⚙️ **How to Run (Historical Reference)**

### **Prerequisites**
- Java 11 or higher
- PostgreSQL database
- Maven 3.6+

### **Configuration**
1. Update `.ini` file with database credentials
2. Ensure PostgreSQL is running
3. Create database named `hms`

### **Build and Run**
```bash
# Build the project
mvn clean install
mvn package -DskipTests

# Run the application
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.io=ALL-UNNAMED \
     -jar target/hms-1.22.3-jar-with-dependencies.jar
```

### **Access**
- **URL**: http://localhost:8000
- **Login**: Navigate to `/Auth` endpoint
- **Admin**: admin@hms.local / admin123

## 🔄 **Migration Status**

This legacy code has been **successfully migrated** to a modern stack:

### **From Java → Python + React**
- **Backend**: Spark Java → FastAPI
- **Frontend**: Server-side templates → React + TypeScript
- **Database**: Raw SQL → SQLAlchemy ORM
- **Authentication**: Session-based → JWT tokens
- **Deployment**: Traditional server → Cloud-native (Render + Netlify)

### **Key Improvements in New Version**
- ⚡ **10x faster deployment** (seconds vs minutes)
- 🌍 **Global CDN** for worldwide access
- 📱 **Mobile-responsive** design
- 🔄 **Auto-scaling** based on demand
- 🔒 **Modern security** with JWT
- 🎨 **Modern UI/UX** with Tailwind CSS

## 📊 **Data Compatibility**

The new FastAPI system is **fully compatible** with the existing PostgreSQL database:

- ✅ **Same database schema**
- ✅ **Compatible password hashing** (BCrypt)
- ✅ **Same user accounts** work in both systems
- ✅ **Data migration scripts** available

## 🚀 **Recommended Action**

**Use the new modern system** in `../hms-fastapi/` for:
- New deployments
- Production environments
- Mobile access
- Modern development

**Keep this legacy code** for:
- Historical reference
- Data migration
- Understanding business logic
- Emergency fallback (if needed)

---

**Note**: This legacy implementation served well but has been replaced by a modern, cloud-native solution that offers better performance, security, and maintainability.

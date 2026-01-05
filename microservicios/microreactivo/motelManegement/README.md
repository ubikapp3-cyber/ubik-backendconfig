# Motel Management Service

A reactive microservice for motel management using Spring Boot 3.5.3, Spring WebFlux, and R2DBC with PostgreSQL.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+ (or use Docker Compose)

## 🚀 Quick Start

### For Azure Database for PostgreSQL

If you're deploying to Azure, we have a dedicated guide and automated scripts:

**📘 [Azure PostgreSQL Setup Guide →](README-AZURE.md)**

Quick setup:
```bash
# Interactive configuration wizard
./azure-quickstart.sh

# Or manual setup
cp .env.azure.template .env.azure
# Edit .env.azure with your Azure credentials
source .env.azure
./init-postgresql-azure.sh
```

### For Local Development

Continue reading below for local PostgreSQL setup with Docker.

## Running the Application

### Option 1: Using Maven (Recommended)

```bash
./mvnw spring-boot:run
```

### Option 2: Using Docker Compose

Start PostgreSQL and the application:

```bash
./start-database.sh
./mvnw spring-boot:run
```

Or use the full Docker Compose setup:

```bash
docker-compose up
```

### Option 3: Running from IntelliJ IDEA

If you encounter errors like "Failed to configure a ConnectionFactory", follow these steps:

1. **Import the project** as a Maven project (File -> Open -> select pom.xml)
2. **Reload Maven project**: Right-click on pom.xml -> Maven -> Reload project
3. **Build the project**: Build -> Build Project (Ctrl+F9 / Cmd+F9)
4. **Invalidate caches** if needed: File -> Invalidate Caches / Restart
5. **Run the application**:
   - Right-click on `MotelManagementApplication.java`
   - Select "Run 'MotelManagementApplication'"
   
Alternatively, use the Maven tool window and run the `spring-boot:run` goal.

## Configuration

The application uses `application.yml` for configuration. Key properties:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/motel_management_db
    username: postgres
    password: carlosmanuel
```

### Environment Variables

For production or to override default values, use environment variables:

- `R2DBC_URL`: R2DBC connection URL (default: r2dbc:postgresql://localhost:5432/motel_management_db)
- `R2DBC_USERNAME`: Database username (default: postgres)
- `R2DBC_PASSWORD`: Database password (default: carlosmanuel)

Example:
```bash
export R2DBC_URL=r2dbc:postgresql://prod-server:5432/motel_db
export R2DBC_USERNAME=myuser
export R2DBC_PASSWORD=mypassword
./mvnw spring-boot:run
```

### Profiles

- **default**: Uses local PostgreSQL (localhost:5432)
- **docker**: Uses PostgreSQL in Docker container (postgres:5432)
- **dev**: Development profile with debug logging and environment variable support
- **azure**: Azure Database for PostgreSQL with SSL support (see [README-AZURE.md](README-AZURE.md))
- **test**: Uses H2 in-memory database for testing

To activate a profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or in IntelliJ: Run -> Edit Configurations -> Add `-Dspring.profiles.active=dev` to VM options

For Azure deployment:
```bash
# Load Azure environment variables
source .env.azure

# Run with Azure profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=azure
```

## Database Setup

### Automatic Initialization

The application automatically creates the required schema on startup using the SQL script:
`src/main/resources/Postgres-init-motel.sql`

### Manual Setup

If you prefer to set up the database manually:

```bash
createdb motel_management_db
psql motel_management_db < src/main/resources/Postgres-init-motel.sql
```

## Testing

Run tests with:

```bash
./mvnw test
```

Tests use an H2 in-memory database configured in `src/test/resources/application-test.yml`.

## Troubleshooting

### "Failed to configure a ConnectionFactory" Error

This error occurs when Spring Boot cannot find the R2DBC configuration. Common causes:

1. **Maven project not imported properly in IDE**
   - Solution: Reload Maven project and rebuild

2. **Resources not copied to target/classes**
   - Solution: Run `mvn clean compile` or Build -> Build Project in IntelliJ

3. **PostgreSQL not running**
   - Solution: Start PostgreSQL using `./start-database.sh` or Docker

4. **Wrong working directory**
   - Solution: Ensure you're running from the `motelManegement` directory

### Connection Refused

If PostgreSQL connection is refused:

1. Verify PostgreSQL is running: `pg_isready`
2. Check the connection settings in `application.yml`
3. Ensure PostgreSQL is accepting connections on port 5432

## API Endpoints

Once running, the application is available at: `http://localhost:8084`

### Main Features

#### 1. Motel Management
- CRUD operations for motels
- Search by city
- Image management

#### 2. Room Management
- CRUD operations for rooms
- Availability tracking
- Room types and pricing

#### 3. Reservation System
- Create and manage reservations
- Check-in/check-out operations
- Status tracking (PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED)
- Availability verification

#### 4. Services
- Manage motel services
- Associate services with rooms

#### 5. **NEW: Chatbot Assistant** 🤖
- Intelligent virtual assistant for querying information
- Natural language conversation support
- **User Features:**
  - Query personal reservations
  - Check available rooms
  - Get motel information
- **Admin Features:**
  - View system-wide reservations
  - Get guidance for creating rooms
  - Access admin management commands
- **Security:**
  - Role-based access control
  - Data filtering to protect confidential information
  - Context-based authorization

**Chatbot API Documentation**: See [docs/CHATBOT_API.md](./docs/CHATBOT_API.md) for detailed chatbot API usage.

### Swagger Documentation

API documentation is available through Swagger UI:
- **Swagger UI**: `http://localhost:8084/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8084/v3/api-docs`

The Swagger interface provides interactive documentation where you can:
- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas
- Understand required parameters and data types

### Key Endpoints

#### Chatbot
- `POST /api/chatbot/sessions` - Create chat session
- `POST /api/chatbot/message` - Send message to chatbot
- `GET /api/chatbot/sessions/{sessionId}/history` - Get chat history
- `PATCH /api/chatbot/sessions/{sessionId}/close` - Close session
- `DELETE /api/chatbot/sessions/{sessionId}` - Delete session

#### Reservations
- `POST /api/reservations` - Create reservation
- `GET /api/reservations/user/{userId}` - Get user reservations
- `PATCH /api/reservations/{id}/confirm` - Confirm reservation

#### Rooms
- `GET /api/rooms/motel/{motelId}/available` - Get available rooms
- `POST /api/rooms` - Create room

#### Motels
- `GET /api/motels` - List all motels
- `GET /api/motels/city/{city}` - Search by city

### Actuator Endpoints

- Health: `http://localhost:8084/actuator/health`
- Info: `http://localhost:8084/actuator/info`

## Project Structure

```
src/main/java/com/ubik/usermanagement/
├── domain/              # Domain models and services
│   ├── model/          # Domain entities (Motel, Room, Reservation, ChatSession, ChatMessage)
│   ├── port/           # Port interfaces (in/out)
│   └── service/        # Business logic services (including ChatbotService)
├── infrastructure/      # Infrastructure layer
│   ├── adapter/        # Adapters (in/out)
│   │   ├── in/web/    # REST controllers, DTOs, mappers
│   │   └── out/       # Persistence adapters, repositories
│   └── config/         # Configuration classes
└── MotelManagementApplication.java
```

## License

[Add your license here]

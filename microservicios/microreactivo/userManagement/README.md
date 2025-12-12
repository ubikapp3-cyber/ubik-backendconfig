# User Management Service

A reactive microservice for user management and authentication using Spring Boot 3.5.3, Spring WebFlux, Spring Security, and R2DBC with PostgreSQL.

## Features

- User registration and authentication
- JWT token-based authentication
- Password reset functionality
- User profile management
- Reactive security with Spring Security

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+

## Running the Application

### Using Maven

```bash
./mvnw spring-boot:run
```

## Configuration

The application uses `application.yml` for configuration. Key properties:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/userManagement_db
    username: postgres
    password: "12345"
```

## API Endpoints

Once running, the application is available at: `http://localhost:8081`

### Swagger Documentation

API documentation is available through Swagger UI:
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/api-docs`

The Swagger interface provides interactive documentation where you can:
- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas
- Understand authentication requirements
- Test JWT authentication

### Main Endpoints

#### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/reset-password-request` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token

#### User Profile (Requires JWT)
- `GET /api/user` - Get user profile
- `PUT /api/user` - Update user profile

## Authentication

This service uses JWT (JSON Web Tokens) for authentication:

1. Register a new user or login using `/api/auth/login`
2. Copy the JWT token from the response
3. Use the token in the `Authorization` header: `Bearer <your-token>`

In Swagger UI:
1. Click the "Authorize" button at the top
2. Enter your JWT token (without "Bearer" prefix)
3. Click "Authorize"
4. Now you can test protected endpoints

## Project Structure

```
src/main/java/com/ubik/usermanagement/
├── application/         # Application layer (use cases)
├── domain/             # Domain models and business logic
├── infrastructure/     # Infrastructure layer
│   ├── adapter/       # Adapters (in/out)
│   └── config/        # Configuration classes
└── UserManagementApplication.java
```

## Security

The service implements:
- BCrypt password hashing (strength: 12)
- JWT token generation and validation
- Reactive security context
- Role-based access control (ADMIN, USER, CLIENT)

## Testing

Run tests with:

```bash
./mvnw test
```

## License

[Add your license here]

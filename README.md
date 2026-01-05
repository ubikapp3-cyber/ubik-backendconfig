# Ubik Backend Configuration

Sistema de gestión de moteles y reservaciones construido con microservicios reactivos usando Spring Boot 3, Spring WebFlux y PostgreSQL.

## 📚 Documentación de API

Para integración con el frontend, consulta:

- **[RESUMEN_API.md](./RESUMEN_API.md)** - Guía rápida con endpoints principales y ejemplos de código
- **[API_INTEGRATION.md](./API_INTEGRATION.md)** - Documentación completa y detallada de todos los endpoints

## 🏗️ Arquitectura

```
Frontend → API Gateway (puerto 8080)
              ↓
              ├─→ User Management Service (puerto 8081)
              │   ├─ Autenticación (JWT)
              │   ├─ Gestión de usuarios
              │   └─ Perfiles de usuario
              │
              └─→ Motel Management Service (puerto 8084)
                  ├─ Gestión de moteles
                  ├─ Gestión de habitaciones
                  ├─ Gestión de reservaciones
                  └─ Gestión de servicios
```

## 🚀 Servicios

### API Gateway
- **Puerto**: 8080
- **Descripción**: Punto de entrada centralizado para todas las peticiones
- **Características**: Routing, CORS, autenticación JWT

### User Management Service
- **Puerto**: 8081
- **Descripción**: Gestión de usuarios y autenticación
- **Documentación**: [microservicios/microreactivo/userManagement/README.md](./microservicios/microreactivo/userManagement/README.md)
- **Swagger**: http://localhost:8081/swagger-ui.html

### Motel Management Service
- **Puerto**: 8084
- **Descripción**: Gestión de moteles, habitaciones, reservaciones y servicios
- **Documentación**: [microservicios/microreactivo/motelManegement/README.md](./microservicios/microreactivo/motelManegement/README.md)
- **Swagger**: http://localhost:8084/swagger-ui.html

## 🔧 Tecnologías

- **Java**: 17+
- **Spring Boot**: 3.5.3
- **Spring WebFlux**: Programación reactiva
- **Spring Cloud Gateway**: API Gateway
- **Spring Security**: Autenticación y autorización
- **R2DBC**: Acceso reactivo a base de datos
- **PostgreSQL**: Base de datos
- **JWT**: Autenticación basada en tokens
- **OpenAPI/Swagger**: Documentación de API

## 📋 Prerequisitos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL 15+
- Node.js 18+ (para el frontend)

## 🏃 Inicio Rápido

### 1. Iniciar Base de Datos

```bash
# PostgreSQL debe estar corriendo en el puerto 5432
# Crear las bases de datos necesarias:
createdb userManagement_db
createdb motel_management_db
```

### 2. Iniciar Servicios

```bash
# Terminal 1 - User Management Service
cd microservicios/microreactivo/userManagement
./mvnw spring-boot:run

# Terminal 2 - Motel Management Service
cd microservicios/microreactivo/motelManegement
./mvnw spring-boot:run

# Terminal 3 - API Gateway
cd microservicios/microreactivo/gateway
./mvnw spring-boot:run
```

### 3. Iniciar Frontend (opcional)

```bash
cd frontend
npm install
npm start
```

## 🔐 Autenticación

El sistema utiliza JWT para autenticación:

1. Registrar usuario: `POST /api/auth/register`
2. Iniciar sesión: `POST /api/auth/login` (retorna token JWT)
3. Usar token en headers: `Authorization: Bearer <token>`

## 📊 Swagger UI

Cada servicio expone su documentación interactiva:

- **Gateway**: http://localhost:8080/swagger-ui.html
- **User Management**: http://localhost:8081/swagger-ui.html
- **Motel Management**: http://localhost:8084/swagger-ui.html

## 📖 Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión

### Moteles
- `GET /api/motels` - Listar moteles
- `GET /api/motels/city/{city}` - Buscar por ciudad
- `POST /api/motels` - Crear motel

### Habitaciones
- `GET /api/rooms/motel/{motelId}/available` - Habitaciones disponibles
- `POST /api/rooms` - Crear habitación

### Reservaciones
- `POST /api/reservations` - Crear reservación
- `GET /api/reservations/user/{userId}` - Reservaciones de usuario
- `PATCH /api/reservations/{id}/confirm` - Confirmar reservación

Ver documentación completa en [API_INTEGRATION.md](./API_INTEGRATION.md)

## 🧪 Testing

```bash
# User Management Service
cd microservicios/microreactivo/userManagement
./mvnw test

# Motel Management Service
cd microservicios/microreactivo/motelManegement
./mvnw test
```

## 📁 Estructura del Proyecto

```
ubik-backendconfig/
├── API_INTEGRATION.md         # Documentación completa de API
├── RESUMEN_API.md            # Guía rápida de API
├── microservicios/
│   └── microreactivo/
│       ├── gateway/          # API Gateway
│       ├── userManagement/   # Servicio de usuarios
│       ├── motelManegement/  # Servicio de moteles
│       └── products/         # Servicio de productos (ejemplo)
└── frontend/                 # Aplicación Angular
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📝 Licencia

[Agregar licencia aquí]

## 👥 Contacto

Para más información o soporte, consulta la documentación de cada servicio o las APIs de Swagger.

---

**Última actualización**: Diciembre 2024

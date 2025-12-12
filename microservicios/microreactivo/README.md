# Microservicios Reactivos Ubik - 2025

## 📋 Descripción

Plataforma de microservicios reactivos construida con **Spring Boot 3**, **Spring WebFlux**, **R2DBC** y **PostgreSQL/MySQL**. La arquitectura incluye un API Gateway, gestión de usuarios con autenticación JWT, y un sistema completo de gestión de moteles con reservas.

## 🚀 Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    🌐 Clientes                              │
│              (Web / Mobile / Desktop)                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              🚪 API Gateway (:8080)                         │
│     - Enrutamiento Inteligente                              │
│     - Autenticación JWT                                     │
│     - Autorización por Roles                                │
│     - CORS Configuration                                    │
└────┬──────────────┬──────────────┬─────────────────────────┘
     │              │              │
     ▼              ▼              ▼
┌─────────┐   ┌─────────┐   ┌─────────────┐
│ 👤 User │   │ 🏨 Motel │   │ 📦 Products │
│  Mgmt   │   │   Mgmt   │   │   Service   │
│  :8081  │   │  :8084   │   │    :8082    │
└─────────┘   └─────────┘   └─────────────┘
     │              │              │
     ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│PostgreSQL│  │PostgreSQL│  │  MySQL   │
│   Users  │  │  Motels  │  │ Products │
└──────────┘  └──────────┘  └──────────┘
```

## 🏗️ Microservicios

### 1. 🚪 API Gateway (:8080)
**Punto de entrada único para toda la plataforma**

- Enrutamiento dinámico a microservicios
- Validación de JWT tokens
- Autorización basada en roles
- Manejo de CORS
- Request/Response logging
- Load balancing (preparado para escalar)

📖 **Documentación:**
- [README Completo](./gateway/README.md)
- [Edge Cases](./gateway/EDGE_CASES.md)
- [Swagger UI](http://localhost:8080/swagger-ui.html) (cuando está corriendo)

### 2. 👤 User Management (:8081)
**Gestión de usuarios y autenticación JWT**

**Características:**
- Registro de usuarios con validación
- Login con generación de JWT
- Gestión de perfiles de usuario
- Reseteo de contraseña con tokens
- Roles: CLIENT, ADMIN, OWNER
- Encriptación BCrypt de contraseñas
- Arquitectura Hexagonal

📖 **Documentación:**
- [README Completo](./userManagement/README.md)
- [Edge Cases](./userManagement/EDGE_CASES.md)
- [Swagger UI](http://localhost:8081/swagger-ui.html) (cuando está corriendo)
- [OpenAPI Spec](http://localhost:8081/v3/api-docs)

**Endpoints Principales:**
```
POST   /api/auth/register          - Registrar usuario
POST   /api/auth/login             - Autenticar usuario
GET    /api/user                   - Obtener perfil
PUT    /api/user                   - Actualizar perfil
POST   /api/auth/reset-password-request
POST   /api/auth/reset-password
```

### 3. 🏨 Motel Management (:8084)
**Gestión completa de moteles, habitaciones, servicios y reservas**

**Características:**
- CRUD de moteles con imágenes
- Gestión de habitaciones con tipos y precios
- Sistema de servicios adicionales
- Reservas con prevención de double-booking
- Verificación de disponibilidad
- Arquitectura Hexagonal
- Sistema de imágenes múltiples

📖 **Documentación:**
- [README API Completo](./motelManegement/README_API.md)
- [Edge Cases](./motelManegement/EDGE_CASES.md)
- [Swagger UI](http://localhost:8084/swagger-ui.html) (cuando está corriendo)
- [OpenAPI Spec](http://localhost:8084/v3/api-docs)

**Endpoints Principales:**
```
GET    /api/motels                 - Listar moteles
GET    /api/motels/city/{city}     - Buscar por ciudad
POST   /api/motels                 - Crear motel
GET    /api/rooms/motel/{id}/available
POST   /api/reservations           - Crear reserva
GET    /api/services               - Listar servicios
```

### 4. 📦 Products (:8082)
**Servicio de productos (para referencia)**

*Nota: Este módulo no está incluido en la documentación según requerimientos*

## 📚 Documentación Completa

### 📊 Diagramas de Flujo
**[Ver Todos los Diagramas Mermaid](./MERMAID_DIAGRAMS.md)**

Incluye:
- Arquitectura general del sistema
- Flujo de autenticación completo
- Flujo de búsqueda y reserva de motel
- Flujo de actualización de perfil
- Flujo de reseteo de contraseña
- Arquitectura hexagonal
- Flujo de manejo de errores
- Prevención de double-booking
- Diagrama de estados de reserva
- Gateway request processing
- Escalabilidad con múltiples instancias
- Diagrama de despliegue

### 🧪 Edge Cases Documentados

Cada microservicio tiene documentación completa de casos borde:

1. **[Gateway Edge Cases](./gateway/EDGE_CASES.md)** - 93 casos cubiertos
   - JWT validation
   - Routing y path matching
   - CORS handling
   - Service communication
   - Authorization

2. **[User Management Edge Cases](./userManagement/EDGE_CASES.md)** - 53 casos cubiertos
   - Autenticación y registro
   - JWT tokens
   - Gestión de perfil
   - Reseteo de contraseña
   - Autorización por roles

3. **[Motel Management Edge Cases](./motelManegement/EDGE_CASES.md)** - 106 casos cubiertos
   - Gestión de moteles
   - Gestión de habitaciones
   - Sistema de reservas
   - Prevención de double-booking
   - Gestión de servicios

## 🛠️ Requisitos del Sistema

### Software Necesario

- **Java 17** o superior
- **Maven 3.6+**
- **PostgreSQL 15+** (para UserManagement y MotelManagement)
- **MySQL 8+** (para Products)
- **Docker** (opcional, para contenedores)

### Puertos Utilizados

| Servicio | Puerto | Base de Datos |
|----------|--------|---------------|
| Gateway | 8080 | - |
| User Management | 8081 | PostgreSQL :5432 |
| Products | 8082 | MySQL :3306 |
| Motel Management | 8084 | PostgreSQL :5432 |

## 🚀 Inicio Rápido

### 1. Configurar Bases de Datos

```bash
# PostgreSQL - User Management
createdb userManagement_db

# PostgreSQL - Motel Management
createdb motel_management_db

# MySQL - Products
mysql -u root -p
CREATE DATABASE products_db;
```

### 2. Configurar Variables de Entorno

```bash
# JWT Configuration (mismo para todos)
export JWT_SECRET=mySecretKey1234567890abcdef1234567890abcdef
export JWT_EXPIRATION=86400000

# PostgreSQL User Management
export USER_DB_URL=r2dbc:postgresql://localhost:5432/userManagement_db
export USER_DB_USERNAME=postgres
export USER_DB_PASSWORD=12345

# PostgreSQL Motel Management
export MOTEL_DB_URL=r2dbc:postgresql://localhost:5432/motel_management_db
export MOTEL_DB_USERNAME=postgres
export MOTEL_DB_PASSWORD=carlosmanuel
```

### 3. Compilar Todos los Servicios

```bash
cd /path/to/microreactivo
mvn clean package -DskipTests
```

### 4. Ejecutar Servicios

En terminales separadas:

```bash
# Terminal 1 - User Management
cd userManagement
./mvnw spring-boot:run

# Terminal 2 - Motel Management
cd motelManegement
./mvnw spring-boot:run

# Terminal 3 - Gateway (último, depende de los otros)
cd gateway
./mvnw spring-boot:run
```

### 5. Verificar que Todo Funcione

```bash
# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8084/actuator/health

# Test funcional completo
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@1234",
    "email": "test@example.com",
    "anonymous": false,
    "roleId": 1
  }'

# 2. Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@1234"
  }')

# 3. Buscar moteles
curl -X GET http://localhost:8080/api/motels

# 4. Ver habitaciones (con JWT)
curl -X GET http://localhost:8080/api/rooms/motel/1/available \
  -H "Authorization: Bearer $TOKEN"
```

## 🐳 Docker Compose (Opcional)

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

## 📖 Swagger UI

Una vez que los servicios estén corriendo, accede a la documentación interactiva:

- **Gateway**: http://localhost:8080/swagger-ui.html
- **User Management**: http://localhost:8081/swagger-ui.html
- **Motel Management**: http://localhost:8084/swagger-ui.html

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Test de un microservicio específico
mvn -pl userManagement test
mvn -pl motelManegement test
mvn -pl gateway test
```

### Tests de Integración

```bash
mvn verify
```

## 📊 Monitoreo

### Actuator Endpoints

Todos los servicios exponen endpoints de Actuator:

```bash
# Health
curl http://localhost:8081/actuator/health

# Metrics
curl http://localhost:8081/actuator/metrics

# Gateway Routes
curl http://localhost:8080/actuator/gateway/routes
```

## 🔧 Troubleshooting

### Problema: "Connection refused" al iniciar Gateway

**Causa**: Los microservicios aún no están listos

**Solución**: Asegurar que User Management y Motel Management estén corriendo antes de iniciar el Gateway

### Problema: "JWT inválido" aunque el token es correcto

**Causa**: JWT_SECRET diferente entre servicios

**Solución**: Verificar que todos los servicios usen el mismo JWT_SECRET

### Problema: No se puede conectar a PostgreSQL

**Solución**:
```bash
# Verificar que PostgreSQL esté corriendo
pg_isready

# Verificar las bases de datos
psql -l
```

## 📈 Escalabilidad

La arquitectura soporta:

- ✅ Múltiples instancias del Gateway (load balancer)
- ✅ Múltiples instancias de cada microservicio
- ✅ Replicación de bases de datos (Primary-Replica)
- ✅ Cache distribuido (Redis)
- ✅ Service Discovery (preparado para Eureka/Consul)

Ver [Diagramas de Escalabilidad](./MERMAID_DIAGRAMS.md#11-escalabilidad---múltiples-instancias)

## 🔐 Seguridad

### Características Implementadas

- ✅ Autenticación JWT
- ✅ Autorización basada en roles
- ✅ Encriptación de contraseñas (BCrypt)
- ✅ Validación de entrada
- ✅ CORS configurado
- ✅ Headers de seguridad
- ✅ Protección contra inyección SQL (R2DBC)
- ✅ Logs sin datos sensibles

### Mejores Prácticas

1. Cambiar JWT_SECRET en producción
2. Usar HTTPS en producción
3. Configurar CORS específicamente para dominios permitidos
4. Implementar rate limiting
5. Agregar WAF (Web Application Firewall)

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Apache 2.0

## 👥 Equipo

Ubik Team - support@ubik.com

---

## 📚 Índice de Documentación

### Por Microservicio

| Microservicio | README | Edge Cases | Swagger |
|---------------|--------|------------|---------|
| Gateway | [📖](./gateway/README.md) | [⚠️](./gateway/EDGE_CASES.md) | [🔗](http://localhost:8080/swagger-ui.html) |
| User Management | [📖](./userManagement/README.md) | [⚠️](./userManagement/EDGE_CASES.md) | [🔗](http://localhost:8081/swagger-ui.html) |
| Motel Management | [📖](./motelManegement/README_API.md) | [⚠️](./motelManegement/EDGE_CASES.md) | [🔗](http://localhost:8084/swagger-ui.html) |

### Diagramas y Arquitectura

- [📊 Todos los Diagramas Mermaid](./MERMAID_DIAGRAMS.md)
- [🏗️ Arquitectura Hexagonal](./MERMAID_DIAGRAMS.md#6-arquitectura-hexagonal---motel-management)
- [🔐 Flujos de Autenticación](./MERMAID_DIAGRAMS.md#2-flujo-de-autenticación-completo)
- [🏨 Flujos de Reserva](./MERMAID_DIAGRAMS.md#3-flujo-de-búsqueda-y-reserva-de-motel)

---

**Última actualización:** Diciembre 2024 | **Versión:** 1.0.0

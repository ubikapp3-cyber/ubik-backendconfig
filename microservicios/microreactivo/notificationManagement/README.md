# Notification Management Service

Microservicio reactivo para la gestión de notificaciones del sistema utilizando arquitectura hexagonal.

## Características

- **Arquitectura Hexagonal (Ports & Adapters)**: Separación clara entre dominio, aplicación e infraestructura
- **Reactive Programming**: Uso de Spring WebFlux y R2DBC para programación reactiva
- **API REST**: Endpoints RESTful para gestionar notificaciones
- **Documentación OpenAPI**: Swagger UI disponible para explorar la API
- **PostgreSQL**: Base de datos relacional con soporte reactivo

## Estructura del Proyecto

```
notification-management/
├── domain/                          # Núcleo de negocio
│   ├── model/                       # Modelos de dominio
│   │   └── Notification.java
│   ├── port/
│   │   ├── in/                      # Puertos de entrada (use cases)
│   │   │   └── NotificationUseCasePort.java
│   │   └── out/                     # Puertos de salida (repositories)
│   │       └── NotificationRepositoryPort.java
│   └── service/                     # Servicios de dominio
│       └── NotificationService.java
└── infrastructure/                  # Adaptadores
    ├── adapter/
    │   ├── in/                      # Adaptadores de entrada
    │   │   └── web/
    │   │       ├── controller/      # Controladores REST
    │   │       ├── dto/             # DTOs
    │   │       ├── mapper/          # Mappers DTO <-> Domain
    │   │       └── exception/       # Manejo de excepciones
    │   └── out/                     # Adaptadores de salida
    │       └── persistence/
    │           ├── entity/          # Entidades JPA
    │           ├── repository/      # Repositorios R2DBC
    │           ├── mapper/          # Mappers Entity <-> Domain
    │           └── NotificationPersistenceAdapter.java
    └── config/                      # Configuración
        └── OpenApiConfig.java
```

## Tipos de Notificación

El sistema soporta diferentes tipos de notificaciones:
- **WELCOME**: Notificaciones de bienvenida
- **BOOKING**: Notificaciones de reservas
- **PAYMENT**: Notificaciones de pagos
- **SYSTEM**: Notificaciones del sistema
- **VERIFICATION**: Notificaciones de verificación
- **CUSTOM**: Notificaciones personalizadas

## Estados de Notificación

- **PENDING**: Pendiente de envío
- **SENT**: Enviada
- **READ**: Leída por el destinatario
- **FAILED**: Falló el envío
- **CANCELLED**: Cancelada

## Endpoints Principales

### Notificaciones

- `POST /api/notifications` - Crear una nueva notificación
- `GET /api/notifications` - Obtener todas las notificaciones
- `GET /api/notifications/{id}` - Obtener notificación por ID
- `GET /api/notifications/recipient/{recipient}` - Obtener notificaciones por destinatario
- `GET /api/notifications/type/{type}` - Obtener notificaciones por tipo
- `GET /api/notifications/status/{status}` - Obtener notificaciones por estado
- `POST /api/notifications/{id}/send` - Enviar notificación
- `POST /api/notifications/{id}/read` - Marcar notificación como leída
- `PUT /api/notifications/{id}` - Actualizar notificación
- `DELETE /api/notifications/{id}` - Eliminar notificación
- `GET /api/notifications/recipient/{recipient}/unread-count` - Contar notificaciones no leídas

## Configuración

### Base de Datos

El servicio requiere una base de datos PostgreSQL. La configuración se encuentra en `application.yml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/notification_management_db
    username: postgres
    password: carlosmanuel
```

### Puerto

Por defecto, el servicio se ejecuta en el puerto **8085**.

## Ejecución

### Requisitos Previos

1. JDK 17 o superior
2. Maven 3.9+
3. PostgreSQL 12+

### Crear la Base de Datos

```sql
CREATE DATABASE notification_management_db;
```

El esquema se inicializará automáticamente al arrancar la aplicación.

### Ejecutar el Servicio

Desde el directorio raíz del proyecto microreactivo:

```bash
mvn -pl notificationManagement spring-boot:run
```

O ejecutar directamente:

```bash
cd notificationManagement
mvn spring-boot:run
```

### Ejecutar con Perfil Específico

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Documentación de la API

Una vez iniciado el servicio, la documentación Swagger estará disponible en:

- Swagger UI: http://localhost:8085/swagger-ui.html
- OpenAPI JSON: http://localhost:8085/v3/api-docs

## Pruebas

```bash
mvn test
```

## Ejemplo de Uso

### Crear una Notificación

```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nueva reserva",
    "message": "Tienes una nueva reserva para mañana",
    "type": "BOOKING",
    "recipient": "user123",
    "recipientType": "USER",
    "metadata": "{\"bookingId\": 456}"
  }'
```

### Obtener Notificaciones de un Usuario

```bash
curl http://localhost:8085/api/notifications/recipient/user123
```

### Enviar una Notificación

```bash
curl -X POST http://localhost:8085/api/notifications/1/send
```

### Marcar como Leída

```bash
curl -X POST http://localhost:8085/api/notifications/1/read
```

## Arquitectura Hexagonal

Este microservicio implementa arquitectura hexagonal con las siguientes capas:

1. **Dominio** (Core): Lógica de negocio pura, independiente de frameworks
   - Models: Entidades de dominio
   - Ports: Interfaces que definen contratos
   - Services: Lógica de negocio

2. **Infraestructura**: Implementaciones técnicas
   - Web Adapters: Controllers REST
   - Persistence Adapters: Implementaciones de repositorios
   - Mappers: Conversión entre capas

## Tecnologías

- Spring Boot 3.5.3
- Spring WebFlux
- Spring Data R2DBC
- PostgreSQL
- Project Reactor
- SpringDoc OpenAPI
- Lombok
- Maven

## Licencia

Apache 2.0

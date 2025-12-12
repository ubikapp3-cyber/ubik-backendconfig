# Diagramas de Flujo - Microservicios Ubik

## 📊 Diagramas Mermaid para la Arquitectura de Microservicios

Este documento contiene todos los diagramas de flujo y secuencia que ilustran el funcionamiento de la plataforma Ubik.

---

## 🏗️ 1. Arquitectura General del Sistema

```mermaid
graph TB
    subgraph Clients["🖥️ Clientes"]
        Web[Web Browser]
        Mobile[Mobile App]
        Desktop[Desktop App]
    end

    subgraph Gateway["🚪 API Gateway :8080"]
        GW[Spring Cloud Gateway]
        JWTFilter[JWT Filter]
        AuthFilter[Authorization Filter]
        CORS[CORS Config]
    end

    subgraph UserService["👤 User Management :8081"]
        Auth[Auth Controller]
        Profile[Profile Controller]
        UserDB[(PostgreSQL Users)]
    end

    subgraph MotelService["🏨 Motel Management :8084"]
        Motels[Motel Controller]
        Rooms[Room Controller]
        Reservations[Reservation Controller]
        Services[Service Controller]
        MotelDB[(PostgreSQL Motels)]
    end

    subgraph ProductService["📦 Products :8082"]
        Products[Product Handler]
        ProductDB[(MySQL Products)]
    end

    Web --> GW
    Mobile --> GW
    Desktop --> GW
    
    GW --> JWTFilter
    JWTFilter --> AuthFilter
    AuthFilter --> CORS
    
    CORS --> Auth
    CORS --> Profile
    CORS --> Motels
    CORS --> Rooms
    CORS --> Reservations
    CORS --> Services
    CORS --> Products
    
    Auth --> UserDB
    Profile --> UserDB
    Motels --> MotelDB
    Rooms --> MotelDB
    Reservations --> MotelDB
    Services --> MotelDB
    Products --> ProductDB

    style Gateway fill:#e1f5ff
    style UserService fill:#fff4e1
    style MotelService fill:#e8f5e9
    style ProductService fill:#fce4ec
```

---

## 🔐 2. Flujo de Autenticación Completo

```mermaid
sequenceDiagram
    participant U as 👤 Usuario
    participant G as 🚪 Gateway
    participant A as 🔑 Auth Service
    participant DB as 💾 PostgreSQL

    Note over U,DB: Fase 1: Registro

    U->>G: POST /api/auth/register
    G->>A: Forward request
    A->>DB: Verificar username único
    alt Username existe
        DB-->>A: Username duplicado
        A-->>G: 400 Bad Request
        G-->>U: "Username ya existe"
    else Username disponible
        DB-->>A: OK
        A->>A: Encriptar password (BCrypt)
        A->>DB: INSERT INTO users
        DB-->>A: Usuario creado
        A-->>G: 201 Created
        G-->>U: "Usuario registrado exitosamente"
    end

    Note over U,DB: Fase 2: Login

    U->>G: POST /api/auth/login
    G->>A: Forward request
    A->>DB: SELECT user WHERE username=?
    alt Usuario no existe
        DB-->>A: No encontrado
        A-->>G: 400 Bad Request
        G-->>U: "Credenciales inválidas"
    else Usuario existe
        DB-->>A: Datos del usuario
        A->>A: Verificar password (BCrypt.matches)
        alt Password incorrecta
            A-->>G: 400 Bad Request
            G-->>U: "Credenciales inválidas"
        else Password correcta
            A->>A: Generar JWT token
            Note over A: Claims: username, role, exp
            A-->>G: 200 OK + JWT Token
            G-->>U: JWT Token
        end
    end

    Note over U,DB: Fase 3: Acceso Protegido

    U->>G: GET /api/user (con JWT)
    G->>G: Validar JWT
    alt Token inválido/expirado
        G-->>U: 401 Unauthorized
    else Token válido
        G->>G: Extraer username y role del token
        G->>G: Agregar headers (X-User-Username, X-User-Role)
        G->>A: Forward con headers
        A->>DB: SELECT user WHERE username=?
        DB-->>A: Datos del usuario
        A-->>G: UserProfileResponse
        G-->>U: UserProfileResponse
    end
```

---

## 🏨 3. Flujo de Búsqueda y Reserva de Motel

```mermaid
sequenceDiagram
    participant U as 👤 Cliente
    participant G as 🚪 Gateway
    participant M as 🏨 Motel Service
    participant DB as 💾 PostgreSQL

    Note over U,DB: Fase 1: Búsqueda de Moteles (Público)

    U->>G: GET /api/motels/city/Quito
    G->>M: Forward request (sin autenticación)
    M->>DB: SELECT * FROM motels WHERE city='Quito'
    DB-->>M: Lista de moteles
    M-->>G: Array[MotelResponse]
    G-->>U: Array[MotelResponse]

    U->>U: Usuario selecciona un motel

    Note over U,DB: Fase 2: Ver Habitaciones Disponibles (Requiere Auth)

    U->>G: GET /api/rooms/motel/1/available (JWT)
    G->>G: Validar JWT
    alt Token inválido
        G-->>U: 401 Unauthorized
    else Token válido
        G->>M: Forward con headers
        M->>DB: SELECT * FROM rooms WHERE motel_id=1 AND available=true
        DB-->>M: Lista de habitaciones
        M-->>G: Array[RoomResponse]
        G-->>U: Array[RoomResponse]
    end

    U->>U: Usuario selecciona habitación y fechas

    Note over U,DB: Fase 3: Verificar Disponibilidad

    U->>G: GET /api/reservations/room/1/availability?checkIn=...&checkOut=... (JWT)
    G->>M: Forward con headers
    M->>DB: SELECT * FROM reservations WHERE room_id=1 AND dates overlap
    alt Hay conflicto de fechas
        DB-->>M: Reservas existentes en periodo
        M-->>G: {"available": false}
        G-->>U: {"available": false}
        U->>U: Mostrar "No disponible"
    else Sin conflictos
        DB-->>M: Sin conflictos
        M-->>G: {"available": true}
        G-->>U: {"available": true}
    end

    Note over U,DB: Fase 4: Crear Reserva

    U->>G: POST /api/reservations (JWT + reservation data)
    G->>M: Forward con headers
    M->>DB: BEGIN TRANSACTION
    M->>DB: Verificar disponibilidad (FOR UPDATE)
    alt No disponible (Race condition)
        DB-->>M: Habitación ocupada
        M->>DB: ROLLBACK
        M-->>G: 409 Conflict
        G-->>U: "Habitación no disponible"
    else Disponible
        DB-->>M: Disponible
        M->>DB: INSERT INTO reservations
        M->>DB: UPDATE rooms SET available=false WHERE id=1
        M->>DB: COMMIT
        DB-->>M: Reserva creada
        M-->>G: 201 Created + ReservationResponse
        G-->>U: ReservationResponse
        U->>U: Mostrar confirmación
    end
```

---

## 🔄 4. Flujo de Actualización de Perfil

```mermaid
sequenceDiagram
    participant U as 👤 Usuario
    participant G as 🚪 Gateway
    participant A as 👤 User Service
    participant DB as 💾 PostgreSQL

    U->>G: PUT /api/user (JWT + update data)
    G->>G: Validar JWT
    G->>G: Extraer username del token
    G->>G: Agregar header X-User-Username
    G->>A: Forward con headers

    A->>DB: SELECT * FROM users WHERE username=?
    alt Usuario no existe
        DB-->>A: Not found
        A-->>G: 404 Not Found
        G-->>U: "Usuario no encontrado"
    else Usuario existe
        DB-->>A: Datos actuales
        
        alt Actualizando email
            A->>DB: SELECT * FROM users WHERE email=? AND id!=current_id
            alt Email ya usado
                DB-->>A: Email existe
                A-->>G: 400 Bad Request
                G-->>U: "Email ya está en uso"
            else Email disponible
                DB-->>A: Email disponible
            end
        end

        alt Actualizando password
            A->>A: Encriptar nueva password (BCrypt)
        end

        A->>DB: UPDATE users SET ... WHERE username=?
        DB-->>A: Usuario actualizado
        A->>DB: SELECT * FROM users WHERE username=?
        DB-->>A: Datos actualizados
        A-->>G: 200 OK + UserProfileResponse
        G-->>U: UserProfileResponse actualizado
    end
```

---

## 🔑 5. Flujo de Reseteo de Contraseña

```mermaid
sequenceDiagram
    participant U as 👤 Usuario
    participant G as 🚪 Gateway
    participant A as 🔑 Auth Service
    participant DB as 💾 PostgreSQL
    participant E as 📧 Email Service

    Note over U,E: Fase 1: Solicitar Reseteo

    U->>G: POST /api/auth/reset-password-request?email=user@example.com
    G->>A: Forward request
    A->>DB: SELECT * FROM users WHERE email=?
    alt Email no existe
        DB-->>A: Not found
        Note over A: Por seguridad, misma respuesta
        A-->>G: 200 OK
        G-->>U: "Si el email existe, recibirás instrucciones"
    else Email existe
        DB-->>A: Usuario encontrado
        A->>A: Generar token aleatorio
        A->>A: Calcular fecha expiración (1 hora)
        A->>DB: UPDATE users SET reset_token=?, reset_token_expiry=?
        DB-->>A: Token guardado
        A->>E: Enviar email con token
        E-->>A: Email enviado
        A-->>G: 200 OK
        G-->>U: "Se ha enviado email con instrucciones"
    end

    Note over U,E: Usuario recibe email y hace clic en link

    Note over U,E: Fase 2: Completar Reseteo

    U->>G: POST /api/auth/reset-password (token + newPassword)
    G->>A: Forward request
    A->>DB: SELECT * FROM users WHERE reset_token=?
    alt Token no existe
        DB-->>A: Not found
        A-->>G: 400 Bad Request
        G-->>U: "Token inválido o expirado"
    else Token existe
        DB-->>A: Usuario encontrado
        A->>A: Verificar fecha expiración
        alt Token expirado
            A-->>G: 400 Bad Request
            G-->>U: "Token inválido o expirado"
        else Token válido
            A->>A: Encriptar nueva password (BCrypt)
            A->>DB: UPDATE users SET password=?, reset_token=NULL
            DB-->>A: Password actualizada
            A-->>G: 200 OK
            G-->>U: "Contraseña actualizada exitosamente"
        end
    end
```

---

## 🏗️ 6. Arquitectura Hexagonal - Motel Management

```mermaid
graph TB
    subgraph Adapters["Adaptadores de Entrada (Primary)"]
        RestAPI[REST Controllers]
        WebSockets[WebSocket Handlers]
        GraphQL[GraphQL Resolvers]
    end

    subgraph Domain["🎯 Capa de Dominio"]
        UseCases[Use Cases / Ports In]
        Models[Domain Models]
        Services[Domain Services]
        Ports[Ports Out]
    end

    subgraph Infrastructure["Adaptadores de Salida (Secondary)"]
        R2DBC[R2DBC Repositories]
        SMTP[Email Service]
        Cache[Redis Cache]
        Storage[S3 Storage]
    end

    RestAPI --> UseCases
    WebSockets --> UseCases
    GraphQL --> UseCases

    UseCases --> Models
    UseCases --> Services
    Services --> Models
    Services --> Ports

    Ports --> R2DBC
    Ports --> SMTP
    Ports --> Cache
    Ports --> Storage

    style Domain fill:#ffeb3b
    style Adapters fill:#4caf50
    style Infrastructure fill:#2196f3
```

---

## 🔄 7. Flujo de Manejo de Errores

```mermaid
graph TB
    Start[Request del Cliente] --> Gateway{Gateway}
    
    Gateway -->|JWT inválido| Error401[401 Unauthorized]
    Gateway -->|JWT válido| ValidateRoute{Validar Ruta}
    
    ValidateRoute -->|Ruta no existe| Error404[404 Not Found]
    ValidateRoute -->|Ruta existe| ForwardRequest[Forward a Microservicio]
    
    ForwardRequest --> ServiceAvailable{Servicio Disponible?}
    
    ServiceAvailable -->|No| Error503[503 Service Unavailable]
    ServiceAvailable -->|Timeout| Error504[504 Gateway Timeout]
    ServiceAvailable -->|Sí| ProcessRequest[Procesar Request]
    
    ProcessRequest --> Validation{Validación}
    
    Validation -->|Datos inválidos| Error400[400 Bad Request]
    Validation -->|Sin permisos| Error403[403 Forbidden]
    Validation -->|Recurso no existe| Error404B[404 Not Found]
    Validation -->|Conflicto| Error409[409 Conflict]
    Validation -->|OK| Success[200/201 Success]
    
    ProcessRequest -->|Error interno| Error500[500 Internal Server Error]
    
    Error401 --> LogError[Log Error]
    Error403 --> LogError
    Error404 --> LogError
    Error404B --> LogError
    Error400 --> LogError
    Error409 --> LogError
    Error500 --> LogError
    Error503 --> LogError
    Error504 --> LogError
    Success --> LogSuccess[Log Success]
    
    LogError --> ReturnResponse[Retornar Respuesta]
    LogSuccess --> ReturnResponse
    
    ReturnResponse --> End[Cliente recibe respuesta]

    style Error401 fill:#f44336
    style Error403 fill:#f44336
    style Error404 fill:#ff9800
    style Error404B fill:#ff9800
    style Error400 fill:#ff9800
    style Error409 fill:#ff9800
    style Error500 fill:#f44336
    style Error503 fill:#f44336
    style Error504 fill:#f44336
    style Success fill:#4caf50
```

---

## 🔀 8. Flujo de Concurrencia - Double Booking Prevention

```mermaid
sequenceDiagram
    participant U1 as 👤 Usuario 1
    participant U2 as 👤 Usuario 2
    participant G as 🚪 Gateway
    participant M as 🏨 Motel Service
    participant DB as 💾 PostgreSQL

    Note over U1,DB: Ambos usuarios intentan reservar misma habitación

    par Usuario 1 solicita reserva
        U1->>G: POST /api/reservations (room_id=1)
        G->>M: Forward request
        M->>DB: BEGIN TRANSACTION
        M->>DB: SELECT * FROM rooms WHERE id=1 FOR UPDATE
    and Usuario 2 solicita reserva (casi simultáneo)
        U2->>G: POST /api/reservations (room_id=1)
        G->>M: Forward request
        M->>DB: BEGIN TRANSACTION
        M->>DB: SELECT * FROM rooms WHERE id=1 FOR UPDATE
        Note over DB: Usuario 2 espera lock
    end

    DB-->>M: Habitación bloqueada (U1)
    M->>M: Verificar disponibilidad
    M->>DB: INSERT INTO reservations (U1)
    M->>DB: UPDATE rooms SET available=false
    M->>DB: COMMIT
    DB-->>M: Reserva creada (U1)
    M-->>G: 201 Created
    G-->>U1: ✅ Reserva exitosa

    Note over DB: Lock liberado, U2 obtiene lock

    DB-->>M: Habitación bloqueada (U2)
    M->>M: Verificar disponibilidad
    alt Habitación ya reservada
        M->>DB: ROLLBACK
        M-->>G: 409 Conflict
        G-->>U2: ❌ Habitación no disponible
    end
```

---

## 📊 9. Diagrama de Estados - Reserva

```mermaid
stateDiagram-v2
    [*] --> PENDING: Crear reserva
    
    PENDING --> CONFIRMED: Usuario confirma
    PENDING --> CANCELLED: Usuario cancela
    PENDING --> EXPIRED: Timeout (15 min)
    
    CONFIRMED --> IN_PROGRESS: Check-in realizado
    CONFIRMED --> CANCELLED: Usuario cancela
    CONFIRMED --> NO_SHOW: Cliente no se presenta
    
    IN_PROGRESS --> COMPLETED: Check-out realizado
    IN_PROGRESS --> CANCELLED: Cancelación forzada
    
    COMPLETED --> [*]
    CANCELLED --> [*]
    EXPIRED --> [*]
    NO_SHOW --> [*]

    note right of PENDING
        - Usuario recibe 15 min
        - para confirmar
    end note

    note right of CONFIRMED
        - Pago procesado
        - Habitación bloqueada
    end note

    note right of COMPLETED
        - Habitación disponible
        - Factura generada
    end note
```

---

## 🔍 10. Flujo de Gateway - Request Processing

```mermaid
graph TB
    Request[Cliente Request] --> ReceiveRequest[Gateway recibe request]
    
    ReceiveRequest --> LogRequest[Log Request]
    LogRequest --> CORSCheck[Verificar CORS]
    
    CORSCheck -->|OPTIONS| CORSResponse[Retornar CORS headers]
    CORSCheck -->|Otros métodos| CheckAuth{Requiere Auth?}
    
    CheckAuth -->|No| RouteRequest[Enrutar Request]
    CheckAuth -->|Sí| JWTPresent{JWT presente?}
    
    JWTPresent -->|No| Return401[401 Unauthorized]
    JWTPresent -->|Sí| ValidateJWT[Validar JWT]
    
    ValidateJWT -->|Inválido| Return401
    ValidateJWT -->|Expirado| Return401
    ValidateJWT -->|Válido| ExtractClaims[Extraer claims]
    
    ExtractClaims --> CheckRole{Rol suficiente?}
    
    CheckRole -->|No| Return403[403 Forbidden]
    CheckRole -->|Sí| AddHeaders[Agregar headers]
    
    AddHeaders --> RouteRequest
    
    RouteRequest --> CallService[Llamar Microservicio]
    
    CallService -->|Success| ForwardResponse[Forward Response]
    CallService -->|Error| HandleError[Manejar Error]
    
    ForwardResponse --> LogResponse[Log Response]
    HandleError --> LogResponse
    
    LogResponse --> ReturnClient[Retornar a Cliente]
    CORSResponse --> ReturnClient
    Return401 --> ReturnClient
    Return403 --> ReturnClient

    style Return401 fill:#f44336
    style Return403 fill:#f44336
    style ForwardResponse fill:#4caf50
```

---

## 📈 11. Escalabilidad - Múltiples Instancias

```mermaid
graph TB
    subgraph LoadBalancer["⚖️ Load Balancer"]
        LB[Nginx / HAProxy]
    end

    subgraph GatewayCluster["🚪 API Gateway Cluster"]
        GW1[Gateway Instance 1]
        GW2[Gateway Instance 2]
        GW3[Gateway Instance 3]
    end

    subgraph UserCluster["👤 User Management Cluster"]
        U1[User Service 1]
        U2[User Service 2]
    end

    subgraph MotelCluster["🏨 Motel Management Cluster"]
        M1[Motel Service 1]
        M2[Motel Service 2]
        M3[Motel Service 3]
    end

    subgraph Database["💾 Databases"]
        UserDB[(User DB Primary)]
        UserDB_Replica[(User DB Replica)]
        MotelDB[(Motel DB Primary)]
        MotelDB_Replica[(Motel DB Replica)]
    end

    LB --> GW1
    LB --> GW2
    LB --> GW3

    GW1 --> U1
    GW1 --> M1
    GW2 --> U2
    GW2 --> M2
    GW3 --> U1
    GW3 --> M3

    U1 --> UserDB
    U2 --> UserDB
    U1 -.read.-> UserDB_Replica
    U2 -.read.-> UserDB_Replica

    M1 --> MotelDB
    M2 --> MotelDB
    M3 --> MotelDB
    M1 -.read.-> MotelDB_Replica
    M2 -.read.-> MotelDB_Replica
    M3 -.read.-> MotelDB_Replica

    UserDB -.replication.-> UserDB_Replica
    MotelDB -.replication.-> MotelDB_Replica

    style LoadBalancer fill:#e1f5ff
    style GatewayCluster fill:#e1f5ff
    style UserCluster fill:#fff4e1
    style MotelCluster fill:#e8f5e9
    style Database fill:#fce4ec
```

---

## 🔧 12. Diagrama de Despliegue

```mermaid
graph TB
    subgraph Internet["🌐 Internet"]
        Users[Usuarios]
    end

    subgraph DMZ["🛡️ DMZ"]
        ALB[Application Load Balancer]
        WAF[Web Application Firewall]
    end

    subgraph AppTier["💻 Application Tier"]
        Gateway1[Gateway Container]
        Gateway2[Gateway Container]
        User1[User Service Container]
        User2[User Service Container]
        Motel1[Motel Service Container]
        Motel2[Motel Service Container]
    end

    subgraph DataTier["💾 Data Tier"]
        UserDB[(PostgreSQL Primary)]
        UserDBRep[(PostgreSQL Replica)]
        MotelDB[(PostgreSQL Primary)]
        MotelDBRep[(PostgreSQL Replica)]
        Redis[(Redis Cache)]
    end

    subgraph Monitoring["📊 Monitoring"]
        Prometheus[Prometheus]
        Grafana[Grafana]
        ELK[ELK Stack]
    end

    Users -->|HTTPS| WAF
    WAF --> ALB
    ALB --> Gateway1
    ALB --> Gateway2

    Gateway1 --> User1
    Gateway1 --> Motel1
    Gateway2 --> User2
    Gateway2 --> Motel2

    User1 --> UserDB
    User2 --> UserDB
    User1 -.-> UserDBRep
    User2 -.-> UserDBRep
    User1 -.-> Redis
    User2 -.-> Redis

    Motel1 --> MotelDB
    Motel2 --> MotelDB
    Motel1 -.-> MotelDBRep
    Motel2 -.-> MotelDBRep
    Motel1 -.-> Redis
    Motel2 -.-> Redis

    UserDB -.replication.-> UserDBRep
    MotelDB -.replication.-> MotelDBRep

    Gateway1 -.metrics.-> Prometheus
    Gateway2 -.metrics.-> Prometheus
    User1 -.metrics.-> Prometheus
    User2 -.metrics.-> Prometheus
    Motel1 -.metrics.-> Prometheus
    Motel2 -.metrics.-> Prometheus

    Prometheus --> Grafana
    Gateway1 -.logs.-> ELK
    Gateway2 -.logs.-> ELK
    User1 -.logs.-> ELK
    User2 -.logs.-> ELK
    Motel1 -.logs.-> ELK
    Motel2 -.logs.-> ELK

    style DMZ fill:#f44336,color:#fff
    style AppTier fill:#4caf50,color:#fff
    style DataTier fill:#2196f3,color:#fff
    style Monitoring fill:#ff9800,color:#fff
```

---

**Última actualización:** Diciembre 2024

## 📝 Notas de Uso

- Todos los diagramas son renderizables en GitHub, GitLab y otras plataformas que soporten Mermaid
- Para editar los diagramas, usa el [Mermaid Live Editor](https://mermaid.live/)
- Los diagramas pueden ser exportados como PNG/SVG desde el editor

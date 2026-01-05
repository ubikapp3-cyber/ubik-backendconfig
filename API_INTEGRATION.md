# Documentación de Integración API - Ubik Backend

## Tabla de Contenidos
- [Información General](#información-general)
- [Autenticación](#autenticación)
- [Configuración de Gateway](#configuración-de-gateway)
- [Endpoints de Autenticación](#endpoints-de-autenticación)
- [Endpoints de Perfil de Usuario](#endpoints-de-perfil-de-usuario)
- [Endpoints de Moteles](#endpoints-de-moteles)
- [Endpoints de Habitaciones](#endpoints-de-habitaciones)
- [Endpoints de Reservaciones](#endpoints-de-reservaciones)
- [Endpoints de Servicios](#endpoints-de-servicios)
- [Modelos de Datos](#modelos-de-datos)
- [Códigos de Estado HTTP](#códigos-de-estado-http)
- [Ejemplos de Integración](#ejemplos-de-integración)

---

## Información General

### URLs Base

El sistema utiliza un **API Gateway** que centraliza todas las peticiones:

- **Gateway (Punto de entrada principal)**: `http://localhost:8080`
- **User Management Service**: `http://localhost:8081` (acceso directo, pero se recomienda usar el Gateway)
- **Motel Management Service**: `http://localhost:8084` (acceso directo, pero se recomienda usar el Gateway)

### Arquitectura de Microservicios

```
Frontend → API Gateway (puerto 8080)
              ↓
              ├─→ User Management Service (puerto 8081)
              └─→ Motel Management Service (puerto 8084)
```

### CORS Configuration

El Gateway tiene configurado CORS para permitir peticiones desde cualquier origen:

```yaml
allowed-origins: "*"
allowed-methods: GET, POST, PUT, DELETE, OPTIONS
allowed-headers: "*"
allow-credentials: false
```

### Swagger/OpenAPI

Cada servicio expone su documentación interactiva:

- **User Management Swagger**: `http://localhost:8081/swagger-ui.html`
- **Motel Management Swagger**: `http://localhost:8084/swagger-ui.html`
- **Gateway Swagger**: `http://localhost:8080/swagger-ui.html`

---

## Autenticación

### Sistema JWT (JSON Web Tokens)

El sistema utiliza JWT para la autenticación. El flujo es:

1. **Registro/Login** → Obtener token JWT
2. **Incluir token** en todas las peticiones protegidas
3. **Token expira** en 24 horas (86400000 ms)

### Headers de Autenticación

Para endpoints protegidos, incluir:

```http
Authorization: Bearer <tu-token-jwt>
```

### Headers Personalizados

El Gateway inyecta headers adicionales en las peticiones:

- `X-User-Id`: ID del usuario autenticado
- `X-User-Role`: Rol del usuario (ADMIN, USER, CLIENT)
- `X-User-Username`: Nombre de usuario

---

## Endpoints de Autenticación

**Base URL**: `http://localhost:8080/api/auth`

### 1. Registrar Usuario

```http
POST /api/auth/register
```

**Request Body**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string (formato email válido)",
  "anonymous": false,
  "roleId": 1
}
```

**Notas**:
- `roleId`: 1=ADMIN, 2=USER, 3=CLIENT
- El password será hasheado con BCrypt (strength: 12)

**Response**: `201 Created`
```json
"Usuario registrado exitosamente"
```

**Errores**:
- `400 Bad Request`: Datos inválidos o usuario ya existe

---

### 2. Iniciar Sesión

```http
POST /api/auth/login
```

**Request Body**:
```json
{
  "username": "string",
  "password": "string"
}
```

**Response**: `200 OK`
```json
"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNjM4..."
```

**Notas**:
- La respuesta es el token JWT como string
- Guardar este token para usarlo en peticiones protegidas

**Errores**:
- `401 Unauthorized`: Credenciales inválidas

---

### 3. Solicitar Restablecimiento de Contraseña

```http
POST /api/auth/reset-password-request?email={email}
```

**Query Parameters**:
- `email`: Email del usuario

**Response**: `200 OK`
```json
"Solicitud de restablecimiento enviada"
```

---

### 4. Restablecer Contraseña

```http
POST /api/auth/reset-password
```

**Request Body**:
```json
{
  "token": "string",
  "newPassword": "string"
}
```

**Response**: `200 OK`
```json
"Contraseña restablecida exitosamente"
```

---

### 5. Endpoints de Prueba (Testing)

```http
GET /api/auth/admin/test
```
**Requiere**: Rol ADMIN

```http
GET /api/auth/user/test
```
**Requiere**: Rol USER o CLIENT

---

## Endpoints de Perfil de Usuario

**Base URL**: `http://localhost:8080/api/user`

**Autenticación**: Todos los endpoints requieren JWT token

### 1. Obtener Perfil

```http
GET /api/user
```

**Headers**:
```http
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "role": "USER",
  "anonymous": false,
  "createdAt": "2024-12-25T10:30:00"
}
```

**Errores**:
- `404 Not Found`: Usuario no encontrado

---

### 2. Actualizar Perfil

```http
PUT /api/user
```

**Headers**:
```http
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "email": "string (opcional)",
  "password": "string (opcional)",
  "anonymous": false
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "username": "string",
  "email": "updated@email.com",
  "role": "USER",
  "anonymous": false,
  "createdAt": "2024-12-25T10:30:00"
}
```

---

## Endpoints de Moteles

**Base URL**: `http://localhost:8080/api/motels`

### 1. Crear Motel

```http
POST /api/motels
```

**Request Body**:
```json
{
  "name": "string (3-100 caracteres, requerido)",
  "address": "string (max 255 caracteres, requerido)",
  "phoneNumber": "string (max 20 caracteres)",
  "description": "string (max 500 caracteres)",
  "city": "string (max 100 caracteres, requerido)",
  "propertyId": 1,
  "imageUrls": [
    "string (max 500 caracteres, máximo 10 URLs)"
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "name": "Motel Paradise",
  "address": "Calle Principal 123",
  "phoneNumber": "+1234567890",
  "description": "Un motel acogedor",
  "city": "Ciudad",
  "propertyId": 1,
  "dateCreated": "2024-12-25T10:30:00",
  "imageUrls": [
    "https://example.com/image1.jpg"
  ]
}
```

**Errores**:
- `400 Bad Request`: Datos inválidos

---

### 2. Obtener Motel por ID

```http
GET /api/motels/{id}
```

**Path Parameters**:
- `id`: ID del motel

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Motel Paradise",
  "address": "Calle Principal 123",
  "phoneNumber": "+1234567890",
  "description": "Un motel acogedor",
  "city": "Ciudad",
  "propertyId": 1,
  "dateCreated": "2024-12-25T10:30:00",
  "imageUrls": [...]
}
```

**Errores**:
- `404 Not Found`: Motel no encontrado

---

### 3. Listar Todos los Moteles

```http
GET /api/motels
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Motel Paradise",
    "address": "Calle Principal 123",
    "phoneNumber": "+1234567890",
    "description": "Un motel acogedor",
    "city": "Ciudad",
    "propertyId": 1,
    "dateCreated": "2024-12-25T10:30:00",
    "imageUrls": [...]
  },
  ...
]
```

---

### 4. Buscar Moteles por Ciudad

```http
GET /api/motels/city/{city}
```

**Path Parameters**:
- `city`: Nombre de la ciudad

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Motel Paradise",
    "city": "Ciudad",
    ...
  }
]
```

---

### 5. Actualizar Motel

```http
PUT /api/motels/{id}
```

**Requiere**: Autenticación JWT

**Path Parameters**:
- `id`: ID del motel

**Request Body**: Similar a crear motel

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Motel Paradise Updated",
  ...
}
```

**Errores**:
- `404 Not Found`: Motel no encontrado
- `400 Bad Request`: Datos inválidos

---

### 6. Eliminar Motel

```http
DELETE /api/motels/{id}
```

**Requiere**: Autenticación JWT

**Path Parameters**:
- `id`: ID del motel

**Response**: `204 No Content`

**Errores**:
- `404 Not Found`: Motel no encontrado

---

## Endpoints de Habitaciones

**Base URL**: `http://localhost:8080/api/rooms`

**Autenticación**: La mayoría de endpoints requieren JWT token

### 1. Crear Habitación

```http
POST /api/rooms
```

**Requiere**: Autenticación JWT

**Request Body**:
```json
{
  "motelId": 1,
  "number": "string (max 20 caracteres, requerido)",
  "roomType": "string (max 50 caracteres, requerido)",
  "price": 99.99,
  "description": "string (max 500 caracteres)",
  "imageUrls": [
    "string (max 500 caracteres, máximo 15 URLs)"
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "motelId": 1,
  "number": "101",
  "roomType": "Deluxe",
  "price": 99.99,
  "description": "Habitación amplia con vista al mar",
  "isAvailable": true,
  "imageUrls": [...]
}
```

---

### 2. Obtener Habitación por ID

```http
GET /api/rooms/{id}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "motelId": 1,
  "number": "101",
  "roomType": "Deluxe",
  "price": 99.99,
  "description": "Habitación amplia",
  "isAvailable": true,
  "imageUrls": [...]
}
```

---

### 3. Listar Todas las Habitaciones

```http
GET /api/rooms
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "motelId": 1,
    "number": "101",
    "roomType": "Deluxe",
    "price": 99.99,
    "isAvailable": true,
    ...
  }
]
```

---

### 4. Buscar Habitaciones por Motel

```http
GET /api/rooms/motel/{motelId}
```

**Path Parameters**:
- `motelId`: ID del motel

**Response**: `200 OK` - Array de habitaciones del motel

---

### 5. Buscar Habitaciones Disponibles por Motel

```http
GET /api/rooms/motel/{motelId}/available
```

**Path Parameters**:
- `motelId`: ID del motel

**Response**: `200 OK` - Array de habitaciones disponibles

---

### 6. Actualizar Habitación

```http
PUT /api/rooms/{id}
```

**Requiere**: Autenticación JWT

**Request Body**: Similar a crear habitación

**Response**: `200 OK`

---

### 7. Eliminar Habitación

```http
DELETE /api/rooms/{id}
```

**Requiere**: Autenticación JWT

**Response**: `204 No Content`

---

## Endpoints de Reservaciones

**Base URL**: `http://localhost:8080/api/reservations`

**Autenticación**: Todos los endpoints requieren JWT token

### 1. Crear Reservación

```http
POST /api/reservations
```

**Request Body**:
```json
{
  "roomId": 1,
  "userId": 1,
  "checkInDate": "2024-12-26T14:00:00",
  "checkOutDate": "2024-12-28T12:00:00",
  "totalPrice": 199.98,
  "specialRequests": "string (max 500 caracteres, opcional)"
}
```

**Validaciones**:
- Las fechas deben ser en el futuro
- `checkOutDate` debe ser posterior a `checkInDate`
- `totalPrice` debe ser positivo

**Response**: `201 Created`
```json
{
  "id": 1,
  "roomId": 1,
  "userId": 1,
  "checkInDate": "2024-12-26T14:00:00",
  "checkOutDate": "2024-12-28T12:00:00",
  "status": "PENDING",
  "totalPrice": 199.98,
  "specialRequests": "Vista al mar",
  "createdAt": "2024-12-25T10:30:00",
  "updatedAt": "2024-12-25T10:30:00"
}
```

**Estados de Reservación**:
- `PENDING`: Pendiente de confirmación
- `CONFIRMED`: Confirmada
- `CHECKED_IN`: Usuario ha hecho check-in
- `CHECKED_OUT`: Usuario ha hecho check-out
- `CANCELLED`: Cancelada

---

### 2. Obtener Reservación por ID

```http
GET /api/reservations/{id}
```

**Response**: `200 OK`

---

### 3. Listar Todas las Reservaciones

```http
GET /api/reservations
```

**Response**: `200 OK` - Array de reservaciones

---

### 4. Buscar Reservaciones por Habitación

```http
GET /api/reservations/room/{roomId}
```

**Response**: `200 OK` - Array de reservaciones de la habitación

---

### 5. Buscar Reservaciones por Usuario

```http
GET /api/reservations/user/{userId}
```

**Response**: `200 OK` - Array de reservaciones del usuario

---

### 6. Buscar Reservaciones Activas por Habitación

```http
GET /api/reservations/room/{roomId}/active
```

**Response**: `200 OK` - Array de reservaciones activas

---

### 7. Buscar Reservaciones por Estado

```http
GET /api/reservations/status/{status}
```

**Path Parameters**:
- `status`: PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

**Response**: `200 OK`

---

### 8. Verificar Disponibilidad de Habitación

```http
GET /api/reservations/room/{roomId}/available?checkIn={checkIn}&checkOut={checkOut}
```

**Query Parameters**:
- `checkIn`: Fecha de entrada (formato ISO 8601: 2024-12-26T14:00:00)
- `checkOut`: Fecha de salida (formato ISO 8601)

**Response**: `200 OK`
```json
true
```

---

### 9. Actualizar Reservación

```http
PUT /api/reservations/{id}
```

**Request Body**: Similar a crear reservación

**Response**: `200 OK`

---

### 10. Confirmar Reservación

```http
PATCH /api/reservations/{id}/confirm
```

**Response**: `200 OK` - Cambia el estado a CONFIRMED

---

### 11. Cancelar Reservación

```http
PATCH /api/reservations/{id}/cancel
```

**Response**: `200 OK` - Cambia el estado a CANCELLED

---

### 12. Check-in

```http
PATCH /api/reservations/{id}/checkin
```

**Response**: `200 OK` - Cambia el estado a CHECKED_IN

---

### 13. Check-out

```http
PATCH /api/reservations/{id}/checkout
```

**Response**: `200 OK` - Cambia el estado a CHECKED_OUT

---

### 14. Eliminar Reservación

```http
DELETE /api/reservations/{id}
```

**Nota**: Solo se pueden eliminar reservaciones canceladas

**Response**: `204 No Content`

---

## Endpoints de Servicios

**Base URL**: `http://localhost:8080/api/services`

**Autenticación**: Todos los endpoints requieren JWT token

### 1. Crear Servicio

```http
POST /api/services
```

**Request Body**:
```json
{
  "name": "string (requerido)",
  "description": "string",
  "price": 15.99
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "name": "WiFi",
  "description": "Internet de alta velocidad",
  "price": 15.99
}
```

---

### 2. Obtener Servicio por ID

```http
GET /api/services/{id}
```

**Response**: `200 OK`

---

### 3. Listar Todos los Servicios

```http
GET /api/services
```

**Response**: `200 OK` - Array de servicios

---

### 4. Obtener Servicio por Nombre

```http
GET /api/services/name/{name}
```

**Response**: `200 OK`

---

### 5. Actualizar Servicio

```http
PUT /api/services/{id}
```

**Response**: `200 OK`

---

### 6. Eliminar Servicio

```http
DELETE /api/services/{id}
```

**Response**: `204 No Content`

---

### 7. Obtener Servicios de una Habitación

```http
GET /api/services/room/{roomId}
```

**Response**: `200 OK`
```json
[1, 2, 3]
```

**Nota**: Retorna un array de IDs de servicios

---

### 8. Asociar Servicio a Habitación

```http
POST /api/services/room/{roomId}/service/{serviceId}
```

**Path Parameters**:
- `roomId`: ID de la habitación
- `serviceId`: ID del servicio

**Response**: `201 Created`

---

### 9. Eliminar Servicio de Habitación

```http
DELETE /api/services/room/{roomId}/service/{serviceId}
```

**Response**: `204 No Content`

---

## Modelos de Datos

### User (Usuario)

```typescript
interface User {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER' | 'CLIENT';
  anonymous: boolean;
  createdAt: string; // ISO 8601
}
```

### Motel

```typescript
interface Motel {
  id: number;
  name: string;
  address: string;
  phoneNumber: string;
  description: string;
  city: string;
  propertyId: number;
  dateCreated: string; // ISO 8601
  imageUrls: string[];
}
```

### Room (Habitación)

```typescript
interface Room {
  id: number;
  motelId: number;
  number: string;
  roomType: string;
  price: number;
  description: string;
  isAvailable: boolean;
  imageUrls: string[];
}
```

### Reservation (Reservación)

```typescript
interface Reservation {
  id: number;
  roomId: number;
  userId: number;
  checkInDate: string; // ISO 8601
  checkOutDate: string; // ISO 8601
  status: 'PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'CANCELLED';
  totalPrice: number;
  specialRequests: string;
  createdAt: string; // ISO 8601
  updatedAt: string; // ISO 8601
}
```

### Service (Servicio)

```typescript
interface Service {
  id: number;
  name: string;
  description: string;
  price: number;
}
```

---

## Códigos de Estado HTTP

| Código | Significado | Uso |
|--------|-------------|-----|
| 200 | OK | Petición exitosa |
| 201 | Created | Recurso creado exitosamente |
| 204 | No Content | Operación exitosa sin contenido de respuesta |
| 400 | Bad Request | Datos de entrada inválidos |
| 401 | Unauthorized | No autenticado o token inválido |
| 403 | Forbidden | No tiene permisos para esta operación |
| 404 | Not Found | Recurso no encontrado |
| 500 | Internal Server Error | Error del servidor |

---

## Ejemplos de Integración

### Ejemplo 1: Flujo de Autenticación (JavaScript)

```javascript
// 1. Registrar usuario
const registerResponse = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'newuser',
    password: 'securepass123',
    email: 'user@example.com',
    anonymous: false,
    roleId: 2 // USER
  })
});

// 2. Iniciar sesión
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'newuser',
    password: 'securepass123'
  })
});

const token = await loginResponse.text(); // JWT token
localStorage.setItem('authToken', token);

// 3. Usar token en peticiones protegidas
const profileResponse = await fetch('http://localhost:8080/api/user', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  }
});

const userProfile = await profileResponse.json();
console.log(userProfile);
```

---

### Ejemplo 2: Buscar y Reservar Habitación (JavaScript)

```javascript
const token = localStorage.getItem('authToken');

// 1. Buscar moteles en una ciudad
const motelsResponse = await fetch('http://localhost:8080/api/motels/city/Ciudad', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
  }
});
const motels = await motelsResponse.json();

// 2. Obtener habitaciones disponibles del primer motel
const motelId = motels[0].id;
const roomsResponse = await fetch(`http://localhost:8080/api/rooms/motel/${motelId}/available`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  }
});
const availableRooms = await roomsResponse.json();

// 3. Verificar disponibilidad para fechas específicas
const roomId = availableRooms[0].id;
const checkIn = '2024-12-26T14:00:00';
const checkOut = '2024-12-28T12:00:00';

const availabilityResponse = await fetch(
  `http://localhost:8080/api/reservations/room/${roomId}/available?checkIn=${checkIn}&checkOut=${checkOut}`,
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  }
);
const isAvailable = await availabilityResponse.json();

// 4. Crear reservación si está disponible
if (isAvailable) {
  const reservationResponse = await fetch('http://localhost:8080/api/reservations', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      roomId: roomId,
      userId: 1, // ID del usuario actual
      checkInDate: checkIn,
      checkOutDate: checkOut,
      totalPrice: 199.98,
      specialRequests: 'Vista al mar'
    })
  });
  
  const reservation = await reservationResponse.json();
  console.log('Reservación creada:', reservation);
}
```

---

### Ejemplo 3: Angular Service para API

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080';
  
  constructor(private http: HttpClient) {}
  
  // Obtener headers con autenticación
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }
  
  // Autenticación
  login(username: string, password: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/api/auth/login`, 
      { username, password }, 
      { responseType: 'text' }
    );
  }
  
  register(data: any): Observable<string> {
    return this.http.post(`${this.baseUrl}/api/auth/register`, 
      data, 
      { responseType: 'text' }
    );
  }
  
  // Perfil de usuario
  getUserProfile(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/user`, {
      headers: this.getAuthHeaders()
    });
  }
  
  // Moteles
  getMotels(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/motels`);
  }
  
  getMotelsByCity(city: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/motels/city/${city}`);
  }
  
  // Habitaciones
  getRoomsByMotel(motelId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/api/rooms/motel/${motelId}`,
      { headers: this.getAuthHeaders() }
    );
  }
  
  getAvailableRooms(motelId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/api/rooms/motel/${motelId}/available`,
      { headers: this.getAuthHeaders() }
    );
  }
  
  // Reservaciones
  createReservation(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/reservations`, 
      data,
      { headers: this.getAuthHeaders() }
    );
  }
  
  getUserReservations(userId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/api/reservations/user/${userId}`,
      { headers: this.getAuthHeaders() }
    );
  }
  
  confirmReservation(reservationId: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/api/reservations/${reservationId}/confirm`,
      {},
      { headers: this.getAuthHeaders() }
    );
  }
  
  cancelReservation(reservationId: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/api/reservations/${reservationId}/cancel`,
      {},
      { headers: this.getAuthHeaders() }
    );
  }
}
```

---

### Ejemplo 4: React Hook para Autenticación

```javascript
import { useState, useEffect } from 'react';

export const useAuth = () => {
  const [token, setToken] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  
  const baseUrl = 'http://localhost:8080';
  
  useEffect(() => {
    const storedToken = localStorage.getItem('authToken');
    if (storedToken) {
      setToken(storedToken);
      fetchUserProfile(storedToken);
    } else {
      setLoading(false);
    }
  }, []);
  
  const fetchUserProfile = async (authToken) => {
    try {
      const response = await fetch(`${baseUrl}/api/user`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json',
        }
      });
      
      if (response.ok) {
        const userData = await response.json();
        setUser(userData);
      }
    } catch (error) {
      console.error('Error fetching user profile:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const login = async (username, password) => {
    try {
      const response = await fetch(`${baseUrl}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password })
      });
      
      if (response.ok) {
        const jwtToken = await response.text();
        localStorage.setItem('authToken', jwtToken);
        setToken(jwtToken);
        await fetchUserProfile(jwtToken);
        return { success: true };
      } else {
        return { success: false, error: 'Credenciales inválidas' };
      }
    } catch (error) {
      return { success: false, error: error.message };
    }
  };
  
  const logout = () => {
    localStorage.removeItem('authToken');
    setToken(null);
    setUser(null);
  };
  
  return {
    token,
    user,
    loading,
    isAuthenticated: !!token,
    login,
    logout
  };
};
```

---

## Notas Importantes

### 1. Formato de Fechas

Todas las fechas usan formato ISO 8601:
```
2024-12-25T14:30:00
```

### 2. Manejo de Errores

Siempre verificar el status code de la respuesta:

```javascript
const response = await fetch(url, options);

if (!response.ok) {
  const errorMessage = await response.text();
  console.error('Error:', response.status, errorMessage);
  // Manejar error apropiadamente
}
```

### 3. Token Expirado

El token JWT expira en 24 horas. Si recibes un error 401:
1. Eliminar token del almacenamiento
2. Redirigir al usuario al login
3. Solicitar nuevo token

### 4. Tipos de Rol

- `roleId: 1` → ADMIN (administrador del sistema)
- `roleId: 2` → USER (usuario regular)
- `roleId: 3` → CLIENT (cliente)

### 5. Desarrollo Local

Para desarrollo local, asegúrate de:
1. Iniciar PostgreSQL
2. Iniciar User Management Service (puerto 8081)
3. Iniciar Motel Management Service (puerto 8084)
4. Iniciar Gateway (puerto 8080)
5. Usar Gateway (puerto 8080) como punto de entrada

### 6. Variables de Entorno

Para producción, configura:
- Base URL del Gateway
- Credenciales de base de datos
- Secret key de JWT

---

## Contacto y Soporte

Para más información, consulta:
- Swagger UI de cada servicio
- README.md de cada microservicio
- Documentación de Spring Boot WebFlux

---

**Última actualización**: 25 de Diciembre de 2024

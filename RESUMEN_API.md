# Resumen de API - Guía Rápida para Integración Frontend

## 🚀 Inicio Rápido

### URL Principal
**Gateway (usar siempre)**: `http://localhost:8080`

### Swagger/Documentación Interactiva
- **Gateway**: http://localhost:8080/swagger-ui.html
- **User Management**: http://localhost:8081/swagger-ui.html
- **Motel Management**: http://localhost:8084/swagger-ui.html

---

## 🔐 Autenticación

### 1. Registrar Usuario
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "usuario123",
  "password": "mipassword",
  "email": "email@example.com",
  "anonymous": false,
  "roleId": 2
}
```

**roleId**: 1=ADMIN, 2=USER, 3=CLIENT

### 2. Login (Obtener Token)
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "usuario123",
  "password": "mipassword"
}
```

**Respuesta**: Token JWT (string)
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSI...
```

### 3. Usar Token en Peticiones
```http
GET http://localhost:8080/api/user
Authorization: Bearer TU_TOKEN_AQUI
```

---

## 📍 Endpoints Principales

### Autenticación (`/api/auth`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/register` | Registrar usuario | No |
| POST | `/login` | Iniciar sesión | No |
| POST | `/reset-password-request?email={email}` | Solicitar reset | No |
| POST | `/reset-password` | Resetear password | No |

### Perfil Usuario (`/api/user`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Obtener perfil | Sí |
| PUT | `/` | Actualizar perfil | Sí |

### Moteles (`/api/motels`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar todos | No |
| GET | `/{id}` | Obtener uno | No |
| GET | `/city/{city}` | Por ciudad | No |
| POST | `/` | Crear | Sí |
| PUT | `/{id}` | Actualizar | Sí |
| DELETE | `/{id}` | Eliminar | Sí |

### Habitaciones (`/api/rooms`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar todas | Sí |
| GET | `/{id}` | Obtener una | Sí |
| GET | `/motel/{motelId}` | Por motel | Sí |
| GET | `/motel/{motelId}/available` | Disponibles | Sí |
| POST | `/` | Crear | Sí |
| PUT | `/{id}` | Actualizar | Sí |
| DELETE | `/{id}` | Eliminar | Sí |

### Reservaciones (`/api/reservations`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar todas | Sí |
| GET | `/{id}` | Obtener una | Sí |
| GET | `/user/{userId}` | Por usuario | Sí |
| GET | `/room/{roomId}` | Por habitación | Sí |
| GET | `/room/{roomId}/available?checkIn=...&checkOut=...` | Verificar disponibilidad | Sí |
| GET | `/status/{status}` | Por estado | Sí |
| POST | `/` | Crear | Sí |
| PUT | `/{id}` | Actualizar | Sí |
| PATCH | `/{id}/confirm` | Confirmar | Sí |
| PATCH | `/{id}/cancel` | Cancelar | Sí |
| PATCH | `/{id}/checkin` | Check-in | Sí |
| PATCH | `/{id}/checkout` | Check-out | Sí |
| DELETE | `/{id}` | Eliminar | Sí |

### Servicios (`/api/services`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/` | Listar todos | Sí |
| GET | `/{id}` | Obtener uno | Sí |
| GET | `/name/{name}` | Por nombre | Sí |
| GET | `/room/{roomId}` | De una habitación | Sí |
| POST | `/` | Crear | Sí |
| POST | `/room/{roomId}/service/{serviceId}` | Asociar a habitación | Sí |
| PUT | `/{id}` | Actualizar | Sí |
| DELETE | `/{id}` | Eliminar | Sí |
| DELETE | `/room/{roomId}/service/{serviceId}` | Quitar de habitación | Sí |

---

## 📦 Modelos de Datos

### Motel
```typescript
{
  id: number
  name: string
  address: string
  phoneNumber: string
  description: string
  city: string
  propertyId: number
  dateCreated: string // ISO 8601
  imageUrls: string[]
}
```

### Habitación (Room)
```typescript
{
  id: number
  motelId: number
  number: string
  roomType: string
  price: number
  description: string
  isAvailable: boolean
  imageUrls: string[]
}
```

### Reservación (Reservation)
```typescript
{
  id: number
  roomId: number
  userId: number
  checkInDate: string // ISO 8601
  checkOutDate: string // ISO 8601
  status: "PENDING" | "CONFIRMED" | "CHECKED_IN" | "CHECKED_OUT" | "CANCELLED"
  totalPrice: number
  specialRequests: string
  createdAt: string
  updatedAt: string
}
```

### Usuario (User)
```typescript
{
  id: number
  username: string
  email: string
  role: "ADMIN" | "USER" | "CLIENT"
  anonymous: boolean
  createdAt: string
}
```

### Servicio (Service)
```typescript
{
  id: number
  name: string
  description: string
  price: number
}
```

---

## 💻 Ejemplo de Uso en JavaScript

```javascript
const API_BASE = 'http://localhost:8080';

// 1. Login y guardar token
async function login(username, password) {
  const response = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  
  const token = await response.text();
  localStorage.setItem('token', token);
  return token;
}

// 2. Obtener perfil del usuario
async function getProfile() {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE}/api/user`, {
    headers: { 
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  return await response.json();
}

// 3. Buscar moteles por ciudad
async function searchMotelsByCity(city) {
  const response = await fetch(`${API_BASE}/api/motels/city/${city}`);
  return await response.json();
}

// 4. Obtener habitaciones disponibles
async function getAvailableRooms(motelId) {
  const token = localStorage.getItem('token');
  const response = await fetch(
    `${API_BASE}/api/rooms/motel/${motelId}/available`,
    {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  return await response.json();
}

// 5. Crear una reservación
async function createReservation(reservationData) {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE}/api/reservations`, {
    method: 'POST',
    headers: { 
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(reservationData)
  });
  
  return await response.json();
}

// 6. Confirmar reservación
async function confirmReservation(reservationId) {
  const token = localStorage.getItem('token');
  const response = await fetch(
    `${API_BASE}/api/reservations/${reservationId}/confirm`,
    {
      method: 'PATCH',
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  return await response.json();
}

// 7. Obtener reservaciones de un usuario
async function getUserReservations(userId) {
  const token = localStorage.getItem('token');
  const response = await fetch(
    `${API_BASE}/api/reservations/user/${userId}`,
    {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  return await response.json();
}
```

---

## 🔄 Flujo de Usuario Típico

### Flujo de Búsqueda y Reserva

```
1. Usuario busca moteles
   → GET /api/motels/city/{city}
   
2. Usuario selecciona un motel
   → GET /api/rooms/motel/{motelId}/available
   
3. Usuario verifica disponibilidad
   → GET /api/reservations/room/{roomId}/available?checkIn=...&checkOut=...
   
4. Usuario crea cuenta (si no la tiene)
   → POST /api/auth/register
   
5. Usuario inicia sesión
   → POST /api/auth/login
   → Guardar token JWT
   
6. Usuario crea reservación
   → POST /api/reservations
   
7. Sistema confirma reservación
   → PATCH /api/reservations/{id}/confirm
   
8. Usuario ve sus reservaciones
   → GET /api/reservations/user/{userId}
```

---

## ⚠️ Códigos de Respuesta

| Código | Significado | Acción |
|--------|-------------|--------|
| 200 | OK | Todo correcto |
| 201 | Created | Recurso creado |
| 204 | No Content | Operación exitosa sin respuesta |
| 400 | Bad Request | Revisar datos enviados |
| 401 | Unauthorized | Token inválido o faltante |
| 403 | Forbidden | Sin permisos |
| 404 | Not Found | Recurso no existe |
| 500 | Server Error | Error del servidor |

---

## 🎯 Tips Importantes

1. **Siempre usar el Gateway** (puerto 8080), no los servicios directamente

2. **Token JWT expira en 24 horas** - renovar si es necesario

3. **Fechas en formato ISO 8601**: `2024-12-25T14:30:00`

4. **Guardar token** en localStorage o sessionStorage:
   ```javascript
   localStorage.setItem('token', tokenValue);
   ```

5. **Manejo de errores**:
   ```javascript
   if (!response.ok) {
     const error = await response.text();
     console.error('Error:', error);
     // Manejar error
   }
   ```

6. **CORS está configurado** - no debería haber problemas de cross-origin

7. **Roles de usuario**:
   - ADMIN (roleId: 1) - Administrador
   - USER (roleId: 2) - Usuario regular
   - CLIENT (roleId: 3) - Cliente

---

## 🛠️ Angular Service Completo

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UbikApiService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  // Auth
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

  // User
  getProfile(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/user`,
      { headers: this.getHeaders() }
    );
  }

  // Motels
  getMotels(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/motels`);
  }

  getMotelsByCity(city: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/motels/city/${city}`);
  }

  // Rooms
  getAvailableRooms(motelId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/api/rooms/motel/${motelId}/available`,
      { headers: this.getHeaders() }
    );
  }

  // Reservations
  createReservation(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/reservations`,
      data,
      { headers: this.getHeaders() }
    );
  }

  getUserReservations(userId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/api/reservations/user/${userId}`,
      { headers: this.getHeaders() }
    );
  }

  confirmReservation(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/api/reservations/${id}/confirm`,
      {},
      { headers: this.getHeaders() }
    );
  }

  cancelReservation(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/api/reservations/${id}/cancel`,
      {},
      { headers: this.getHeaders() }
    );
  }

  checkRoomAvailability(
    roomId: number,
    checkIn: string,
    checkOut: string
  ): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.baseUrl}/api/reservations/room/${roomId}/available?checkIn=${checkIn}&checkOut=${checkOut}`,
      { headers: this.getHeaders() }
    );
  }
}
```

---

## 📚 Documentación Completa

Para más detalles, ver: [API_INTEGRATION.md](./API_INTEGRATION.md)

---

**Última actualización**: 25 de Diciembre de 2024

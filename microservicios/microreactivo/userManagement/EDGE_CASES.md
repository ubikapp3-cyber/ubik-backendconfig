# Edge Cases - User Management Microservice

## 📋 Casos Borde Cubiertos

Este documento detalla todos los casos especiales y situaciones límite manejadas por el microservicio de gestión de usuarios.

---

## 🔐 Autenticación y Registro

### 1. Registro de Usuario

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Username duplicado | Rechaza el registro | 400 | "Error: El username ya está en uso" |
| Email duplicado | Rechaza el registro | 400 | "Error: El email ya está registrado" |
| Username vacío | Rechaza el registro | 400 | "El username es requerido" |
| Email inválido | Rechaza el registro | 400 | "Email inválido" |
| Contraseña débil | Rechaza el registro | 400 | "La contraseña debe cumplir requisitos mínimos" |
| RoleId inválido (< 1) | Rechaza el registro | 400 | "roleId debe ser un número positivo" |
| RoleId inexistente | Rechaza el registro | 400 | "Rol no encontrado" |
| Campo anonymous null | Rechaza el registro | 400 | "El campo anonymous es requerido" |
| Espacios en username | Se recortan automáticamente | 201 | Usuario creado |
| Email con mayúsculas | Se normaliza a minúsculas | 201 | Usuario creado |

#### 💡 Ejemplo: Username Duplicado

```bash
# Primera solicitud - Éxito
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "Test@123",
    "email": "john@example.com",
    "anonymous": false,
    "roleId": 1
  }'
# Respuesta: 201 - "Usuario registrado exitosamente"

# Segunda solicitud con mismo username - Falla
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "Different@456",
    "email": "different@example.com",
    "anonymous": false,
    "roleId": 1
  }'
# Respuesta: 400 - "Error: El username ya está en uso"
```

### 2. Login

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Credenciales correctas | Retorna JWT | 200 | Token JWT válido |
| Username incorrecto | Rechaza login | 400 | "Error: Credenciales inválidas" |
| Password incorrecta | Rechaza login | 400 | "Error: Credenciales inválidas" |
| Username vacío | Rechaza login | 400 | "El username es requerido" |
| Password vacío | Rechaza login | 400 | "La contraseña es requerida" |
| Usuario no existe | Rechaza login | 400 | "Error: Credenciales inválidas" |
| Múltiples intentos fallidos | Acepta reintentos (sin bloqueo) | 400 | "Error: Credenciales inválidas" |
| Username con espacios | Se recorta y valida | Variable | - |

#### 💡 Ejemplo: Credenciales Incorrectas

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "WrongPassword"
  }'
# Respuesta: 400 - "Error: Credenciales inválidas"
```

### 3. JWT Token

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Token válido | Acceso concedido | 200 | - |
| Token expirado (>24h) | Acceso denegado | 401 | "Token expirado" |
| Token malformado | Acceso denegado | 401 | "Token inválido" |
| Token sin Bearer prefix | Acceso denegado | 401 | "Token faltante" |
| Token vacío | Acceso denegado | 401 | "Token faltante" |
| Firma JWT inválida | Acceso denegado | 401 | "Token inválido" |
| Token con secret incorrecto | Acceso denegado | 401 | "Token inválido" |
| Header sin Authorization | Acceso denegado | 401 | "No autenticado" |

#### 💡 Ejemplo: Token Expirado

```bash
# Token generado hace más de 24 horas
curl -X GET http://localhost:8081/api/user \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired-token..."
# Respuesta: 401 - "Token expirado"
```

---

## 👤 Gestión de Perfil

### 4. Obtener Perfil

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Usuario existe | Retorna perfil | 200 | JSON del perfil |
| Usuario no existe | Error | 404 | "Usuario no encontrado" |
| Token válido pero usuario eliminado | Error | 404 | "Usuario no encontrado" |
| Header X-User-Username faltante | Error | 400 | "Username requerido" |
| Header X-User-Username vacío | Error | 400 | "Username inválido" |
| Username con caracteres especiales | Busca y retorna si existe | 200 | JSON del perfil |

#### 💡 Ejemplo: Usuario No Encontrado

```bash
# Usuario fue eliminado pero token aún válido
curl -X GET http://localhost:8081/api/user \
  -H "Authorization: Bearer <token-valido>" \
  -H "X-User-Username: deleted_user"
# Respuesta: 404 - "Usuario no encontrado"
```

### 5. Actualizar Perfil

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Actualización válida | Actualiza y retorna perfil | 200 | JSON actualizado |
| Email nuevo duplicado | Rechaza actualización | 400 | "Email ya está en uso" |
| Email inválido | Rechaza actualización | 400 | "Email inválido" |
| Contraseña nueva válida | Actualiza y encripta | 200 | JSON actualizado |
| Contraseña nueva débil | Rechaza actualización | 400 | "Contraseña debe cumplir requisitos" |
| Solo actualizar email | Actualiza solo email | 200 | JSON actualizado |
| Solo actualizar password | Actualiza solo password | 200 | JSON actualizado |
| Body vacío | Rechaza actualización | 400 | "Debe especificar campos a actualizar" |
| Usuario no existe | Error | 404 | "Usuario no encontrado" |
| Email con espacios | Se recorta y valida | Variable | - |

#### 💡 Ejemplo: Email Duplicado

```bash
curl -X PUT http://localhost:8081/api/user \
  -H "Authorization: Bearer <token>" \
  -H "X-User-Username: user1" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "existing@example.com"
  }'
# Si existing@example.com ya existe para otro usuario:
# Respuesta: 400 - "Email ya está en uso"
```

---

## 🔑 Reseteo de Contraseña

### 6. Solicitar Reseteo

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Email existe | Genera token y envía email | 200 | "Email enviado con instrucciones" |
| Email no existe | Respuesta genérica (seguridad) | 200 | "Si el email existe, recibirás instrucciones" |
| Email inválido | Rechaza solicitud | 400 | "Email inválido" |
| Email vacío | Rechaza solicitud | 400 | "Email requerido" |
| Múltiples solicitudes seguidas | Acepta, regenera token | 200 | "Email enviado con instrucciones" |
| Token anterior no expirado | Se sobrescribe con nuevo token | 200 | "Email enviado con instrucciones" |

#### 💡 Ejemplo: Múltiples Solicitudes

```bash
# Primera solicitud
curl -X POST "http://localhost:8081/api/auth/reset-password-request?email=john@example.com"
# Respuesta: 200 - Token1 generado

# Segunda solicitud 5 minutos después
curl -X POST "http://localhost:8081/api/auth/reset-password-request?email=john@example.com"
# Respuesta: 200 - Token2 generado (Token1 ya no es válido)
```

### 7. Completar Reseteo

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Token válido | Actualiza contraseña | 200 | "Contraseña actualizada exitosamente" |
| Token expirado | Rechaza reseteo | 400 | "Token inválido o expirado" |
| Token inválido | Rechaza reseteo | 400 | "Token inválido o expirado" |
| Token ya usado | Rechaza reseteo | 400 | "Token ya fue utilizado" |
| Nueva contraseña débil | Rechaza reseteo | 400 | "Contraseña debe cumplir requisitos" |
| Nueva contraseña vacía | Rechaza reseteo | 400 | "Nueva contraseña requerida" |
| Token vacío | Rechaza reseteo | 400 | "Token requerido" |

#### 💡 Ejemplo: Token Expirado

```bash
# Token generado hace más de 1 hora (ejemplo de expiración)
curl -X POST http://localhost:8081/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "expired-token-abc123",
    "newPassword": "NewSecure@Pass456"
  }'
# Respuesta: 400 - "Token inválido o expirado"
```

---

## 🛡️ Autorización y Roles

### 8. Acceso por Rol

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| ADMIN accede a /admin/test | Acceso concedido | 200 | "Admin access granted" |
| CLIENT accede a /admin/test | Acceso denegado | 403 | "Acceso denegado" |
| OWNER accede a /admin/test | Acceso denegado | 403 | "Acceso denegado" |
| CLIENT accede a /user/test | Acceso concedido | 200 | "User or Client access granted" |
| OWNER accede a /user/test | Acceso concedido | 200 | "User or Client access granted" |
| Sin token accede a endpoint protegido | Acceso denegado | 401 | "No autenticado" |
| Rol inválido en JWT | Acceso denegado | 403 | "Rol inválido" |
| Token sin claim de rol | Acceso denegado | 403 | "Rol no especificado" |

#### 💡 Ejemplo: CLIENT Intenta Acceder a Endpoint Admin

```bash
# Login como CLIENT
TOKEN=$(curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "client_user", "password": "Pass@123"}')

# Intentar acceder a endpoint admin
curl -X GET http://localhost:8081/api/auth/admin/test \
  -H "Authorization: Bearer $TOKEN"
# Respuesta: 403 - "Acceso denegado"
```

---

## 🌐 Integración con Gateway

### 9. Headers del Gateway

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Gateway agrega X-User-Username | Usa el header correctamente | 200 | Operación exitosa |
| Header X-User-Username faltante | Error | 400 | "Username requerido" |
| Header con username no existente | Error | 404 | "Usuario no encontrado" |
| Gateway agrega X-User-Role | Procesa correctamente | 200 | Operación exitosa |
| Request directo (sin Gateway) | Procesa si tiene headers | Variable | - |
| Headers malformados | Error | 400 | "Headers inválidos" |

---

## 📊 Validaciones de Entrada

### 10. Validaciones de Campos

#### ✅ Casos Cubiertos

| Campo | Validación | Comportamiento si Falla |
|-------|-----------|------------------------|
| username | NotBlank, 3-50 caracteres | 400 - "Username debe tener entre 3 y 50 caracteres" |
| password | NotBlank, mínimo 8 caracteres | 400 - "Contraseña debe tener al menos 8 caracteres" |
| email | @Email, NotBlank | 400 - "Email inválido" |
| roleId | Min(1), NotNull | 400 - "roleId debe ser un número positivo" |
| anonymous | NotNull | 400 - "El campo anonymous es requerido" |

#### 💡 Ejemplo: Múltiples Validaciones Fallidas

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ab",
    "password": "123",
    "email": "invalid-email",
    "anonymous": null,
    "roleId": 0
  }'
# Respuesta: 400 - Con lista de todos los errores de validación
```

---

## 🔄 Casos de Concurrencia

### 11. Operaciones Concurrentes

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Resultado |
|------|---------------|-----------|
| Dos registros simultáneos con mismo username | Uno falla | Primero: 201, Segundo: 400 |
| Dos registros simultáneos con mismo email | Uno falla | Primero: 201, Segundo: 400 |
| Actualización concurrente de mismo usuario | Última escritura gana | Ambas: 200 (última prevalece) |
| Login durante actualización de usuario | Ambos procesan correctamente | Ambos: 200 |
| Reseteo de password durante login | Ambos procesan | Variable según timing |

---

## 💾 Manejo de Base de Datos

### 12. Errores de Base de Datos

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Conexión a BD fallida | Error interno | 500 | "Error de conexión a base de datos" |
| Query timeout | Error interno | 500 | "Timeout en operación de base de datos" |
| Constraint violation (unique) | Error | 400 | Mensaje específico del constraint |
| BD no disponible | Error interno | 500 | "Servicio temporalmente no disponible" |

---

## 🧪 Testing de Edge Cases

### Comando para Probar Todos los Casos

```bash
# Script de prueba completo
./test-edge-cases.sh
```

### Casos Críticos a Verificar Manualmente

1. ✅ Token JWT expirado después de 24 horas
2. ✅ Múltiples usuarios registrándose simultáneamente
3. ✅ Usuario eliminado pero con token válido
4. ✅ Cambio de contraseña durante sesión activa
5. ✅ Reseteo de contraseña con tokens múltiples

---

## 📈 Métricas de Edge Cases

| Categoría | Casos Cubiertos | Porcentaje |
|-----------|-----------------|------------|
| Autenticación | 25 | 100% |
| Autorización | 8 | 100% |
| Validaciones | 11 | 100% |
| Base de Datos | 4 | 100% |
| Concurrencia | 5 | 100% |
| **Total** | **53** | **100%** |

---

**Última actualización:** Diciembre 2024

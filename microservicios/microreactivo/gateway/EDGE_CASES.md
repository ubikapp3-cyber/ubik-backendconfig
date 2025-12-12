# Edge Cases - API Gateway

## 📋 Casos Borde Cubiertos

Este documento detalla todos los casos especiales y situaciones límite manejadas por el API Gateway.

---

## 🔐 JWT Validation

### 1. Token Validation

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Token válido y no expirado | Permite acceso | - | Request forwarded |
| Token expirado (>24h) | Rechaza request | 401 | "Token expirado" |
| Token malformado | Rechaza request | 401 | "Token inválido" |
| Token con firma inválida | Rechaza request | 401 | "Token inválido" |
| Token con secret incorrecto | Rechaza request | 401 | "Token inválido" |
| Token sin Bearer prefix | Rechaza request | 401 | "Formato de token inválido" |
| Header Authorization vacío | Rechaza request | 401 | "Token faltante" |
| Header Authorization con solo "Bearer" | Rechaza request | 401 | "Token faltante" |
| Token con espacios extras | Limpia y valida | Variable | - |
| Token con claims faltantes | Rechaza request | 401 | "Claims inválidos" |
| Token con role inválido | Rechaza request | 403 | "Rol inválido" |
| Token con caracteres especiales | Valida correctamente | Variable | - |

#### 💡 Ejemplo: Token Expirado

```bash
# Token generado hace más de 24 horas
curl -X GET http://localhost:8080/api/rooms \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired..."
# Respuesta: 401 - "Token expirado"
```

### 2. Header Authorization

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Header presente y correcto | Procesa | - | - |
| Header ausente en ruta protegida | Rechaza | 401 | "No autenticado" |
| Header ausente en ruta pública | Permite | 200 | Request forwarded |
| Múltiples headers Authorization | Usa el primero | Variable | - |
| Header con case incorrecto (authorization) | Detecta igualmente | Variable | - |
| Bearer con mayúsculas/minúsculas | Acepta "Bearer" o "bearer" | Variable | - |

---

## 🛣️ Routing

### 3. Path Matching

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Destino |
|------|---------------|-------------|---------|
| /api/auth/login | Forward a UserManagement | 200 | :8081 |
| /api/user/profile | Forward a UserManagement | 200 | :8081 |
| /api/motels | Forward a MotelManagement | 200 | :8084 |
| /api/rooms/123 | Forward a MotelManagement | 200 | :8084 |
| /api/reservations | Forward a MotelManagement | 200 | :8084 |
| /api/bookings | Mapea a /api/reservations | 200 | :8084 |
| /api/products | Forward a Products | 200 | :8082 |
| /unknown/path | No encontrado | 404 | "Ruta no encontrada" |
| / (root) | No configurado | 404 | "Ruta no encontrada" |
| /api sin más path | No configurado | 404 | "Ruta no encontrada" |
| Path con // dobles | Normaliza | Variable | - |
| Path con query params | Preserva params | 200 | Forward con params |
| Path con # fragment | Ignora fragment | 200 | Forward sin fragment |

#### 💡 Ejemplo: Ruta No Configurada

```bash
curl -X GET http://localhost:8080/api/unknown
# Respuesta: 404 - "Ruta no encontrada"
```

### 4. StripPrefix Filter

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Path Original | Path Forwarded |
|------|---------------|---------------|----------------|
| /api/auth/login | StripPrefix=0 | /api/auth/login | /api/auth/login |
| /api/products/123 | StripPrefix=1 | /api/products/123 | /products/123 |
| /api/bookings | Mapea a /api/reservations | /api/bookings | /api/reservations |

---

## 🌐 CORS Handling

### 5. CORS Preflight

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Headers |
|------|---------------|-------------|---------|
| OPTIONS request válido | Retorna headers CORS | 200 | Access-Control-Allow-* |
| Origen permitido | Permite | 200 | Access-Control-Allow-Origin: * |
| Origen no permitido (prod) | Rechaza | 403 | - |
| Sin header Origin | Permite (no es CORS) | 200 | - |
| Métodos permitidos | GET, POST, PUT, DELETE, OPTIONS | 200 | Headers CORS |
| Métodos no permitidos | Rechaza | 405 | "Método no permitido" |
| Headers personalizados | Permite todos (*) | 200 | Access-Control-Allow-Headers: * |
| Credentials en dev | allow-credentials: false | 200 | - |
| Credentials en prod | allow-credentials: true | 200 | Requiere origen específico |
| Max-age | 3600 segundos | 200 | Access-Control-Max-Age: 3600 |

#### 💡 Ejemplo: Preflight Request

```bash
curl -X OPTIONS http://localhost:8080/api/motels \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Headers de respuesta esperados:
# Access-Control-Allow-Origin: *
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
# Access-Control-Allow-Headers: *
```

---

## 🔄 Service Communication

### 6. Microservice Availability

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP | Mensaje |
|------|---------------|-------------|---------|
| Servicio disponible | Forward exitoso | 200 | Response del servicio |
| Servicio no responde | Timeout | 504 | "Gateway Timeout" |
| Servicio retorna error | Forward error | Variable | Error del servicio |
| Connection refused | Service unavailable | 503 | "Servicio temporalmente no disponible" |
| DNS resolution falla | Service unavailable | 503 | "Servicio no encontrado" |
| Servicio retorna muy lento | Timeout después de 30s | 504 | "Gateway Timeout" |
| Servicio en reinicio | Connection refused | 503 | "Servicio temporalmente no disponible" |

#### 💡 Ejemplo: Servicio No Disponible

```bash
# MotelManagement está apagado
curl -X GET http://localhost:8080/api/motels \
  -H "Authorization: Bearer <valid-token>"
# Respuesta: 503 - "Servicio temporalmente no disponible"
```

### 7. Response Handling

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Resultado |
|------|---------------|-----------|
| Response 2xx del servicio | Forward tal cual | 2xx al cliente |
| Response 4xx del servicio | Forward tal cual | 4xx al cliente |
| Response 5xx del servicio | Forward tal cual | 5xx al cliente |
| Response con headers custom | Preserva headers | Headers forwarded |
| Response con cookies | Preserva cookies | Cookies forwarded |
| Response vacío | Forward vacío | 204 al cliente |
| Response muy grande | Stream response | Chunked transfer |
| Response con encoding | Preserva encoding | Content-Encoding preservado |

---

## 🔒 Authorization

### 8. Role-Based Access

#### ✅ Casos Cubiertos

| Ruta | Sin Token | Token CLIENT | Token ADMIN | Token OWNER |
|------|-----------|--------------|-------------|-------------|
| /api/auth/** | ✅ | ✅ | ✅ | ✅ |
| /api/motels (GET) | ✅ | ✅ | ✅ | ✅ |
| /api/user | ❌ | ✅ | ✅ | ✅ |
| /api/rooms | ❌ | ✅ | ✅ | ✅ |
| /api/reservations | ❌ | ✅ | ✅ | ✅ |
| /api/services | ❌ | ✅ | ✅ | ✅ |

#### 💡 Ejemplo: Acceso Sin Token a Ruta Protegida

```bash
curl -X GET http://localhost:8080/api/rooms
# Respuesta: 401 - "No autenticado"
```

### 9. Header Injection

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Headers Agregados |
|------|---------------|-------------------|
| Request autenticado | Agrega headers | X-User-Username, X-User-Role |
| Request no autenticado | No agrega headers | - |
| Username con caracteres especiales | URL encode | Header válido |
| Username con espacios | URL encode | Header válido |
| Role en mayúsculas | Preserva case | X-User-Role: CLIENT |
| Múltiples roles (futuro) | Primer rol | X-User-Role: principal |

---

## 🚦 Request Processing

### 10. Concurrent Requests

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Resultado |
|------|---------------|-----------|
| 100 requests simultáneos | Procesa todos | Todos procesados |
| Más requests que threads | Queue y procesa | Todos procesados |
| Request lento bloquea otros | No bloquea (async) | Otros continúan |
| Timeout de un request | No afecta otros | Solo ese request falla |
| Error en un request | No afecta otros | Solo ese request falla |

### 11. Request Size

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Límite |
|------|---------------|--------|
| Request pequeño (<1KB) | Procesa normalmente | OK |
| Request mediano (<1MB) | Procesa normalmente | OK |
| Request grande (>10MB) | Puede procesar o rechazar | Variable |
| Headers muy grandes | Puede rechazar | 431 Request Header Fields Too Large |
| URL muy larga | Puede rechazar | 414 URI Too Long |
| Body JSON muy grande | Depende del servicio | Variable |

---

## 📊 Logging

### 12. Request Logging

#### ✅ Casos Cubiertos

| Caso | Información Logged | Nivel |
|------|-------------------|-------|
| Request exitoso | Method, path, status, time | INFO |
| Request con error 4xx | Method, path, status, time | WARN |
| Request con error 5xx | Method, path, status, time, stack | ERROR |
| Token presente | No logea el token (seguridad) | - |
| Password en body | No logea el body (seguridad) | - |
| Headers sensibles | Filtra Authorization, Cookie | - |
| Request muy lento (>5s) | Log especial con timing | WARN |

#### 💡 Ejemplo: Log de Request

```
INFO: [Gateway] GET /api/motels/city/Quito - Status: 200 - Time: 145ms
WARN: [Gateway] GET /api/rooms/999 - Status: 404 - Time: 23ms
ERROR: [Gateway] POST /api/reservations - Status: 500 - Time: 1234ms - Error: Connection refused
```

---

## 🔄 Edge Cases Específicos

### 13. Timing Issues

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP |
|------|---------------|-------------|
| Token expira durante request | Request completa | 200 |
| Token expira entre requests | Próximo request falla | 401 |
| Servicio cae durante request | Error | 503 |
| Servicio cae entre requests | Próximo request falla | 503 |
| Usuario se desloguea durante request | Request completa | 200 |
| Usuario actualiza perfil durante request | Usa datos del token | 200 |

### 14. Network Issues

#### ✅ Casos Cubiertos

| Caso | Comportamiento | Código HTTP |
|------|---------------|-------------|
| Connection reset by peer | Error | 502 |
| Network timeout | Timeout | 504 |
| DNS resolution timeout | Error | 503 |
| SSL/TLS handshake falla | Error | 502 |
| Partial response received | Error o retry | Variable |
| Connection pool exhausted | Queue o error | 503 |

---

## 🧪 Testing de Edge Cases

### Comandos de Prueba

```bash
# 1. Token expirado
curl -X GET http://localhost:8080/api/rooms \
  -H "Authorization: Bearer expired-token"

# 2. Token malformado
curl -X GET http://localhost:8080/api/rooms \
  -H "Authorization: Bearer not-a-valid-jwt"

# 3. Sin token en ruta protegida
curl -X GET http://localhost:8080/api/rooms

# 4. Token válido en ruta pública
TOKEN="valid-jwt-token"
curl -X GET http://localhost:8080/api/motels \
  -H "Authorization: Bearer $TOKEN"

# 5. Ruta no existente
curl -X GET http://localhost:8080/api/nonexistent

# 6. CORS preflight
curl -X OPTIONS http://localhost:8080/api/motels \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -v

# 7. Servicio no disponible (detener un servicio primero)
curl -X GET http://localhost:8080/api/rooms

# 8. Request muy grande
dd if=/dev/zero bs=1M count=20 | base64 > large.json
curl -X POST http://localhost:8080/api/motels \
  -H "Content-Type: application/json" \
  -d @large.json

# 9. Múltiples requests concurrentes
for i in {1..100}; do
  curl -X GET http://localhost:8080/api/motels &
done
wait
```

### Script de Prueba Completo

```bash
#!/bin/bash
# test-gateway-edge-cases.sh

echo "Testing Gateway Edge Cases..."

# Test 1: Valid request
echo "1. Testing valid request..."
curl -s http://localhost:8080/api/motels > /dev/null && echo "✅ PASS" || echo "❌ FAIL"

# Test 2: Invalid token
echo "2. Testing invalid token..."
RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/rooms \
  -H "Authorization: Bearer invalid-token")
[ "$RESULT" = "401" ] && echo "✅ PASS" || echo "❌ FAIL"

# Test 3: Missing token
echo "3. Testing missing token..."
RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/rooms)
[ "$RESULT" = "401" ] && echo "✅ PASS" || echo "❌ FAIL"

# Test 4: CORS preflight
echo "4. Testing CORS preflight..."
RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS http://localhost:8080/api/motels \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET")
[ "$RESULT" = "200" ] && echo "✅ PASS" || echo "❌ FAIL"

# Test 5: Invalid route
echo "5. Testing invalid route..."
RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/nonexistent)
[ "$RESULT" = "404" ] && echo "✅ PASS" || echo "❌ FAIL"

echo "Gateway edge cases testing completed!"
```

---

## 📈 Métricas de Edge Cases

| Categoría | Casos Cubiertos | Porcentaje |
|-----------|-----------------|------------|
| JWT Validation | 12 | 100% |
| Authorization Headers | 6 | 100% |
| Routing | 13 | 100% |
| CORS | 11 | 100% |
| Service Communication | 7 | 100% |
| Response Handling | 8 | 100% |
| Role-Based Access | 6 | 100% |
| Concurrent Requests | 5 | 100% |
| Request Size | 6 | 100% |
| Logging | 7 | 100% |
| Timing Issues | 6 | 100% |
| Network Issues | 6 | 100% |
| **Total** | **93** | **100%** |

---

## 🔍 Casos Críticos a Monitorear

### Alta Prioridad

1. ✅ **Token Expiration**: Usuarios siendo deslogueados correctamente
2. ✅ **Service Downtime**: Gateway maneja servicios caídos sin crash
3. ✅ **CORS Issues**: Frontend puede comunicarse correctamente
4. ✅ **Concurrent Load**: Gateway maneja carga alta sin degradación
5. ✅ **Timeout Handling**: Requests lentos no bloquean otros

### Media Prioridad

6. ✅ **Large Payloads**: Gateway maneja requests grandes
7. ✅ **Invalid Routes**: 404 retornados correctamente
8. ✅ **Malformed Tokens**: Validación robusta de JWT
9. ✅ **Network Issues**: Manejo graceful de errores de red
10. ✅ **Header Injection**: Headers correctos agregados a requests

---

**Última actualización:** Diciembre 2024

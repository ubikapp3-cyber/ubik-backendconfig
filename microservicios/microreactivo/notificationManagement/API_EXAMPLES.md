# API Examples - Notification Management Service

This document contains examples of how to use the Notification Management API using curl commands.

## Base URL
```
http://localhost:8085/api/notifications
```

## Examples

### 1. Create a Notification

```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nueva reserva confirmada",
    "message": "Tu reserva para la habitación 101 ha sido confirmada",
    "type": "BOOKING",
    "recipient": "user123",
    "recipientType": "USER",
    "metadata": "{\"roomId\": 101, \"checkIn\": \"2024-01-15\"}"
  }'
```

### 2. Get All Notifications

```bash
curl -X GET http://localhost:8085/api/notifications
```

### 3. Get Notification by ID

```bash
curl -X GET http://localhost:8085/api/notifications/1
```

### 4. Get Notifications by Recipient

```bash
curl -X GET http://localhost:8085/api/notifications/recipient/user123
```

### 5. Get Notifications by Type

```bash
curl -X GET http://localhost:8085/api/notifications/type/BOOKING
```

### 6. Get Notifications by Status

```bash
curl -X GET http://localhost:8085/api/notifications/status/PENDING
```

### 7. Send a Notification (Mark as Sent)

```bash
curl -X POST http://localhost:8085/api/notifications/1/send
```

### 8. Mark Notification as Read

```bash
curl -X POST http://localhost:8085/api/notifications/1/read
```

### 9. Update a Notification

```bash
curl -X PUT http://localhost:8085/api/notifications/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nueva reserva actualizada",
    "message": "Tu reserva ha sido modificada",
    "type": "BOOKING"
  }'
```

### 10. Delete a Notification

```bash
curl -X DELETE http://localhost:8085/api/notifications/1
```

### 11. Get Unread Notifications Count for a Recipient

```bash
curl -X GET http://localhost:8085/api/notifications/recipient/user123/unread-count
```

## Complete Workflow Example

### Step 1: Create a Welcome Notification
```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "¡Bienvenido a Ubik!",
    "message": "Gracias por registrarte en nuestra plataforma. Estamos encantados de tenerte con nosotros.",
    "type": "WELCOME",
    "recipient": "newuser456",
    "recipientType": "USER",
    "metadata": "{\"registrationDate\": \"2024-01-10\"}"
  }'
```

Response (example):
```json
{
  "id": 10,
  "title": "¡Bienvenido a Ubik!",
  "message": "Gracias por registrarte en nuestra plataforma. Estamos encantados de tenerte con nosotros.",
  "type": "WELCOME",
  "recipient": "newuser456",
  "recipientType": "USER",
  "status": "PENDING",
  "createdAt": "2024-01-10T10:30:00",
  "sentAt": null,
  "readAt": null,
  "metadata": "{\"registrationDate\": \"2024-01-10\"}"
}
```

### Step 2: Send the Notification
```bash
curl -X POST http://localhost:8085/api/notifications/10/send
```

Response (example):
```json
{
  "id": 10,
  "title": "¡Bienvenido a Ubik!",
  "message": "Gracias por registrarte en nuestra plataforma. Estamos encantados de tenerte con nosotros.",
  "type": "WELCOME",
  "recipient": "newuser456",
  "recipientType": "USER",
  "status": "SENT",
  "createdAt": "2024-01-10T10:30:00",
  "sentAt": "2024-01-10T10:35:00",
  "readAt": null,
  "metadata": "{\"registrationDate\": \"2024-01-10\"}"
}
```

### Step 3: Check Unread Notifications
```bash
curl -X GET http://localhost:8085/api/notifications/recipient/newuser456/unread-count
```

Response (example):
```json
1
```

### Step 4: Get User's Notifications
```bash
curl -X GET http://localhost:8085/api/notifications/recipient/newuser456
```

### Step 5: Mark as Read
```bash
curl -X POST http://localhost:8085/api/notifications/10/read
```

## Testing with Different Notification Types

### Payment Reminder
```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Recordatorio de pago",
    "message": "Tu factura de $150.00 vence en 3 días",
    "type": "PAYMENT",
    "recipient": "user789",
    "recipientType": "USER",
    "metadata": "{\"invoiceId\": \"INV-2024-001\", \"amount\": 150.00}"
  }'
```

### System Notification (Broadcast)
```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Mantenimiento programado",
    "message": "El sistema estará en mantenimiento el 15 de enero de 2:00 AM a 4:00 AM",
    "type": "SYSTEM",
    "recipient": "all",
    "recipientType": "BROADCAST",
    "metadata": "{\"maintenanceWindow\": \"2024-01-15T02:00:00/2024-01-15T04:00:00\"}"
  }'
```

### Email Verification
```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Confirma tu correo electrónico",
    "message": "Por favor haz clic en el enlace para verificar tu dirección de correo",
    "type": "VERIFICATION",
    "recipient": "user999",
    "recipientType": "USER",
    "metadata": "{\"verificationToken\": \"abc123xyz789\"}"
  }'
```

## Error Handling Examples

### 1. Try to update a sent notification (should fail)
```bash
curl -X PUT http://localhost:8085/api/notifications/10 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Trying to update",
    "message": "This should fail",
    "type": "TEST"
  }'
```

Expected error: Status 409 (Conflict) - "Solo se pueden actualizar notificaciones en estado PENDING"

### 2. Try to send an already sent notification
```bash
curl -X POST http://localhost:8085/api/notifications/10/send
```

Expected error: Status 409 (Conflict) - "La notificación ya ha sido enviada"

### 3. Try to create notification with missing fields
```bash
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "message": "Test"
  }'
```

Expected error: Status 400 (Bad Request) with validation errors

## Swagger UI

For interactive API documentation and testing, visit:
```
http://localhost:8085/swagger-ui.html
```

## OpenAPI JSON

To get the OpenAPI specification in JSON format:
```
http://localhost:8085/v3/api-docs
```

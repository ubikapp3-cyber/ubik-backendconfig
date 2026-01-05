# Chatbot Service API Documentation

## Overview

The Chatbot Service provides an intelligent virtual assistant for the motel management system. It allows users to query information about reservations, rooms, and motels through natural language conversations. Administrators have additional capabilities to manage the motel through the chatbot.

## Features

### For All Users
- Query reservation information
- Check available rooms
- Get motel information
- View general help and commands

### For Administrators
- View all reservations in the system
- Get guidance for creating rooms
- Access admin management commands
- View comprehensive system information

### Security
- **Role-based access control**: Different capabilities based on user role (USER vs ADMIN)
- **Data filtering**: Sensitive information is filtered based on user permissions
- **Context-based authorization**: Each session tracks user context for proper authorization
- **No confidential data leakage**: Users can only see their own reservations; admins see limited system data

## API Endpoints

### 1. Create Chat Session

**Endpoint:** `POST /api/chatbot/sessions`

**Description:** Creates a new chat session for a user. A session must be created before sending messages.

**Request Body:**
```json
{
  "userId": 1,
  "userRole": "USER"
}
```

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "userRole": "USER",
  "startedAt": "2026-01-05T19:00:00",
  "lastActivityAt": "2026-01-05T19:00:00",
  "status": "ACTIVE",
  "context": {}
}
```

**Status Codes:**
- `201 Created` - Session created successfully
- `400 Bad Request` - Invalid input data

---

### 2. Send Message to Chatbot

**Endpoint:** `POST /api/chatbot/message`

**Description:** Sends a message to the chatbot and receives an intelligent response based on the message intent.

**Request Body:**
```json
{
  "sessionId": 1,
  "message": "Quiero ver mis reservas",
  "userId": 1,
  "userRole": "USER"
}
```

**Response:**
```json
{
  "id": 1,
  "sessionId": 1,
  "message": "Quiero ver mis reservas",
  "response": "🎯 Tus reservaciones:\n\n🔹 Reservación #1\n   Estado: Confirmada\n   Check-in: 05/01/2026 14:00\n   Check-out: 06/01/2026 12:00\n   Total: $150.00\n\n",
  "messageType": "BOT_RESPONSE",
  "timestamp": "2026-01-05T19:01:00"
}
```

**Status Codes:**
- `200 OK` - Message processed successfully
- `400 Bad Request` - Invalid input data
- `404 Not Found` - Session not found

---

### 3. Get Session

**Endpoint:** `GET /api/chatbot/sessions/{sessionId}`

**Description:** Retrieves information about a specific chat session.

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "userRole": "USER",
  "startedAt": "2026-01-05T19:00:00",
  "lastActivityAt": "2026-01-05T19:05:00",
  "status": "ACTIVE",
  "context": {}
}
```

**Status Codes:**
- `200 OK` - Session found
- `404 Not Found` - Session not found

---

### 4. Get Session History

**Endpoint:** `GET /api/chatbot/sessions/{sessionId}/history`

**Query Parameters:**
- `limit` (optional): Maximum number of messages to return (default: 100, max: 500)

**Description:** Retrieves the complete message history for a chat session.

**Response:**
```json
[
  {
    "id": 1,
    "sessionId": 1,
    "message": "hola",
    "response": "¡Hola! Soy el asistente virtual de gestión de moteles...",
    "messageType": "BOT_RESPONSE",
    "timestamp": "2026-01-05T19:00:00"
  },
  {
    "id": 2,
    "sessionId": 1,
    "message": "mis reservas",
    "response": "🎯 Tus reservaciones:\n\n...",
    "messageType": "BOT_RESPONSE",
    "timestamp": "2026-01-05T19:01:00"
  }
]
```

**Status Codes:**
- `200 OK` - History retrieved successfully

---

### 5. Close Session

**Endpoint:** `PATCH /api/chatbot/sessions/{sessionId}/close`

**Description:** Marks a session as closed. The history is preserved but no more messages can be sent.

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "userRole": "USER",
  "startedAt": "2026-01-05T19:00:00",
  "lastActivityAt": "2026-01-05T19:10:00",
  "status": "CLOSED",
  "context": {}
}
```

**Status Codes:**
- `200 OK` - Session closed successfully

---

### 6. Delete Session

**Endpoint:** `DELETE /api/chatbot/sessions/{sessionId}`

**Description:** Permanently deletes a session and all its message history.

**Status Codes:**
- `204 No Content` - Session deleted successfully

---

## Supported Commands

### General Commands (All Users)

| Command | Description | Example |
|---------|-------------|---------|
| `hola`, `buenos días` | Greets the user | "hola" |
| `ayuda`, `help` | Shows available commands | "ayuda" |
| `mis reservas` | Shows user's reservations | "quiero ver mis reservas" |
| `habitaciones disponibles` | Shows available rooms | "habitaciones disponibles" |
| `información del motel` | Shows motel information | "motel" |

### Admin Commands (Administrators Only)

| Command | Description | Example |
|---------|-------------|---------|
| `todas las reservas` | Shows all system reservations | "quiero ver todas las reservas" |
| `crear habitación` | Provides guidance for creating rooms | "crear habitacion" |
| `panel de administración` | Shows admin panel information | "panel" |

## Intent Classification

The chatbot uses keyword-based intent classification to understand user requests:

1. **Greeting Intent**: Welcomes the user
2. **Help Intent**: Provides available commands
3. **Reservation Query**: Retrieves reservation information
4. **Room Query**: Shows available rooms
5. **Motel Query**: Displays motel information
6. **Admin Commands**: Special administrative functions (admin only)

## Security Features

### 1. Role-Based Access Control
- User sessions track the user's role (USER or ADMIN)
- Admins have access to additional commands and system-wide data
- Regular users can only access their own data

### 2. Data Filtering
- **User reservations**: Users only see their own reservations
- **Admin reservations**: Admins see limited reservation data (last 20, with filtered information)
- **Sensitive data**: Internal IDs and sensitive fields are filtered for non-admin users

### 3. Context Security
- Each session maintains its own context
- Context is used to authorize requests
- Sessions can be closed or deleted to prevent unauthorized access

## Database Schema

### chat_sessions Table
```sql
CREATE TABLE chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    context TEXT
);
```

### chat_messages Table
```sql
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    response TEXT,
    message_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
);
```

## Example Usage Flows

### Example 1: User Checks Their Reservations

```bash
# 1. Create session
POST /api/chatbot/sessions
{
  "userId": 1,
  "userRole": "USER"
}

# Response: { "id": 1, ... }

# 2. Send message
POST /api/chatbot/message
{
  "sessionId": 1,
  "message": "mis reservas",
  "userId": 1,
  "userRole": "USER"
}

# Response: Shows user's reservations
```

### Example 2: Admin Queries System Information

```bash
# 1. Create admin session
POST /api/chatbot/sessions
{
  "userId": 5,
  "userRole": "ADMIN"
}

# 2. Get help
POST /api/chatbot/message
{
  "sessionId": 2,
  "message": "ayuda",
  "userId": 5,
  "userRole": "ADMIN"
}

# Response: Shows admin and user commands

# 3. View all reservations
POST /api/chatbot/message
{
  "sessionId": 2,
  "message": "todas las reservas",
  "userId": 5,
  "userRole": "ADMIN"
}

# Response: Shows system reservations (limited to 20)
```

### Example 3: Check Available Rooms

```bash
POST /api/chatbot/message
{
  "sessionId": 1,
  "message": "habitaciones disponibles",
  "userId": 1,
  "userRole": "USER"
}

# Response: Lists available rooms with details
```

## Error Handling

The chatbot gracefully handles various scenarios:

- **Unknown intent**: Returns a helpful message suggesting to type 'ayuda'
- **No data found**: Provides appropriate messages (e.g., "No tienes reservaciones registradas")
- **Session not found**: Returns 404 with error message
- **Invalid input**: Returns 400 with validation errors

## Integration with Frontend

To integrate the chatbot in your frontend:

1. Create a session when the user opens the chat
2. Send messages through the `/api/chatbot/message` endpoint
3. Display the response in the chat UI
4. Optionally, load history when reopening an existing session
5. Close the session when the user is done

Example JavaScript/TypeScript integration:

```typescript
// Create session
const sessionResponse = await fetch('/api/chatbot/sessions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ userId: currentUserId, userRole: currentUserRole })
});
const session = await sessionResponse.json();

// Send message
const messageResponse = await fetch('/api/chatbot/message', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    sessionId: session.id,
    message: userInput,
    userId: currentUserId,
    userRole: currentUserRole
  })
});
const chatMessage = await messageResponse.json();
console.log(chatMessage.response); // Display to user
```

## Testing

Unit tests are provided in `ChatbotServiceTest.java` covering:
- Session creation
- Message processing
- Intent classification
- Role-based responses
- Session management

Run tests with:
```bash
./mvnw test -Dtest=ChatbotServiceTest
```

## Future Enhancements

Potential improvements for the chatbot:
- Natural Language Processing (NLP) integration for better intent recognition
- Multi-language support
- Voice input/output capabilities
- Integration with external AI services (OpenAI, etc.)
- Advanced context management with conversation memory
- Scheduled task creation through chat
- Real-time notifications

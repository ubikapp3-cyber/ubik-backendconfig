-- Script de inicialización de PostgreSQL para el servicio de notificaciones
-- Este script se ejecuta automáticamente cuando el contenedor de PostgreSQL se inicia por primera vez

-- Crear la base de datos si no existe
SELECT 'CREATE DATABASE notification_management_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_management_db')\gexec

-- Conectar a la base de datos
\c notification_management_db;

-- Crear tabla de notificaciones
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(100) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    metadata TEXT,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'READ', 'FAILED', 'CANCELLED'))
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status ON notifications(recipient, status);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);

-- Datos de ejemplo
INSERT INTO notifications (title, message, type, recipient, recipient_type, status, created_at)
SELECT * FROM (VALUES
    ('Bienvenida al sistema', 'Gracias por registrarte en nuestra plataforma', 'WELCOME', 'user123', 'USER', 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('Nueva reserva', 'Tienes una nueva reserva para el 25 de diciembre', 'BOOKING', 'user123', 'USER', 'SENT', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('Recordatorio de pago', 'Tu factura vence en 3 días', 'PAYMENT', 'user456', 'USER', 'PENDING', CURRENT_TIMESTAMP),
    ('Actualización del sistema', 'El sistema estará en mantenimiento esta noche', 'SYSTEM', 'all', 'BROADCAST', 'SENT', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
    ('Confirmación de email', 'Por favor confirma tu dirección de correo electrónico', 'VERIFICATION', 'user789', 'USER', 'PENDING', CURRENT_TIMESTAMP)
) AS v(title, message, type, recipient, recipient_type, status, created_at)
WHERE NOT EXISTS (SELECT 1 FROM notifications);

-- Mensaje de confirmación
\echo 'Base de datos notification_management_db inicializada correctamente'

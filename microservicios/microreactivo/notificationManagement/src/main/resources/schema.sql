-- Script de inicialización de la base de datos para el servicio de notificaciones

DROP TABLE IF EXISTS notifications CASCADE;

-- Tabla de notificaciones
CREATE TABLE notifications (
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

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_recipient_status ON notifications(recipient, status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Datos de ejemplo para pruebas
INSERT INTO notifications (title, message, type, recipient, recipient_type, status, created_at) VALUES
('Bienvenida al sistema', 'Gracias por registrarte en nuestra plataforma', 'WELCOME', 'user123', 'USER', 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('Nueva reserva', 'Tienes una nueva reserva para el 25 de diciembre', 'BOOKING', 'user123', 'USER', 'SENT', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('Recordatorio de pago', 'Tu factura vence en 3 días', 'PAYMENT', 'user456', 'USER', 'PENDING', CURRENT_TIMESTAMP),
('Actualización del sistema', 'El sistema estará en mantenimiento esta noche', 'SYSTEM', 'all', 'BROADCAST', 'SENT', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
('Confirmación de email', 'Por favor confirma tu dirección de correo electrónico', 'VERIFICATION', 'user789', 'USER', 'PENDING', CURRENT_TIMESTAMP);

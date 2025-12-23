-- Script de inicialización de la base de datos para tests (H2)

DROP TABLE IF EXISTS notifications CASCADE;

-- Tabla de notificaciones
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Datos de ejemplo para pruebas (compatible con H2)
INSERT INTO notifications (title, message, type, recipient, recipient_type, status, created_at) VALUES
('Bienvenida al sistema', 'Gracias por registrarte en nuestra plataforma', 'WELCOME', 'user123', 'USER', 'SENT', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
('Nueva reserva', 'Tienes una nueva reserva para el 25 de diciembre', 'BOOKING', 'user123', 'USER', 'SENT', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
('Recordatorio de pago', 'Tu factura vence en 3 días', 'PAYMENT', 'user456', 'USER', 'PENDING', CURRENT_TIMESTAMP),
('Actualización del sistema', 'El sistema estará en mantenimiento esta noche', 'SYSTEM', 'all', 'BROADCAST', 'SENT', DATEADD('HOUR', -5, CURRENT_TIMESTAMP)),
('Confirmación de email', 'Por favor confirma tu dirección de correo electrónico', 'VERIFICATION', 'user789', 'USER', 'PENDING', CURRENT_TIMESTAMP);

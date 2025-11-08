-- Base de datos para Reservas con integración de usuarios

DROP TABLE IF EXISTS reservation CASCADE;

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    
    -- Referencias a otros microservicios
    room_id BIGINT NOT NULL,
    motel_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,  -- Del microservicio de usuarios
    
    -- Información del cliente
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100),
    customer_phone VARCHAR(20),
    customer_document VARCHAR(50),
    
    -- Fechas
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP NOT NULL,
    
    -- Información financiera
    total_price NUMERIC(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    
    -- Estado
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    -- Información adicional
    special_requests TEXT,
    cancellation_reason TEXT,
    
    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint para evitar solapamiento
    CONSTRAINT no_overlap EXCLUDE USING GIST (
        room_id WITH =,
        tsrange(check_in, check_out) WITH &&
    ) WHERE (status IN ('CONFIRMED', 'CHECKED_IN'))
);

-- Índices
CREATE INDEX idx_reservation_room ON reservation(room_id);
CREATE INDEX idx_reservation_motel ON reservation(motel_id);
CREATE INDEX idx_reservation_username ON reservation(username);
CREATE INDEX idx_reservation_dates ON reservation(check_in, check_out);
CREATE INDEX idx_reservation_status ON reservation(status);
CREATE INDEX idx_reservation_email ON reservation(customer_email);

-- Trigger para updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_reservation_updated_at
    BEFORE UPDATE ON reservation
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Datos de prueba
INSERT INTO reservation (
    room_id, motel_id, username, customer_name, customer_email,
    customer_phone, check_in, check_out, total_price,
    payment_method, payment_status, status
) VALUES
(1, 1, 'juan.perez', 'Juan Pérez', 'juan.perez@email.com',
 '555-1111', '2025-11-15 14:00:00', '2025-11-15 18:00:00',
 150000.00, 'credit_card', 'PAID', 'CONFIRMED'),

(2, 1, 'maria.garcia', 'María García', 'maria.garcia@email.com',
 '555-2222', '2025-11-16 15:00:00', '2025-11-16 20:00:00',
 180000.00, 'debit_card', 'PAID', 'CONFIRMED');

SELECT 'Reservas insertadas:' as info, COUNT(*) as cantidad FROM reservation;
SELECT '¡Base de datos inicializada!' as mensaje;
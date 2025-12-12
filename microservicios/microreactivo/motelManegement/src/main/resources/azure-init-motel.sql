-- ============================================================================
-- Script de inicialización para Azure Database for PostgreSQL
-- Proyecto: Motel Management System
-- Base de datos: motel_management_db
-- ============================================================================
-- 
-- Instrucciones:
-- 1. Este script está optimizado para Azure Database for PostgreSQL
-- 2. Incluye configuraciones específicas de Azure
-- 3. Se puede ejecutar directamente con el script init-postgresql-azure.sh
-- 4. O manualmente con:
--    PGPASSWORD='password' psql -h servidor.postgres.database.azure.com \
--      -U usuario -d motel_management_db --set=sslmode=require \
--      -f azure-init-motel.sql
--
-- ============================================================================

-- Configurar búsqueda de esquema
SET search_path TO public;

-- Eliminar tablas si existen (para re-ejecución limpia)
DROP TABLE IF EXISTS room_service CASCADE;
DROP TABLE IF EXISTS room_image CASCADE;
DROP TABLE IF EXISTS service CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS motel CASCADE;

-- ============================================================================
-- CREACIÓN DE TABLAS
-- ============================================================================

-- Tabla de moteles
CREATE TABLE motel (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    description VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    property_id BIGINT,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comentarios para la tabla motel
COMMENT ON TABLE motel IS 'Información de los moteles en el sistema';
COMMENT ON COLUMN motel.property_id IS 'ID de la propiedad asociada en el sistema de propiedades';
COMMENT ON COLUMN motel.date_created IS 'Fecha de creación del registro';

-- Índices para motel
CREATE INDEX idx_motel_city ON motel(city);
CREATE INDEX idx_motel_property ON motel(property_id);
CREATE INDEX idx_motel_name ON motel(name);

-- Tabla de habitaciones
CREATE TABLE room (
    id BIGSERIAL PRIMARY KEY,
    motel_id BIGINT NOT NULL,
    number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    description VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    capacity INTEGER DEFAULT 2 CHECK (capacity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (motel_id) REFERENCES motel(id) ON DELETE CASCADE,
    UNIQUE (motel_id, number)
);

-- Comentarios para la tabla room
COMMENT ON TABLE room IS 'Habitaciones disponibles en cada motel';
COMMENT ON COLUMN room.number IS 'Número único de habitación dentro del motel';
COMMENT ON COLUMN room.is_available IS 'Estado de disponibilidad de la habitación';
COMMENT ON COLUMN room.capacity IS 'Capacidad máxima de personas';

-- Índices para room
CREATE INDEX idx_room_motel ON room(motel_id);
CREATE INDEX idx_room_available ON room(is_available);
CREATE INDEX idx_room_type ON room(room_type);
CREATE INDEX idx_room_price ON room(price);

-- Tabla de imágenes de habitaciones
CREATE TABLE room_image (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
);

-- Comentarios para la tabla room_image
COMMENT ON TABLE room_image IS 'Imágenes de las habitaciones';
COMMENT ON COLUMN room_image.display_order IS 'Orden de visualización de la imagen';

-- Índices para room_image
CREATE INDEX idx_room_image_room ON room_image(room_id);
CREATE INDEX idx_room_image_order ON room_image(room_id, display_order);

-- Tabla de servicios
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comentarios para la tabla service
COMMENT ON TABLE service IS 'Servicios y amenidades disponibles';
COMMENT ON COLUMN service.icon IS 'Nombre del icono de Material Icons';
COMMENT ON COLUMN service.is_active IS 'Si el servicio está activo o no';

-- Índice para service
CREATE INDEX idx_service_name ON service(name);
CREATE INDEX idx_service_active ON service(is_active);

-- Tabla intermedia para la relación muchos-a-muchos entre room y service
CREATE TABLE room_service (
    room_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, service_id),
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(id) ON DELETE CASCADE
);

-- Comentarios para la tabla room_service
COMMENT ON TABLE room_service IS 'Relación entre habitaciones y servicios disponibles';

-- Índices para room_service
CREATE INDEX idx_room_service_room ON room_service(room_id);
CREATE INDEX idx_room_service_service ON room_service(service_id);

-- ============================================================================
-- DATOS INICIALES - SERVICIOS
-- ============================================================================

INSERT INTO service (name, description, icon, is_active) VALUES
    ('Jacuzzi', 'Jacuzzi privado en la habitación', 'hot_tub', TRUE),
    ('Spa', 'Acceso a spa y sauna', 'spa', TRUE),
    ('WiFi', 'Internet de alta velocidad', 'wifi', TRUE),
    ('TV Cable', 'Televisión por cable premium', 'tv', TRUE),
    ('Minibar', 'Minibar completamente equipado', 'local_bar', TRUE),
    ('Aire Acondicionado', 'Climatización completa', 'ac_unit', TRUE),
    ('Estacionamiento', 'Garaje privado', 'local_parking', TRUE),
    ('Room Service', 'Servicio a la habitación 24/7', 'room_service', TRUE),
    ('Cama King', 'Cama tamaño King', 'king_bed', TRUE),
    ('Vista al Mar', 'Habitación con vista al mar', 'beach_access', TRUE),
    ('Balcón', 'Balcón privado', 'balcony', TRUE),
    ('Cocina', 'Cocina equipada', 'kitchen', TRUE),
    ('Desayuno', 'Desayuno incluido', 'free_breakfast', TRUE),
    ('Gimnasio', 'Acceso al gimnasio', 'fitness_center', TRUE),
    ('Piscina', 'Acceso a piscina', 'pool', TRUE)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- DATOS DE EJEMPLO - MOTELES
-- ============================================================================

INSERT INTO motel (name, address, phone_number, description, city, property_id) VALUES
    ('Motel Paraíso', 'Calle 10 #15-20', '+57-555-0100', 'Motel romántico con temática tropical y ambiente acogedor', 'Medellín', 1),
    ('Motel Las Estrellas', 'Avenida 80 #50-30', '+57-555-0200', 'Experiencia de lujo y privacidad absoluta', 'Medellín', 1),
    ('Motel El Oasis', 'Carrera 70 #45-12', '+57-555-0300', 'Tranquilidad y confort en plena ciudad', 'Bogotá', 2),
    ('Motel Vista Hermosa', 'Diagonal 25 #30-15', '+57-555-0400', 'Increíbles vistas panorámicas de la ciudad', 'Cali', 3),
    ('Motel Romance', 'Calle 45 #12-34', '+57-555-0500', 'El lugar perfecto para parejas', 'Cartagena', 4)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- DATOS DE EJEMPLO - HABITACIONES
-- ============================================================================

INSERT INTO room (motel_id, number, room_type, price, description, is_available, capacity) VALUES
-- Motel Paraíso (id=1)
(1, '101', 'Suite Ejecutiva', 150000.00, 'Suite con jacuzzi y sala de estar', TRUE, 2),
(1, '102', 'Suite Romántica', 180000.00, 'Ambiente romántico con jacuzzi doble', TRUE, 2),
(1, '103', 'Suite Presidencial', 250000.00, 'La mejor suite con todas las amenidades', TRUE, 4),
(1, '104', 'Suite Estándar', 120000.00, 'Confortable y económica', TRUE, 2),

-- Motel Las Estrellas (id=2)
(2, '201', 'Suite VIP', 200000.00, 'Suite VIP con vista panorámica', TRUE, 2),
(2, '202', 'Suite Deluxe', 220000.00, 'Suite deluxe con spa privado', TRUE, 3),
(2, '203', 'Suite Imperial', 280000.00, 'Lujo supremo y privacidad absoluta', FALSE, 4),
(2, '204', 'Suite Premium', 190000.00, 'Elegancia y confort superior', TRUE, 2),

-- Motel El Oasis (id=3)
(3, '301', 'Suite Estándar', 120000.00, 'Comodidad y buen precio', TRUE, 2),
(3, '302', 'Suite Premium', 170000.00, 'Elegancia y confort', TRUE, 2),
(3, '303', 'Suite Familiar', 200000.00, 'Ideal para familias', TRUE, 4),

-- Motel Vista Hermosa (id=4)
(4, '401', 'Suite Panorámica', 210000.00, 'Vista increíble de 180 grados', TRUE, 2),
(4, '402', 'Suite Terraza', 230000.00, 'Con terraza privada', TRUE, 3),

-- Motel Romance (id=5)
(5, '501', 'Suite Luna de Miel', 240000.00, 'Perfecta para luna de miel', TRUE, 2),
(5, '502', 'Suite Romántica Plus', 195000.00, 'Ambiente íntimo y acogedor', TRUE, 2)
ON CONFLICT (motel_id, number) DO NOTHING;

-- ============================================================================
-- DATOS DE EJEMPLO - ASOCIACIÓN HABITACIONES-SERVICIOS
-- ============================================================================

-- Suite Ejecutiva (id=1)
INSERT INTO room_service (room_id, service_id) VALUES
    (1, 1), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7)
ON CONFLICT DO NOTHING;

-- Suite Romántica (id=2)
INSERT INTO room_service (room_id, service_id) VALUES
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 11)
ON CONFLICT DO NOTHING;

-- Suite Presidencial (id=3)
INSERT INTO room_service (room_id, service_id) VALUES
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 11)
ON CONFLICT DO NOTHING;

-- Suite Estándar Paraíso (id=4)
INSERT INTO room_service (room_id, service_id) VALUES
    (4, 3), (4, 4), (4, 6)
ON CONFLICT DO NOTHING;

-- Suite VIP (id=5)
INSERT INTO room_service (room_id, service_id) VALUES
    (5, 1), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 10)
ON CONFLICT DO NOTHING;

-- Suite Deluxe (id=6)
INSERT INTO room_service (room_id, service_id) VALUES
    (6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7), (6, 8), (6, 10), (6, 11)
ON CONFLICT DO NOTHING;

-- Suite Imperial (id=7)
INSERT INTO room_service (room_id, service_id) VALUES
    (7, 1), (7, 2), (7, 3), (7, 4), (7, 5), (7, 6), (7, 7), (7, 8), (7, 9), (7, 10), (7, 11), (7, 12)
ON CONFLICT DO NOTHING;

-- Suite Premium Las Estrellas (id=8)
INSERT INTO room_service (room_id, service_id) VALUES
    (8, 1), (8, 3), (8, 4), (8, 5), (8, 6), (8, 11)
ON CONFLICT DO NOTHING;

-- Suite Estándar El Oasis (id=9)
INSERT INTO room_service (room_id, service_id) VALUES
    (9, 3), (9, 4), (9, 6)
ON CONFLICT DO NOTHING;

-- Suite Premium El Oasis (id=10)
INSERT INTO room_service (room_id, service_id) VALUES
    (10, 1), (10, 3), (10, 4), (10, 5), (10, 6), (10, 11)
ON CONFLICT DO NOTHING;

-- Suite Familiar (id=11)
INSERT INTO room_service (room_id, service_id) VALUES
    (11, 3), (11, 4), (11, 6), (11, 7), (11, 13), (11, 15)
ON CONFLICT DO NOTHING;

-- Suite Panorámica (id=12)
INSERT INTO room_service (room_id, service_id) VALUES
    (12, 1), (12, 3), (12, 4), (12, 5), (12, 6), (12, 10), (12, 11)
ON CONFLICT DO NOTHING;

-- Suite Terraza (id=13)
INSERT INTO room_service (room_id, service_id) VALUES
    (13, 1), (13, 2), (13, 3), (13, 4), (13, 5), (13, 6), (13, 10), (13, 11)
ON CONFLICT DO NOTHING;

-- Suite Luna de Miel (id=14)
INSERT INTO room_service (room_id, service_id) VALUES
    (14, 1), (14, 2), (14, 3), (14, 4), (14, 5), (14, 6), (14, 10), (14, 11), (14, 13)
ON CONFLICT DO NOTHING;

-- Suite Romántica Plus (id=15)
INSERT INTO room_service (room_id, service_id) VALUES
    (15, 1), (15, 3), (15, 4), (15, 5), (15, 6), (15, 11)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- VISTAS Y FUNCIONES ÚTILES (Opcional)
-- ============================================================================

-- Vista: Resumen de habitaciones con servicios
CREATE OR REPLACE VIEW v_room_summary AS
SELECT 
    r.id as room_id,
    r.number as room_number,
    r.room_type,
    r.price,
    r.is_available,
    m.name as motel_name,
    m.city,
    COUNT(DISTINCT rs.service_id) as service_count,
    STRING_AGG(DISTINCT s.name, ', ' ORDER BY s.name) as services
FROM room r
JOIN motel m ON r.motel_id = m.id
LEFT JOIN room_service rs ON r.id = rs.room_id
LEFT JOIN service s ON rs.service_id = s.id
GROUP BY r.id, r.number, r.room_type, r.price, r.is_available, m.name, m.city;

COMMENT ON VIEW v_room_summary IS 'Vista resumen de habitaciones con sus servicios';

-- ============================================================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- ============================================================================

DO $$
DECLARE
    motel_count INTEGER;
    room_count INTEGER;
    service_count INTEGER;
    room_service_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO motel_count FROM motel;
    SELECT COUNT(*) INTO room_count FROM room;
    SELECT COUNT(*) INTO service_count FROM service;
    SELECT COUNT(*) INTO room_service_count FROM room_service;
    
    RAISE NOTICE '';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'VERIFICACIÓN DE DATOS INSERTADOS';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Moteles insertados:      %', motel_count;
    RAISE NOTICE 'Habitaciones insertadas: %', room_count;
    RAISE NOTICE 'Servicios insertados:    %', service_count;
    RAISE NOTICE 'Relaciones room-service: %', room_service_count;
    RAISE NOTICE '============================================';
    RAISE NOTICE '';
    
    IF motel_count > 0 AND room_count > 0 AND service_count > 0 THEN
        RAISE NOTICE '✅ Base de datos inicializada correctamente!';
    ELSE
        RAISE EXCEPTION '❌ Error: Algunos datos no fueron insertados correctamente';
    END IF;
END $$;

-- ============================================================================
-- SCRIPT COMPLETADO
-- ============================================================================

SELECT '¡Base de datos de Azure PostgreSQL inicializada exitosamente!' as mensaje;

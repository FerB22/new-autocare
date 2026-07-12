-- =====================================================================================
-- AutoCare - Script de Inicialización de Bases de Datos (PostgreSQL)
-- Ejecutar este script en su servidor PostgreSQL local (Puerto 5432)
-- =====================================================================================

-- NOTA: En PostgreSQL, la creación de bases de datos debe ejecutarse una por una 
-- y fuera de bloques transaccionales. Si alguna ya existe, PostgreSQL reportará 
-- un error que puede ser ignorado de forma segura.

CREATE DATABASE autocare_booking;
CREATE DATABASE autocare_inventory;
CREATE DATABASE autocare_analytics;
CREATE DATABASE autocare_diagnostics;
CREATE DATABASE autocare_hr;
CREATE DATABASE autocare_billing;
CREATE DATABASE autocare_loyalty;
CREATE DATABASE autocare_workshop;
CREATE DATABASE autocare_procurement;
CREATE DATABASE autocare_notification;
CREATE DATABASE autocare_garage;

-- =====================================================================================
-- Seed de Datos Iniciales (Opcional)
-- Ejecutar en la base de datos respectiva después de levantar los servicios
-- =====================================================================================

-- 1. Insertar el cliente y el vehículo en garage-service (Para que pase la validación)
-- \c autocare_garage;
-- INSERT INTO clientes (id, nombre, email, telefono) VALUES (1, 'Juan Perez', 'juan@test.com', '123456');
-- INSERT INTO vehiculos (id, cliente_id, placa, marca, modelo) VALUES (10, 1, 'ABC-123', 'Toyota', 'Corolla');

-- 2. Si deseas poblar datos de prueba en la tabla de citas (después de levantar booking-service):
-- \c autocare_booking;
-- INSERT INTO citas (fecha_hora, motivo, estado, cliente_id, vehiculo_id) VALUES 
-- ('2026-12-15 10:30:00', 'Mantenimiento preventivo', 'AGENDADA', 1, 10);

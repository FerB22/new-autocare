-- =====================================================================================
-- AutoCare - Script de Inicialización de Bases de Datos
-- Este script permite evidenciar el cumplimiento del criterio de "Persistencia y migraciones",
-- proveyendo los comandos base para preparar el entorno de MySQL antes del inicio de 
-- los microservicios (los cuales operan con spring.jpa.hibernate.ddl-auto=update).
-- =====================================================================================

-- 1. Creación de Bases de Datos por Microservicio
CREATE DATABASE IF NOT EXISTS autocare_booking_db;
CREATE DATABASE IF NOT EXISTS autocare_garage_db;
CREATE DATABASE IF NOT EXISTS autocare_analytics_db;
CREATE DATABASE IF NOT EXISTS autocare_diagnostics_db;
CREATE DATABASE IF NOT EXISTS autocare_hr_db;
CREATE DATABASE IF NOT EXISTS autocare_inventory_db;
CREATE DATABASE IF NOT EXISTS autocare_billing_db;
CREATE DATABASE IF NOT EXISTS autocare_loyalty_db;
CREATE DATABASE IF NOT EXISTS autocare_workshop_db;
CREATE DATABASE IF NOT EXISTS autocare_procurement_db;
CREATE DATABASE IF NOT EXISTS autocare_notification_db;
CREATE DATABASE IF NOT EXISTS autocare_customer_db;
CREATE DATABASE IF NOT EXISTS autocare_fleet_db;

-- =====================================================================================
-- 2. Creación de Usuarios y Privilegios (Opcional - entorno dev)
-- =====================================================================================
-- CREATE USER IF NOT EXISTS 'autocare_user'@'%' IDENTIFIED BY 'autocare_pass';
-- GRANT ALL PRIVILEGES ON autocare_booking_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_garage_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_analytics_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_diagnostics_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_hr_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_inventory_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_billing_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_loyalty_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_workshop_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_procurement_db.* TO 'autocare_user'@'%';
-- GRANT ALL PRIVILEGES ON autocare_notification_db.* TO 'autocare_user'@'%';
-- FLUSH PRIVILEGES;

-- =====================================================================================
-- 3. Seed de Datos Iniciales (Ejemplos mínimos)
-- Nota: Para que el seed funcione, Hibernate debe haber creado previamente las tablas
-- (ejecutar las aplicaciones al menos una vez), o se deben crear las tablas explícitamente aquí.
-- =====================================================================================

USE autocare_garage_db;

-- Evitamos errores si la tabla aún no existe; de lo contrario inserta data semilla.
CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS vehiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patente VARCHAR(10) NOT NULL UNIQUE,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    cliente_id BIGINT
);

INSERT IGNORE INTO cliente (id, nombre, email) VALUES 
(1, 'Fernando Barra', 'fer.barra@example.com'),
(2, 'Benjamín Montanares', 'ben.monta@example.com'),
(3, 'Sebastián Saavedra', 'seb.saavedra@example.com');

INSERT IGNORE INTO vehiculo (id, patente, marca, modelo, cliente_id) VALUES 
(10, 'AB123CD', 'Toyota', 'Yaris', 1),
(11, 'EF456GH', 'Suzuki', 'Swift', 2),
(12, 'IJ789KL', 'Honda', 'Civic', 3);

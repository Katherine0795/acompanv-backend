-- OBSOLETO: este archivo usa la tabla socio_puesto, que ya no existe.
-- Use db/mercado_db.sql (esquema + datos de prueba alineados con el backend).

-- Inserts de prueba para mercado_db
-- Basado en scriptBDComerciantes.sql
-- 10 registros por tabla

USE mercado_db;

-- =========================
-- INSERTS: SOCIO
-- =========================
INSERT INTO socio (nombre, dni, telefono, estado, fecha_creacion) VALUES
('Juan Perez', '12345678', '900000001', TRUE, '2026-04-01 08:00:00'),
('Maria Lopez', '12345679', '900000002', TRUE, '2026-04-01 08:10:00'),
('Carlos Ramos', '12345680', '900000003', TRUE, '2026-04-01 08:20:00'),
('Ana Torres', '12345681', '900000004', TRUE, '2026-04-01 08:30:00'),
('Luis Mendoza', '12345682', '900000005', TRUE, '2026-04-01 08:40:00'),
('Rosa Castillo', '12345683', '900000006', TRUE, '2026-04-01 08:50:00'),
('Pedro Salazar', '12345684', '900000007', TRUE, '2026-04-01 09:00:00'),
('Lucia Fernandez', '12345685', '900000008', TRUE, '2026-04-01 09:10:00'),
('Jorge Herrera', '12345686', '900000009', TRUE, '2026-04-01 09:20:00'),
('Elena Vargas', '12345687', '900000010', TRUE, '2026-04-01 09:30:00');

-- =========================
-- INSERTS: PUESTO
-- =========================
INSERT INTO puesto (codigo, descripcion, estado, es_propiedad_asociacion) VALUES
('P001', 'Puesto de abarrotes', TRUE, FALSE),
('P002', 'Puesto de frutas', TRUE, FALSE),
('P003', 'Puesto de verduras', TRUE, FALSE),
('P004', 'Puesto de carnes', TRUE, FALSE),
('P005', 'Puesto de ropa', TRUE, FALSE),
('P006', 'Puesto de calzado', TRUE, FALSE),
('P007', 'Puesto de comida', TRUE, FALSE),
('P008', 'Puesto de accesorios', TRUE, FALSE),
('P009', 'Puesto de libreria', TRUE, TRUE),
('P010', 'Puesto de bazar', TRUE, TRUE);

-- =========================
-- INSERTS: SOCIO_PUESTO
-- =========================
INSERT INTO socio_puesto (id_socio, id_puesto, fecha_asignacion, fecha_fin) VALUES
(1, 1, '2026-01-01', NULL),
(2, 2, '2026-01-02', NULL),
(3, 3, '2026-01-03', NULL),
(4, 4, '2026-01-04', NULL),
(5, 5, '2026-01-05', NULL),
(6, 6, '2026-01-06', NULL),
(7, 7, '2026-01-07', NULL),
(8, 8, '2026-01-08', NULL),
(9, 9, '2026-01-09', NULL),
(10, 10, '2026-01-10', NULL);

-- =========================
-- INSERTS: MOTIVO_COBRO
-- =========================
INSERT INTO motivo_cobro (descripcion) VALUES
('Mantenimiento'),
('Limpieza'),
('Seguridad'),
('Alquiler de puesto'),
('Servicio de agua'),
('Servicio de luz'),
('Cuota extraordinaria'),
('Penalidad por mora'),
('Licencia interna'),
('Fondo comun');

-- =========================
-- INSERTS: DEUDA
-- =========================
INSERT INTO deuda (id_socio, id_puesto, id_motivo, id_lote, monto, fecha, estado, fecha_pago, fecha_creacion) VALUES
(1, 1, 1, NULL, 50.00, '2026-04-01', 'PAGADA', '2026-04-15', '2026-04-01 07:00:00'),
(2, 2, 2, NULL, 60.00, '2026-04-02', 'PAGADA', '2026-04-15', '2026-04-02 07:00:00'),
(3, 3, 3, NULL, 55.00, '2026-04-03', 'PAGADA', '2026-04-15', '2026-04-03 07:00:00'),
(4, 4, 4, NULL, 70.00, '2026-04-04', 'PAGADA', '2026-04-15', '2026-04-04 07:00:00'),
(5, 5, 5, NULL, 45.00, '2026-04-05', 'PAGADA', '2026-04-15', '2026-04-05 07:00:00'),
(6, 6, 6, NULL, 80.00, '2026-04-06', 'PAGADA', '2026-04-15', '2026-04-06 07:00:00'),
(7, 7, 7, NULL, 65.00, '2026-04-07', 'PAGADA', '2026-04-15', '2026-04-07 07:00:00'),
(8, 8, 8, NULL, 75.00, '2026-04-08', 'PAGADA', '2026-04-15', '2026-04-08 07:00:00'),
(9, 9, 9, NULL, 90.00, '2026-04-09', 'PAGADA', '2026-04-15', '2026-04-09 07:00:00'),
(10, 10, 10, NULL, 100.00, '2026-04-10', 'PAGADA', '2026-04-15', '2026-04-10 07:00:00');

-- =========================
-- INSERTS: ROL
-- =========================
INSERT INTO rol (nombre) VALUES
('ADMIN'),
('CAJERO'),
('COBRADOR'),
('SUPERVISOR'),
('TESORERO'),
('SECRETARIO'),
('PRESIDENTE'),
('OPERADOR'),
('AUDITOR'),
('INVITADO');

-- =========================
-- INSERTS: USUARIO
-- =========================
INSERT INTO usuario (username, password, estado) VALUES
('admin1', '123456', TRUE),
('cajero1', '123456', TRUE),
('cobrador1', '123456', TRUE),
('supervisor1', '123456', TRUE),
('tesorero1', '123456', TRUE),
('secretario1', '123456', TRUE),
('presidente1', '123456', TRUE),
('operador1', '123456', TRUE),
('auditor1', '123456', TRUE),
('invitado1', '123456', TRUE);

-- =========================
-- INSERTS: USUARIO_ROL
-- =========================
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 10);

-- =========================
-- INSERTS: COMPROBANTE
-- =========================
INSERT INTO comprobante (numero, tipo, id_puesto, id_usuario, fecha, total, vuelto) VALUES
('COMP-0001', 'RECIBO', 1, 1, '2026-04-15 08:00:00', 50.00, 0.00),
('COMP-0002', 'RECIBO', 2, 2, '2026-04-15 08:15:00', 60.00, 0.00),
('COMP-0003', 'RECIBO', 3, 3, '2026-04-15 08:30:00', 55.00, 0.00),
('COMP-0004', 'RECIBO', 4, 4, '2026-04-15 08:45:00', 70.00, 0.00),
('COMP-0005', 'RECIBO', 5, 5, '2026-04-15 09:00:00', 45.00, 0.00),
('COMP-0006', 'RECIBO', 6, 6, '2026-04-15 09:15:00', 80.00, 0.00),
('COMP-0007', 'RECIBO', 7, 7, '2026-04-15 09:30:00', 65.00, 0.00),
('COMP-0008', 'RECIBO', 8, 8, '2026-04-15 09:45:00', 75.00, 0.00),
('COMP-0009', 'RECIBO', 9, 9, '2026-04-15 10:00:00', 90.00, 0.00),
('COMP-0010', 'RECIBO', 10, 10, '2026-04-15 10:15:00', 100.00, 0.00);

-- =========================
-- INSERTS: DETALLE_COMPROBANTE
-- =========================
INSERT INTO detalle_comprobante (id_comprobante, id_deuda, monto_pagado, fecha_registro) VALUES
(1, 1, 50.00, '2026-04-15 08:00:00'),
(2, 2, 60.00, '2026-04-15 08:15:00'),
(3, 3, 55.00, '2026-04-15 08:30:00'),
(4, 4, 70.00, '2026-04-15 08:45:00'),
(5, 5, 45.00, '2026-04-15 09:00:00'),
(6, 6, 80.00, '2026-04-15 09:15:00'),
(7, 7, 65.00, '2026-04-15 09:30:00'),
(8, 8, 75.00, '2026-04-15 09:45:00'),
(9, 9, 90.00, '2026-04-15 10:00:00'),
(10, 10, 100.00, '2026-04-15 10:15:00');

-- =========================
-- INSERTS: PAGO
-- =========================
INSERT INTO pago (metodo, monto_acumulado, fecha_registro) VALUES
('EFECTIVO', 50.00, '2026-04-15 08:00:00'),
('YAPE', 60.00, '2026-04-15 08:15:00'),
('PLIN', 55.00, '2026-04-15 08:30:00'),
('TRANSFERENCIA', 70.00, '2026-04-15 08:45:00'),
('EFECTIVO', 45.00, '2026-04-15 09:00:00'),
('YAPE', 80.00, '2026-04-15 09:15:00'),
('PLIN', 65.00, '2026-04-15 09:30:00'),
('TRANSFERENCIA', 75.00, '2026-04-15 09:45:00'),
('EFECTIVO', 90.00, '2026-04-15 10:00:00'),
('YAPE', 100.00, '2026-04-15 10:15:00');

-- =========================
-- INSERTS: DETALLE_PAGO
-- =========================
INSERT INTO detalle_pago (id_comprobante, id_pago) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 10);

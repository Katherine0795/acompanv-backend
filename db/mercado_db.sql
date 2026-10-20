-- =====================================================================
-- mercado_db · SCRIPT OFICIAL (MySQL 8.x)
-- Proyecto ACOMPAV · Desarrollo de Aplicaciones Web II
-- Alineado con las entidades JPA del backend:
--   * Asignación socio-puesto: columna puesto.id_socio (tabla socio_puesto eliminada)
--   * Pagos: pago + detalle_pago (tabla metodo_pago_comprobante eliminada)
--   * lote_deuda incluida (la entidad Lote existe; deuda.id_lote es opcional)
--   * Contraseñas cifradas con BCrypt. Contraseña de todos los usuarios de prueba: 123456
-- ADVERTENCIA: RECREA la base de datos y BORRA los datos existentes.
-- Respalde antes:  mysqldump -u root mercado_db > respaldo.sql
-- =====================================================================

DROP DATABASE IF EXISTS mercado_db;
CREATE DATABASE mercado_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mercado_db;

-- ---------- SOCIO ----------
CREATE TABLE socio (
    id_socio        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    dni             VARCHAR(15),
    telefono        VARCHAR(20)  NOT NULL,
    estado          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_socio_telefono UNIQUE (telefono)
);

-- ---------- PUESTO (id_socio = socio asignado; NULL = vacante) ----------
CREATE TABLE puesto (
    id_puesto                BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo                   VARCHAR(20)  NOT NULL,
    descripcion              VARCHAR(100),
    estado                   BOOLEAN      NOT NULL DEFAULT TRUE,
    es_propiedad_asociacion  BOOLEAN      NOT NULL DEFAULT FALSE,
    id_socio                 BIGINT       NULL,
    CONSTRAINT uq_puesto_codigo UNIQUE (codigo),
    CONSTRAINT fk_puesto_socio FOREIGN KEY (id_socio) REFERENCES socio(id_socio)
);

-- ---------- MOTIVO_COBRO ----------
CREATE TABLE motivo_cobro (
    id_motivo    INT AUTO_INCREMENT PRIMARY KEY,
    descripcion  VARCHAR(100) NOT NULL
);

-- ---------- ROL / USUARIO / USUARIO_ROL ----------
CREATE TABLE rol (
    id_rol  INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
);

CREATE TABLE usuario (
    id_usuario  INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(200) NOT NULL,   -- hash BCrypt (60 caracteres)
    estado      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_username UNIQUE (username)
);

CREATE TABLE usuario_rol (
    id_usuario  INT NOT NULL,
    id_rol      INT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_usuario_rol_rol     FOREIGN KEY (id_rol)     REFERENCES rol(id_rol)
);

-- ---------- LOTE_DEUDA (opcional) ----------
CREATE TABLE lote_deuda (
    id_lote      BIGINT AUTO_INCREMENT PRIMARY KEY,
    descripcion  VARCHAR(255),
    fecha        DATE NOT NULL
);

-- ---------- DEUDA ----------
CREATE TABLE deuda (
    id_deuda        BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_socio        BIGINT        NOT NULL,
    id_puesto       BIGINT        NOT NULL,
    id_motivo       INT           NOT NULL,
    id_lote         BIGINT        NULL,
    monto           DECIMAL(10,2) NOT NULL,
    fecha           DATE          NOT NULL,
    estado          ENUM('PENDIENTE','PAGADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago      DATE          NULL,
    fecha_creacion  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deuda_socio  FOREIGN KEY (id_socio)  REFERENCES socio(id_socio),
    CONSTRAINT fk_deuda_puesto FOREIGN KEY (id_puesto) REFERENCES puesto(id_puesto),
    CONSTRAINT fk_deuda_motivo FOREIGN KEY (id_motivo) REFERENCES motivo_cobro(id_motivo),
    CONSTRAINT fk_deuda_lote   FOREIGN KEY (id_lote)   REFERENCES lote_deuda(id_lote),
    CONSTRAINT ck_deuda_monto  CHECK (monto > 0)
);

-- ---------- COMPROBANTE ----------
CREATE TABLE comprobante (
    id_comprobante  BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero          VARCHAR(30)   NOT NULL,
    tipo            VARCHAR(20)   NOT NULL DEFAULT 'RECIBO',
    id_puesto       BIGINT        NOT NULL,
    id_usuario      INT           NULL,
    fecha           DATETIME      NOT NULL,
    total           DECIMAL(10,2) NOT NULL,
    vuelto          DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_comprobante_numero UNIQUE (numero),
    CONSTRAINT fk_comprobante_puesto  FOREIGN KEY (id_puesto)  REFERENCES puesto(id_puesto),
    CONSTRAINT fk_comprobante_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ---------- DETALLE_COMPROBANTE (una deuda solo puede pagarse una vez) ----------
CREATE TABLE detalle_comprobante (
    id_detalle      BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_comprobante  BIGINT        NOT NULL,
    id_deuda        BIGINT        NOT NULL,
    monto_pagado    DECIMAL(10,2) NOT NULL,
    fecha_registro  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_detalle_deuda UNIQUE (id_deuda),
    CONSTRAINT fk_detalle_comprobante FOREIGN KEY (id_comprobante) REFERENCES comprobante(id_comprobante),
    CONSTRAINT fk_detalle_deuda       FOREIGN KEY (id_deuda)       REFERENCES deuda(id_deuda)
);

-- ---------- PAGO / DETALLE_PAGO ----------
CREATE TABLE pago (
    id_pago          BIGINT AUTO_INCREMENT PRIMARY KEY,
    metodo           ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA') NOT NULL,
    monto_acumulado  DECIMAL(10,2) NOT NULL,
    fecha_registro   DATETIME      NOT NULL
);

CREATE TABLE detalle_pago (
    id_detalle_pago  BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_comprobante   BIGINT NOT NULL,
    id_pago          BIGINT NOT NULL,
    CONSTRAINT fk_detalle_pago_comprobante FOREIGN KEY (id_comprobante) REFERENCES comprobante(id_comprobante),
    CONSTRAINT fk_detalle_pago_pago        FOREIGN KEY (id_pago)        REFERENCES pago(id_pago)
);

-- ---------- ÍNDICES ----------
CREATE INDEX idx_deuda_socio_estado ON deuda (id_socio, estado);
CREATE INDEX idx_deuda_puesto       ON deuda (id_puesto);
CREATE INDEX idx_comprobante_fecha  ON comprobante (fecha);

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================
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

-- Las asignaciones vienen de la antigua tabla socio_puesto (socio N -> puesto N)
INSERT INTO puesto (codigo, descripcion, estado, es_propiedad_asociacion, id_socio) VALUES
('P001', 'Puesto de abarrotes', TRUE, FALSE, 1),
('P002', 'Puesto de frutas', TRUE, FALSE, 2),
('P003', 'Puesto de verduras', TRUE, FALSE, 3),
('P004', 'Puesto de carnes', TRUE, FALSE, 4),
('P005', 'Puesto de ropa', TRUE, FALSE, 5),
('P006', 'Puesto de calzado', TRUE, FALSE, 6),
('P007', 'Puesto de comida', TRUE, FALSE, 7),
('P008', 'Puesto de accesorios', TRUE, FALSE, 8),
('P009', 'Puesto de libreria', TRUE, TRUE, 9),
('P010', 'Puesto de bazar', TRUE, TRUE, 10);

INSERT INTO motivo_cobro (descripcion) VALUES
('Mantenimiento'), ('Limpieza'), ('Seguridad'), ('Alquiler de puesto'), ('Servicio de agua'),
('Servicio de luz'), ('Cuota extraordinaria'), ('Penalidad por mora'), ('Licencia interna'), ('Fondo comun');

-- Deudas 1-10: abril, pagadas. Deudas 11-15: mayo, PENDIENTES (para probar el cobro)
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
(10, 10, 10, NULL, 100.00, '2026-04-10', 'PAGADA', '2026-04-15', '2026-04-10 07:00:00'),
(1, 1, 1, NULL, 50.00, '2026-05-01', 'PENDIENTE', NULL, '2026-05-01 07:00:00'),
(1, 1, 2, NULL, 20.00, '2026-05-01', 'PENDIENTE', NULL, '2026-05-01 07:00:00'),
(2, 2, 1, NULL, 50.00, '2026-05-01', 'PENDIENTE', NULL, '2026-05-01 07:00:00'),
(2, 2, 3, NULL, 15.00, '2026-05-01', 'PENDIENTE', NULL, '2026-05-01 07:00:00'),
(3, 3, 1, NULL, 50.00, '2026-05-01', 'PENDIENTE', NULL, '2026-05-01 07:00:00');

INSERT INTO rol (nombre) VALUES
('ADMIN'), ('CAJERO'), ('COBRADOR'), ('SUPERVISOR'), ('TESORERO'),
('SECRETARIO'), ('PRESIDENTE'), ('OPERADOR'), ('AUDITOR'), ('INVITADO');

-- Contraseña de todos: 123456 (guardada como hash BCrypt)
INSERT INTO usuario (username, password, estado) VALUES
('admin1', '$2a$10$N/V7mICjG5Am0K7bZwL6HuTQuO8Du4X2F1mXydRSDQLcvitXm2wva', TRUE),
('cajero1', '$2a$10$Cs.9Vv2rHq0LPMCMeZh7ou00DsMImyoYgZGrGX2cADTu7wbSdqINO', TRUE),
('cobrador1', '$2a$10$jR05Oh7v2emP9CnK0j.AgeUO4HCAiPp95bdo1Br/dfpHaooqm8Jv.', TRUE),
('supervisor1', '$2a$10$Vw24rlcNXAzNYmBI/axFQ.1CUsJFRjADp8JZrPQu99mNxWtIPVAPC', TRUE),
('tesorero1', '$2a$10$eN/hvfzz6ZeB.E42rFqPi.DVZFtKynAgo1EQDp2yXsDHk3FRBbJ8q', TRUE),
('secretario1', '$2a$10$2bptZwcHKC8YlJLLCTuLc.GBwExCN9N91U4LJlYHWntrQn25BJ7cG', TRUE),
('presidente1', '$2a$10$sfX4Z0BpSJsw/h3fSjs7y.gioJpBy7enpcnQb.oDr5RW7zlQkRdYi', TRUE),
('operador1', '$2a$10$8Rs1vBPx0DliR1ZxL7irCez0yw6zHfl4lTfuixf2DBY3k1i/FSfwC', TRUE),
('auditor1', '$2a$10$87F4Bo2WeUJZ7MkHJ2mTjuyEW9LCXrCnOgb7M34sTq5okTFypekJW', TRUE),
('invitado1', '$2a$10$FkCmjCsctjuenV.Tawb5GuRk38Bm9lYnus2p03WQDWcJEmrugpGta', TRUE);

INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9), (10, 10);

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

INSERT INTO detalle_comprobante (id_comprobante, id_deuda, monto_pagado, fecha_registro) VALUES
(1, 1, 50.00, '2026-04-15 08:00:00'), (2, 2, 60.00, '2026-04-15 08:15:00'),
(3, 3, 55.00, '2026-04-15 08:30:00'), (4, 4, 70.00, '2026-04-15 08:45:00'),
(5, 5, 45.00, '2026-04-15 09:00:00'), (6, 6, 80.00, '2026-04-15 09:15:00'),
(7, 7, 65.00, '2026-04-15 09:30:00'), (8, 8, 75.00, '2026-04-15 09:45:00'),
(9, 9, 90.00, '2026-04-15 10:00:00'), (10, 10, 100.00, '2026-04-15 10:15:00');

INSERT INTO pago (metodo, monto_acumulado, fecha_registro) VALUES
('EFECTIVO', 50.00, '2026-04-15 08:00:00'), ('YAPE', 60.00, '2026-04-15 08:15:00'),
('PLIN', 55.00, '2026-04-15 08:30:00'), ('TRANSFERENCIA', 70.00, '2026-04-15 08:45:00'),
('EFECTIVO', 45.00, '2026-04-15 09:00:00'), ('YAPE', 80.00, '2026-04-15 09:15:00'),
('PLIN', 65.00, '2026-04-15 09:30:00'), ('TRANSFERENCIA', 75.00, '2026-04-15 09:45:00'),
('EFECTIVO', 90.00, '2026-04-15 10:00:00'), ('YAPE', 100.00, '2026-04-15 10:15:00');

INSERT INTO detalle_pago (id_comprobante, id_pago) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9), (10, 10);

-- Control: filas por tabla
SELECT 'socio' AS tabla, COUNT(*) AS total FROM socio
UNION ALL SELECT 'puesto', COUNT(*) FROM puesto
UNION ALL SELECT 'motivo_cobro', COUNT(*) FROM motivo_cobro
UNION ALL SELECT 'deuda', COUNT(*) FROM deuda
UNION ALL SELECT 'rol', COUNT(*) FROM rol
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario
UNION ALL SELECT 'usuario_rol', COUNT(*) FROM usuario_rol
UNION ALL SELECT 'comprobante', COUNT(*) FROM comprobante
UNION ALL SELECT 'detalle_comprobante', COUNT(*) FROM detalle_comprobante
UNION ALL SELECT 'pago', COUNT(*) FROM pago
UNION ALL SELECT 'detalle_pago', COUNT(*) FROM detalle_pago;

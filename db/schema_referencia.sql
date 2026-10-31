-- =====================================================================
-- Sistema de Gestión y Reserva de Citas para una Peluquería — APF2
-- Diseño físico de la base de datos · MySQL 8.0 · InnoDB · utf8mb4
-- =====================================================================
CREATE DATABASE IF NOT EXISTS peluqueria_citas
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE peluqueria_citas;

CREATE TABLE rol (
  id_rol TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE sede (
  id_sede INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  direccion VARCHAR(150) NOT NULL,
  telefono VARCHAR(20) NULL
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_rol TINYINT UNSIGNED NOT NULL,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  telefono VARCHAR(20) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,          -- BCrypt, nunca texto plano
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso DATETIME NULL,
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id_usuario BIGINT UNSIGNED PRIMARY KEY,
  acepta_whatsapp TINYINT(1) NOT NULL DEFAULT 1,
  acepta_correo TINYINT(1) NOT NULL DEFAULT 1,
  observaciones VARCHAR(200) NULL,
  CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE estilista (
  id_usuario BIGINT UNSIGNED PRIMARY KEY,
  id_sede INT UNSIGNED NOT NULL,
  especialidad VARCHAR(80) NOT NULL,
  CONSTRAINT fk_estilista_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_estilista_sede FOREIGN KEY (id_sede) REFERENCES sede(id_sede)
) ENGINE=InnoDB;

CREATE TABLE horario_estilista (
  id_horario INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_estilista BIGINT UNSIGNED NOT NULL,
  dia_semana TINYINT NOT NULL,                  -- 1 = lunes ... 7 = domingo
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  CONSTRAINT ck_horario_dia CHECK (dia_semana BETWEEN 1 AND 7),
  CONSTRAINT ck_horario_horas CHECK (hora_fin > hora_inicio),
  CONSTRAINT uq_horario UNIQUE (id_estilista, dia_semana, hora_inicio),
  CONSTRAINT fk_horario_estilista FOREIGN KEY (id_estilista) REFERENCES estilista(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE categoria_servicio (
  id_categoria SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE servicio (
  id_servicio INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_categoria SMALLINT UNSIGNED NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  descripcion VARCHAR(200) NULL,
  precio DECIMAL(8,2) NOT NULL,
  duracion_min SMALLINT UNSIGNED NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT ck_servicio_precio CHECK (precio >= 0),
  CONSTRAINT ck_servicio_duracion CHECK (duracion_min > 0),
  CONSTRAINT fk_servicio_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_servicio(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE estilista_servicio (
  id_estilista BIGINT UNSIGNED NOT NULL,
  id_servicio INT UNSIGNED NOT NULL,
  PRIMARY KEY (id_estilista, id_servicio),
  CONSTRAINT fk_es_estilista FOREIGN KEY (id_estilista) REFERENCES estilista(id_usuario),
  CONSTRAINT fk_es_servicio FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio)
) ENGINE=InnoDB;

CREATE TABLE cita (
  id_cita BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_cliente BIGINT UNSIGNED NOT NULL,
  id_estilista BIGINT UNSIGNED NOT NULL,
  id_sede INT UNSIGNED NOT NULL,
  fecha DATE NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  estado ENUM('PENDIENTE','CONFIRMADA','CANCELADA','ATENDIDA','AUSENTE') NOT NULL DEFAULT 'PENDIENTE',
  total DECIMAL(9,2) NOT NULL,
  motivo_cancelacion VARCHAR(200) NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- Columna generada: NULL cuando la cita está cancelada, de modo que el horario vuelve a quedar libre.
  -- MySQL permite varios NULL en un índice UNIQUE.
  hora_inicio_activa TIME GENERATED ALWAYS AS (CASE WHEN estado = 'CANCELADA' THEN NULL ELSE hora_inicio END) STORED,
  CONSTRAINT ck_cita_horas CHECK (hora_fin > hora_inicio),
  CONSTRAINT uq_cita_estilista_slot UNIQUE (id_estilista, fecha, hora_inicio_activa),
  CONSTRAINT fk_cita_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_usuario),
  CONSTRAINT fk_cita_estilista FOREIGN KEY (id_estilista) REFERENCES estilista(id_usuario),
  CONSTRAINT fk_cita_sede FOREIGN KEY (id_sede) REFERENCES sede(id_sede)
) ENGINE=InnoDB;
CREATE INDEX ix_cita_cliente_fecha ON cita (id_cliente, fecha);
CREATE INDEX ix_cita_fecha_estado ON cita (fecha, estado);

CREATE TABLE detalle_cita (
  id_cita BIGINT UNSIGNED NOT NULL,
  id_servicio INT UNSIGNED NOT NULL,
  precio_aplicado DECIMAL(8,2) NOT NULL,
  duracion_min SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (id_cita, id_servicio),
  CONSTRAINT fk_detalle_cita FOREIGN KEY (id_cita) REFERENCES cita(id_cita),
  CONSTRAINT fk_detalle_servicio FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio)
) ENGINE=InnoDB;

CREATE TABLE notificacion (
  id_notificacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_cita BIGINT UNSIGNED NOT NULL,
  tipo ENUM('REGISTRO','CONFIRMACION','RECORDATORIO','CANCELACION','REPROGRAMACION') NOT NULL,
  canal ENUM('WHATSAPP','CORREO') NOT NULL,
  mensaje VARCHAR(300) NOT NULL,
  fecha_programada DATETIME NOT NULL,
  fecha_envio DATETIME NULL,
  estado_envio ENUM('PENDIENTE','ENVIADA','FALLIDA') NOT NULL DEFAULT 'PENDIENTE',
  intentos TINYINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT fk_notif_cita FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
) ENGINE=InnoDB;
CREATE INDEX ix_notif_pendientes ON notificacion (estado_envio, fecha_programada);

CREATE TABLE auditoria (
  id_auditoria BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario BIGINT UNSIGNED NULL,
  accion VARCHAR(50) NOT NULL,
  entidad VARCHAR(40) NOT NULL,
  id_entidad BIGINT UNSIGNED NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ip VARCHAR(45) NULL,
  CONSTRAINT fk_audit_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Datos semilla (catálogos). Los usuarios se crean desde la aplicación,
-- que guarda la contraseña con BCrypt.
-- ---------------------------------------------------------------------
INSERT INTO rol (nombre) VALUES ('CLIENTE'),('ESTILISTA'),('ADMINISTRADOR');
INSERT INTO sede (nombre, direccion, telefono) VALUES ('Sede principal','Dirección por definir con la peluquería',NULL);
INSERT INTO categoria_servicio (nombre) VALUES ('Corte'),('Color'),('Peinado'),('Tratamiento');
INSERT INTO servicio (id_categoria,nombre,descripcion,precio,duracion_min) VALUES
 (1,'Corte de cabello','Corte con estilista a elección',35.00,40),
 (2,'Tinte y color','Incluye lavado',120.00,90),
 (3,'Peinado para evento','Reservar con 24 h de anticipación',80.00,60),
 (4,'Tratamiento capilar','Hidratación profunda',65.00,50);
-- Los precios y duraciones son de ejemplo y deben validarse con la peluquería.

-- ---------------------------------------------------------------------
-- Seguridad: mínimo privilegio (cambiar las contraseñas antes de usar)
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'app_citas'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE_APP';
GRANT SELECT, INSERT, UPDATE, DELETE ON peluqueria_citas.* TO 'app_citas'@'localhost';
CREATE USER IF NOT EXISTS 'app_reportes'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE_REPORTES';
GRANT SELECT ON peluqueria_citas.* TO 'app_reportes'@'localhost';

-- ---------------------------------------------------------------------
-- Consultas de reportes clave
-- ---------------------------------------------------------------------
-- R1. Citas por estado en un período
-- SELECT estado, COUNT(*) AS total FROM cita WHERE fecha BETWEEN '2026-06-01' AND '2026-06-30' GROUP BY estado;
-- R2. Servicios más solicitados
-- SELECT s.nombre, COUNT(*) AS veces FROM detalle_cita d JOIN servicio s ON s.id_servicio=d.id_servicio
--   JOIN cita c ON c.id_cita=d.id_cita WHERE c.estado IN ('CONFIRMADA','ATENDIDA') GROUP BY s.nombre ORDER BY veces DESC;
-- R3. Minutos atendidos por estilista
-- SELECT c.id_estilista, SUM(TIME_TO_SEC(TIMEDIFF(c.hora_fin,c.hora_inicio))/60) AS minutos
--   FROM cita c WHERE c.estado='ATENDIDA' GROUP BY c.id_estilista;

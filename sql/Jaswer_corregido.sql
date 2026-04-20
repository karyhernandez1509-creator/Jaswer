-- =============================================
-- JASWER - ESQUEMA CORREGIDO Y COMPATIBLE CON EL CODIGO JAVA
-- =============================================

CREATE DATABASE IF NOT EXISTS jaswer
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE jaswer;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS impuestos;
DROP TABLE IF EXISTS proveedores;
DROP TABLE IF EXISTS empleados;
DROP TABLE IF EXISTS usuarios;
SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------
-- TABLA: proveedores
-- Usada por FrmProveedor, FrmProductos y FrmListaProductos
-- -------------------------------------------------
CREATE TABLE proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    tipo_identificacion VARCHAR(30) NULL,
    identificacion VARCHAR(30) NULL,
    razon_social VARCHAR(150) NULL,
    nombre_comercial VARCHAR(150) NULL,
    correo VARCHAR(120) NULL,
    telefono VARCHAR(30) NULL,
    telefono1 VARCHAR(30) NULL,
    telefono2 VARCHAR(30) NULL,
    direccion VARCHAR(180) NULL,
    ciudad VARCHAR(80) NULL,
    provincia VARCHAR(80) NULL,
    pais VARCHAR(80) NULL,
    tipo_contribuyente VARCHAR(80) NULL,
    obligado_contabilidad VARCHAR(5) NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_proveedores_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -------------------------------------------------
-- TABLA: impuestos
-- Usada por FrmProductos
-- -------------------------------------------------
CREATE TABLE impuestos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -------------------------------------------------
-- TABLA: productos
-- Compatible con FrmProductos y FrmListaProductos
-- -------------------------------------------------
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(180) NOT NULL,
    proveedor_id INT NULL,
    impuesto_id INT NULL,
    costo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    precio DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    tipo VARCHAR(50) NULL,
    cocina TINYINT(1) NOT NULL DEFAULT 0,
    barra TINYINT(1) NOT NULL DEFAULT 0,
    otros TINYINT(1) NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_productos_codigo (codigo),
    KEY idx_productos_nombre (nombre),
    KEY idx_productos_tipo (tipo),
    CONSTRAINT fk_productos_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_productos_impuesto
        FOREIGN KEY (impuesto_id) REFERENCES impuestos(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -------------------------------------------------
-- TABLA: empleados
-- Usada por Login y modulo de administracion
-- -------------------------------------------------
CREATE TABLE empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    usuario VARCHAR(60) NOT NULL,
    contrasena VARCHAR(120) NOT NULL,
    es_admin TINYINT(1) NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_empleados_codigo (codigo),
    UNIQUE KEY uk_empleados_usuario (usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -------------------------------------------------
-- TABLA LEGACY: usuarios (compatibilidad con Login)
-- -------------------------------------------------
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(60) NOT NULL,
    contrasena VARCHAR(120) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_usuarios_usuario (usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -------------------------------------------------
-- DATOS INICIALES
-- -------------------------------------------------
INSERT INTO proveedores (nombre, razon_social, nombre_comercial, correo, telefono, activo)
VALUES ('Proveedor General', 'Proveedor General S.A.', 'Proveedor General', 'proveedor@jaswer.com', '0999999999', 1);

INSERT INTO impuestos (nombre, porcentaje, activo)
VALUES
    ('IVA 0%', 0.00, 1),
    ('IVA 12%', 12.00, 1),
    ('IVA 15%', 15.00, 1);

INSERT INTO empleados (codigo, nombre, usuario, contrasena, es_admin, activo)
VALUES ('EMP-ADMIN', 'Administrador General', 'admin_empleado', 'admin123', 1, 1);

INSERT INTO usuarios (usuario, contrasena, activo)
VALUES ('admin', 'admin123', 1);

INSERT INTO productos (
    codigo, nombre, proveedor_id, impuesto_id, costo, precio,
    stock, stock_minimo, tipo, cocina, barra, otros, activo
)
VALUES (
    'P-0001', 'Producto Demo', 1, 3, 10.00, 15.00,
    5, 2, 'Laptop', 0, 0, 1, 1
);

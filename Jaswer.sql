DROP DATABASE IF EXISTS jaswer;
CREATE DATABASE jaswer CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE jaswer;

CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200) NULL
) ENGINE=InnoDB;

CREATE TABLE marca (
    id_marca INT AUTO_INCREMENT PRIMARY KEY,
    nombre_marca VARCHAR(100) NOT NULL,
    pais_origen VARCHAR(100) NULL
) ENGINE=InnoDB;

CREATE TABLE modelo (
    id_modelo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_modelo VARCHAR(100) NOT NULL,
    id_marca INT NULL,
    CONSTRAINT fk_modelo_marca
        FOREIGN KEY (id_marca) REFERENCES marca(id_marca)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE estado_producto (
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200) NULL
) ENGINE=InnoDB;

CREATE TABLE proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    tipo_identificacion VARCHAR(30) NULL,
    identificacion VARCHAR(30) NULL,
    razon_social VARCHAR(150) NULL,
    nombre_comercial VARCHAR(150) NULL,
    contacto VARCHAR(50) NULL,
    telefono VARCHAR(20) NULL,
    telefono1 VARCHAR(20) NULL,
    telefono2 VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    direccion VARCHAR(200) NULL,
    ciudad VARCHAR(80) NULL,
    provincia VARCHAR(80) NULL,
    pais VARCHAR(80) NULL,
    tipo_contribuyente VARCHAR(80) NULL,
    obligado_contabilidad VARCHAR(5) NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_proveedores_nombre (nombre),
    UNIQUE KEY uk_proveedores_identificacion (identificacion)
) ENGINE=InnoDB;

CREATE TABLE impuestos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(60) NOT NULL,
    contrasena VARCHAR(120) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uk_usuarios_usuario (usuario)
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(300) NULL,
    proveedor_id INT NULL,
    impuesto_id INT NULL,
    costo DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    tipo VARCHAR(50) NULL,
    cocina TINYINT(1) NOT NULL DEFAULT 0,
    barra TINYINT(1) NOT NULL DEFAULT 0,
    otros TINYINT(1) NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    id_categoria INT NULL,
    id_marca INT NULL,
    id_modelo INT NULL,
    id_estado INT NULL,
    CONSTRAINT fk_productos_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_productos_impuesto FOREIGN KEY (impuesto_id) REFERENCES impuestos(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_productos_marca FOREIGN KEY (id_marca) REFERENCES marca(id_marca) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_productos_modelo FOREIGN KEY (id_modelo) REFERENCES modelo(id_modelo) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_productos_estado FOREIGN KEY (id_estado) REFERENCES estado_producto(id_estado) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE detalle_producto (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    nombre_caracteristica VARCHAR(100) NOT NULL,
    valor_caracteristica VARCHAR(150) NOT NULL,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos(id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE stock (
    id_stock INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 0,
    fecha_actualizacion DATE NULL,
    CONSTRAINT fk_stock_producto FOREIGN KEY (id_producto) REFERENCES productos(id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO proveedores (nombre, razon_social, identificacion, contacto, telefono, correo, direccion, activo)
VALUES ('Proveedor General', 'Proveedor General S.A.', '9999999999999', 'Proveedor', '0999999999', 'proveedor@jaswer.com', 'Direccion General', 1);

INSERT INTO impuestos (nombre, porcentaje, activo) VALUES
('IVA 0%', 0.00, 1),
('IVA 12%', 12.00, 1),
('IVA 15%', 15.00, 1);

INSERT INTO empleados (codigo, nombre, usuario, contrasena, es_admin, activo)
VALUES ('EMP-ADMIN', 'Administrador General', 'admin_empleado', 'admin123', 1, 1);

INSERT INTO usuarios (usuario, contrasena, activo)
VALUES ('admin', 'admin123', 1);

INSERT INTO productos (
    codigo, nombre, descripcion, proveedor_id, impuesto_id, costo, precio,
    stock, stock_minimo, tipo, cocina, barra, otros, activo
) VALUES (
    'P-0001', 'Producto Demo', 'Producto inicial',
    1, 3, 10.00, 15.00,
    5, 2, 'Laptop', 0, 0, 1, 1
);
CREATE DATABASE JASWER;
 USE JASWER;

 CREATE TABLE CATEGORIA(
 Id_categoria int primary key auto_increment,
 nombre_categoria varchar (100),
 descripcion varchar (200)
 );
 CREATE TABLE MARCA (
 Id_marca int primary key auto_increment,
 nombre_marca varchar (100),
 pais_origen varchar (100)
 );
 CREATE TABLE MODELO(
 Id_modelo int primary key auto_increment,
 nombre_modelo varchar (100),
 id_marca int,
 foreign key (Id_marca) references MARCA (Id_marca)
 ); 
 CREATE TABLE PROVEEDOR(
 Id_proveedor int primary key auto_increment,
 Razon_social varchar(150),
 Ruc varchar(20)unique,
 Contacto varchar(50),
 Telefono varchar(20),
 Email varchar(50),
 Direccion varchar(200)
 );
 CREATE TABLE ESTADO_PRODUCTO(
 Id_estado int primary key auto_increment,
 nombre_estado varchar (50),
 descripcion varchar (200)
 );
 CREATE TABLE USUARIO_EMPLEADO(
 Id_usuario int primary key auto_increment,
 nombres varchar (100),
 apellidos varchar (100),
 cargo varchar (100),
 telefono varchar (10),
 email varchar (100)
 );
 CREATE TABLE PRODUCTO(
 Id_producto int primary key auto_increment,
 nombre varchar (150),
 descripcion varchar (300),
 cantidad int,
 costo decimal (10,2),
 precio decimal (10,2),
 Id_categoria int, 
 Id_marca int,
 Id_modelo int,
 Id_proveedor int,
 Id_estado int,
 foreign key (Id_categoria) references CATEGORIA(Id_categoria),
 foreign key (Id_marca) references MARCA(Id_marca),
 foreign key (Id_modelo) references MODELO(Id_modelo),
 foreign key (Id_proveedor) references PROVEEDOR(Id_proveedor),
 foreign key (Id_estado) references ESTADO_PRODUCTO(Id_estado)
 );
CREATE TABLE detalle_producto(
id_detalle INT AUTO_INCREMENT PRIMARY KEY,
id_producto INT,
nombre_caracteristica VARCHAR(100),
valor_caracteristica VARCHAR(150),
FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);
 CREATE TABLE STOCK(
 Id_stock int primary key auto_increment,
 id_producto INT,
cantidad INT,
fecha_actualizacion DATE,
FOREIGN KEY (id_producto) REFERENCES PRODUCTO(id_producto)
 );
CREATE TABLE IF NOT EXISTS empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    usuario VARCHAR(60) NOT NULL,
    contrasena VARCHAR(120) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_empleados_codigo (codigo),
    UNIQUE KEY uk_empleados_usuario (usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO empleados (codigo, nombre, usuario, contrasena, activo)
SELECT 'EMP-ADMIN', 'Administrador General', 'admin_empleado', 'admin123', 1
WHERE NOT EXISTS (
    SELECT 1 FROM empleados WHERE usuario = 'admin_empleado'
);
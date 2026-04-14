USE jaswer;

ALTER TABLE proveedores 
ADD COLUMN tipo_identificacion VARCHAR(20),
ADD COLUMN identificacion VARCHAR(20),
ADD COLUMN razon_social VARCHAR(150),
ADD COLUMN nombre_comercial VARCHAR(150),
ADD COLUMN correo VARCHAR(100),
ADD COLUMN telefono1 VARCHAR(20),
ADD COLUMN telefono2 VARCHAR(20),
ADD COLUMN direccion VARCHAR(200),
ADD COLUMN ciudad VARCHAR(100),
ADD COLUMN provincia VARCHAR(100),
ADD COLUMN pais VARCHAR(100),
ADD COLUMN tipo_contribuyente VARCHAR(50),
ADD COLUMN obligado_contabilidad VARCHAR(5);

INSERT INTO proveedores 
(
nombre,
tipo_identificacion,
identificacion,
razon_social,
nombre_comercial,
correo,
telefono1,
telefono2,
direccion,
ciudad,
provincia,
pais,
tipo_contribuyente,
obligado_contabilidad,
activo
)
VALUES
(
'Proveedor Ejemplo',
'RUC',
'1234567890001',
'Empresa Ejemplo S.A',
'Comercial Ejemplo',
'correo@ejemplo.com',
'0999999999',
'022222222',
'Av Siempre Viva',
'Quito',
'Pichincha',
'Ecuador',
'Persona Natural',
'SI',
1
);

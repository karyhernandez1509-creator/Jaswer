USE jaswer;

ALTER TABLE proveedores
    ADD COLUMN IF NOT EXISTS tipo_identificacion VARCHAR(30) NULL AFTER nombre,
    ADD COLUMN IF NOT EXISTS identificacion VARCHAR(30) NULL AFTER tipo_identificacion,
    ADD COLUMN IF NOT EXISTS razon_social VARCHAR(150) NULL AFTER identificacion,
    ADD COLUMN IF NOT EXISTS nombre_comercial VARCHAR(150) NULL AFTER razon_social,
    ADD COLUMN IF NOT EXISTS correo VARCHAR(120) NULL AFTER nombre_comercial,
    ADD COLUMN IF NOT EXISTS telefono VARCHAR(30) NULL AFTER correo,
    ADD COLUMN IF NOT EXISTS telefono2 VARCHAR(30) NULL AFTER telefono,
    ADD COLUMN IF NOT EXISTS direccion VARCHAR(200) NULL AFTER telefono2,
    ADD COLUMN IF NOT EXISTS ciudad VARCHAR(100) NULL AFTER direccion,
    ADD COLUMN IF NOT EXISTS provincia VARCHAR(100) NULL AFTER ciudad,
    ADD COLUMN IF NOT EXISTS pais VARCHAR(100) NULL AFTER provincia,
    ADD COLUMN IF NOT EXISTS tipo_contribuyente VARCHAR(80) NULL AFTER pais,
    ADD COLUMN IF NOT EXISTS obligado_contabilidad TINYINT(1) NOT NULL DEFAULT 0 AFTER tipo_contribuyente,
    ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER activo,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER creado_en;

INSERT INTO proveedores (
    nombre,
    tipo_identificacion,
    identificacion,
    razon_social,
    nombre_comercial,
    correo,
    telefono,
    telefono2,
    direccion,
    ciudad,
    provincia,
    pais,
    tipo_contribuyente,
    obligado_contabilidad,
    activo
) VALUES (
    'Proveedor Demo',
    'RUC',
    '0999999999001',
    'Proveedor Demo S.A.',
    'Proveedor Demo',
    'demo@proveedor.com',
    '0999999999',
    '022222222',
    'Av. Principal 123',
    'Guayaquil',
    'Guayas',
    'Ecuador',
    'Sociedad',
    1,
    1
)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

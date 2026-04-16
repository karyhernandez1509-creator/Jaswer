-- Tabla para CRUD de empleados desde formulario de administrador
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

-- Usuario admin inicial para pruebas (opcional)
INSERT INTO empleados (codigo, nombre, usuario, contrasena, activo)
SELECT 'EMP-ADMIN', 'Administrador General', 'admin_empleado', 'admin123', 1
WHERE NOT EXISTS (
    SELECT 1 FROM empleados WHERE usuario = 'admin_empleado'
);

-- Script de actualizacion para permitir roles admin en empleados
ALTER TABLE empleados
ADD COLUMN IF NOT EXISTS es_admin TINYINT(1) NOT NULL DEFAULT 0 AFTER contrasena;

-- Convierte en administrador a un usuario ya existente (cambia el usuario segun necesites)
UPDATE empleados
SET es_admin = 1
WHERE usuario = 'admin_empleado';

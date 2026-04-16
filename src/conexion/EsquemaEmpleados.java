package conexion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class EsquemaEmpleados {

    private EsquemaEmpleados() {
    }

    public static void asegurarColumnaEsAdmin(Connection con) throws SQLException {
        if (con == null || !tablaExiste(con, "empleados")) {
            return;
        }

        if (!columnaExiste(con, "empleados", "es_admin")) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate(
                    "ALTER TABLE empleados ADD COLUMN es_admin TINYINT(1) NOT NULL DEFAULT 0 AFTER contrasena"
                );
            }
        }

        promoverAdministradoresPorDefecto(con);
    }

    private static void promoverAdministradoresPorDefecto(Connection con) throws SQLException {
        final String sql = "UPDATE empleados SET es_admin = 1 WHERE LOWER(usuario) IN ('admin', 'administrador', 'admin_empleado')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static boolean tablaExiste(Connection con, String tabla) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getTables(con.getCatalog(), null, tabla, null)) {
            return rs.next();
        }
    }

    private static boolean columnaExiste(Connection con, String tabla, String columna) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getColumns(con.getCatalog(), null, tabla, columna)) {
            return rs.next();
        }
    }
}

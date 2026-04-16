package conexion;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    
    public static Connection conectar() {
        
        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/jaswer",
                "root",
                "Kary123"
            );

            System.out.println("Conexion exitosa");

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return con;
    }
}


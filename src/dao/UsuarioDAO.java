/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;


import conexion.Conexion;
import java.sql.*;

public class UsuarioDAO {

    public String login(String usuario, String password) {

        String rol = null;

        try {
            Connection con = Conexion.conectar();

            String sql = "SELECT rol FROM usuarios WHERE usuario=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, usuario);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rol = rs.getString("rol");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return rol;
    }
}

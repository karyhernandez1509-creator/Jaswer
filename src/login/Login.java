/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package login;

import conexion.Conexion;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import modelo.SesionUsuario;
import vista.FrmMenuPrincipal;

/**
 *
 * @author MASTER
 */
public class Login extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Login.class.getName());

    /**
     * Creates new form Login
     */
    public Login() {
        initComponents();
        setLocationRelativeTo(null);
        configurarEventos();
        jPanel1.setOpaque(false);
    }

    private void configurarEventos() {
        
        jButton1.addActionListener(e -> iniciarSesion());
        jTextField2.addActionListener(e -> iniciarSesion());
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(
                    Login.this,
                    "La opcion de recuperar contrasena estara disponible proximamente.",
                    "Recuperar contrasena",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    private void iniciarSesion() {
        String usuario = jTextField1.getText().trim();
        String contrasena = new String(jTextField2.getPassword()).trim();

        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese su usuario.");
            jTextField1.requestFocus();
            return;
        }

        if (contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese su contrasena.");
            jTextField2.requestFocus();
            return;
        }

        ResultadoLogin resultado = autenticarUsuario(usuario, contrasena);
        if (resultado.autenticado) {
            SesionUsuario.iniciarSesion(usuario, resultado.esAdministrador);
            new FrmMenuPrincipal().setVisible(true);
            dispose();
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            "Usuario o contrasena incorrectos.",
            "Credenciales invalidas",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private ResultadoLogin autenticarUsuario(String usuario, String contrasena) {
        final String sqlEmpleados = "SELECT es_admin FROM empleados WHERE usuario = ? AND contrasena = ? AND activo = 1 LIMIT 1";
        final String sqlUsuarios = "SELECT 1 FROM usuarios WHERE usuario = ? AND contrasena = ? LIMIT 1";

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar a la base de datos.",
                    "Error de conexion",
                    JOptionPane.ERROR_MESSAGE
                );
                return ResultadoLogin.fallido();
            }

            if (tablaExiste(con, "empleados")) {
                try (PreparedStatement ps = con.prepareStatement(sqlEmpleados)) {
                    ps.setString(1, usuario);
                    ps.setString(2, contrasena);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return ResultadoLogin.exitoso(rs.getInt("es_admin") == 1);
                        }
                    }
                }
            }

            if (tablaExiste(con, "usuarios")) {
                try (PreparedStatement ps = con.prepareStatement(sqlUsuarios)) {
                    ps.setString(1, usuario);
                    ps.setString(2, contrasena);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            boolean esAdminLegacy = "admin".equalsIgnoreCase(usuario)
                                || "administrador".equalsIgnoreCase(usuario);
                            return ResultadoLogin.exitoso(esAdminLegacy);
                        }
                    }
                }
            }

            return ResultadoLogin.fallido();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al validar credenciales: " + e.getMessage(),
                "Error SQL",
                JOptionPane.ERROR_MESSAGE
            );
            return ResultadoLogin.fallido();
        }
    }

    private boolean tablaExiste(Connection con, String nombreTabla) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getTables(con.getCatalog(), null, nombreTabla, null)) {
            return rs.next();
        }
    }

    private static class ResultadoLogin {
        private final boolean autenticado;
        private final boolean esAdministrador;

        private ResultadoLogin(boolean autenticado, boolean esAdministrador) {
            this.autenticado = autenticado;
            this.esAdministrador = esAdministrador;
        }

        private static ResultadoLogin exitoso(boolean esAdministrador) {
            return new ResultadoLogin(true, esAdministrador);
        }

        private static ResultadoLogin fallido() {
            return new ResultadoLogin(false, false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 255, 102));
        setForeground(new java.awt.Color(255, 51, 0));

        panelPrincipal.setBackground(new java.awt.Color(240, 244, 248));
        panelPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(102, 102, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Inicio de Sesión");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 400, 40));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Usuario:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 100, -1, -1));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 125, 270, 35));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Contraseña:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 180, -1, -1));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField2.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 205, 270, 35));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setText("Ingresar");
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 290, 123, 41));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 255, 255));
        jLabel4.setText("Recuperar contraseña");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 250, -1, -1));

        panelPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 180, 400, 380));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/login/login.png"))); // NOI18N
        jLabel5.setText("jLabel5");
        jLabel5.setPreferredSize(new java.awt.Dimension(900, 780));
        panelPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 780));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    @Override
public void paint(Graphics g) {
    super.paint(g);

    Graphics2D g2 = (Graphics2D) g;

    // Suavizado
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Color glass
    g2.setColor(new java.awt.Color(120, 100, 255, 90));

    // Dibuja el efecto sobre el panel
    g2.fillRoundRect(
        jPanel1.getX(),
        jPanel1.getY(),
        jPanel1.getWidth(),
        jPanel1.getHeight(),
        30,
        30
    );

    // Borde
    g2.setColor(new java.awt.Color(255, 255, 255, 120));
    g2.drawRoundRect(
        jPanel1.getX(),
        jPanel1.getY(),
        jPanel1.getWidth(),
        jPanel1.getHeight(),
        30,
        30
    );
}
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPasswordField jTextField2;
    private javax.swing.JPanel panelPrincipal;
    // End of variables declaration//GEN-END:variables
}

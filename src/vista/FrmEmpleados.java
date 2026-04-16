package vista;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class FrmEmpleados extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmEmpleados.class.getName());
    private Integer idEmpleadoEdicion;

    public FrmEmpleados() {
        this(null);
    }

    public FrmEmpleados(Integer idEmpleadoEdicion) {
        this.idEmpleadoEdicion = idEmpleadoEdicion;
        initComponents();
        setLocationRelativeTo(null);
        configurarEventos();

        if (idEmpleadoEdicion != null) {
            cargarEmpleadoParaEdicion(idEmpleadoEdicion);
            setTitle("Editar Empleado");
            btnGuardar.setText("Actualizar");
        }
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardarEmpleado());
        btnCancelar.addActionListener(e -> regresarAListaEmpleados());
    }

    private void guardarEmpleado() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword()).trim();
        String confirmar = new String(txtConfirmarContrasena.getPassword()).trim();

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el codigo del empleado.");
            txtCodigo.requestFocus();
            return;
        }

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del empleado.");
            txtNombre.requestFocus();
            return;
        }

        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el usuario del empleado.");
            txtUsuario.requestFocus();
            return;
        }

        if (idEmpleadoEdicion == null && contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la contrasena del empleado.");
            txtContrasena.requestFocus();
            return;
        }

        if (!contrasena.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "La contrasena y su confirmacion no coinciden.");
            txtConfirmarContrasena.requestFocus();
            return;
        }

        if (!usuarioDisponible(usuario, idEmpleadoEdicion)) {
            JOptionPane.showMessageDialog(this, "El usuario ya existe, ingrese uno diferente.");
            txtUsuario.requestFocus();
            return;
        }

        if (idEmpleadoEdicion == null) {
            insertarEmpleado(codigo, nombre, usuario, contrasena);
        } else {
            actualizarEmpleado(codigo, nombre, usuario, contrasena);
        }
    }

    private boolean usuarioDisponible(String usuario, Integer idActual) {
        String sql = "SELECT id FROM empleados WHERE usuario = ?";
        if (idActual != null) {
            sql += " AND id <> ?";
        }

        try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return false;
            }

            ps.setString(1, usuario);
            if (idActual != null) {
                ps.setInt(2, idActual);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo validar el usuario: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private void insertarEmpleado(String codigo, String nombre, String usuario, String contrasena) {
        final String sql = "INSERT INTO empleados (codigo, nombre, usuario, contrasena, activo) VALUES (?, ?, ?, ?, 1)";

        try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, usuario);
            ps.setString(4, contrasena);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Empleado guardado correctamente.");
            regresarAListaEmpleados();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el empleado: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void actualizarEmpleado(String codigo, String nombre, String usuario, String contrasena) {
        final boolean actualizarContrasena = !contrasena.isEmpty();
        final String sql;

        if (actualizarContrasena) {
            sql = "UPDATE empleados SET codigo = ?, nombre = ?, usuario = ?, contrasena = ? WHERE id = ?";
        } else {
            sql = "UPDATE empleados SET codigo = ?, nombre = ?, usuario = ? WHERE id = ?";
        }

        try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            int indice = 1;
            ps.setString(indice++, codigo);
            ps.setString(indice++, nombre);
            ps.setString(indice++, usuario);
            if (actualizarContrasena) {
                ps.setString(indice++, contrasena);
            }
            ps.setInt(indice, idEmpleadoEdicion);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Empleado actualizado correctamente.");
            regresarAListaEmpleados();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo actualizar el empleado: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarEmpleadoParaEdicion(int idEmpleado) {
        final String sql = "SELECT codigo, nombre, usuario FROM empleados WHERE id = ?";

        try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "No se encontro el empleado seleccionado.");
                    regresarAListaEmpleados();
                    return;
                }

                txtCodigo.setText(rs.getString("codigo"));
                txtNombre.setText(rs.getString("nombre"));
                txtUsuario.setText(rs.getString("usuario"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar el empleado para editar: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void regresarAListaEmpleados() {
        java.awt.EventQueue.invokeLater(() -> new FrmListaEmpleados().setVisible(true));
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelFormulario = new javax.swing.JPanel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        lblContrasena = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        lblConfirmarContrasena = new javax.swing.JLabel();
        txtConfirmarContrasena = new javax.swing.JPasswordField();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Empleado");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelHeader.setBackground(new java.awt.Color(0, 0, 153));
        panelHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelHeader.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText("EL JASWER DEL SOFWER");
        panelHeader.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 400, 30));

        getContentPane().add(panelHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 60));

        panelFormulario.setBackground(new java.awt.Color(245, 245, 245));
        panelFormulario.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelFormulario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCodigo.setText("Codigo *");
        panelFormulario.add(lblCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 100, 20));
        panelFormulario.add(txtCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 200, 30));

        lblNombre.setText("Nombre *");
        panelFormulario.add(lblNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 100, 20));
        panelFormulario.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 840, 30));

        lblUsuario.setText("Usuario *");
        panelFormulario.add(lblUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 100, 20));
        panelFormulario.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 250, 30));

        lblContrasena.setText("Contrasena *");
        panelFormulario.add(lblContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 120, 20));
        panelFormulario.add(txtContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 250, 30));

        lblConfirmarContrasena.setText("Confirmar contrasena *");
        panelFormulario.add(lblConfirmarContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 160, 20));
        panelFormulario.add(txtConfirmarContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 250, 30));

        btnGuardar.setBackground(new java.awt.Color(0, 153, 0));
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        panelFormulario.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 80, -1));

        btnCancelar.setBackground(new java.awt.Color(204, 0, 0));
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cancelar");
        panelFormulario.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 30, 90, -1));

        getContentPane().add(panelFormulario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 900, 500));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
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

        java.awt.EventQueue.invokeLater(() -> new FrmEmpleados().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblConfirmarContrasena;
    private javax.swing.JLabel lblContrasena;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel panelFormulario;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JPasswordField txtConfirmarContrasena;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}

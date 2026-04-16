package vista;

import conexion.Conexion;
import conexion.EsquemaEmpleados;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.SesionUsuario;

public class FrmListaEmpleados extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmListaEmpleados.class.getName());
    private final String usuarioSesion;
    private final boolean esAdminSesion;

    public FrmListaEmpleados() {
        this(SesionUsuario.getUsuario(), SesionUsuario.esAdministrador());
    }

    public FrmListaEmpleados(String usuarioSesion, boolean esAdminSesion) {
        this.usuarioSesion = usuarioSesion == null ? "" : usuarioSesion;
        this.esAdminSesion = esAdminSesion;
        if (!this.esAdminSesion) {
            JOptionPane.showMessageDialog(
                this,
                "Solo un administrador puede acceder a esta pantalla.",
                "Acceso denegado",
                JOptionPane.WARNING_MESSAGE
            );
            new FrmMenuPrincipal().setVisible(true);
            dispose();
            return;
        }
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
        configurarEventos();
        cargarEmpleados();
    }

    private void configurarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID_INTERNO", "Codigo", "Nombre", "Usuario", "Rol", "Accion"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        tblEmpleados.setModel(modelo);
        tblEmpleados.getColumnModel().getColumn(0).setMinWidth(0);
        tblEmpleados.getColumnModel().getColumn(0).setMaxWidth(0);
        tblEmpleados.getColumnModel().getColumn(0).setPreferredWidth(0);
        tblEmpleados.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        tblEmpleados.getColumnModel().getColumn(5).setCellEditor(
            new ButtonEditor(new JCheckBox(), this::manejarAccionFila)
        );
    }

    private void configurarEventos() {
        btnMenuPrincipal.addActionListener(e -> regresarAMenuPrincipal());
        btnBuscar.addActionListener(e -> cargarEmpleados());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnCrearNuevo.addActionListener(e -> abrirFormularioEmpleado(null));
    }

    private void regresarAMenuPrincipal() {
        new FrmMenuPrincipal().setVisible(true);
        dispose();
    }

    private void abrirFormularioEmpleado(Integer idEmpleado) {
        FrmEmpleados frmEmpleados = new FrmEmpleados(idEmpleado, usuarioSesion, esAdminSesion);
        frmEmpleados.setVisible(true);
        dispose();
    }

    private void cargarEmpleados() {
        DefaultTableModel modelo = (DefaultTableModel) tblEmpleados.getModel();
        modelo.setRowCount(0);

        String filtroNombre = txtNombre.getText().trim();
        String filtroUsuario = txtUsuario.getText().trim();

        final StringBuilder sql = new StringBuilder(
            "SELECT id, codigo, nombre, usuario, es_admin FROM empleados WHERE activo = 1"
        );

        if (!filtroNombre.isEmpty()) {
            sql.append(" AND nombre LIKE ?");
        }
        if (!filtroUsuario.isEmpty()) {
            sql.append(" AND usuario LIKE ?");
        }

        sql.append(" ORDER BY nombre");

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            EsquemaEmpleados.asegurarColumnaEsAdmin(con);
            validarEstructuraEmpleados(con);

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int indice = 1;
                if (!filtroNombre.isEmpty()) {
                    ps.setString(indice++, "%" + filtroNombre + "%");
                }
                if (!filtroUsuario.isEmpty()) {
                    ps.setString(indice++, "%" + filtroUsuario + "%");
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        modelo.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("usuario"),
                            rs.getInt("es_admin") == 1 ? "Administrador" : "Empleado",
                            "Editar / Eliminar"
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los empleados: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void validarEstructuraEmpleados(Connection con) throws SQLException {
        Set<String> columnas = obtenerColumnasTabla(con, "empleados");
        if (!columnas.contains("id")
            || !columnas.contains("codigo")
            || !columnas.contains("nombre")
            || !columnas.contains("usuario")
            || !columnas.contains("contrasena")
            || !columnas.contains("es_admin")
            || !columnas.contains("activo")) {
            throw new SQLException(
                "La tabla empleados debe contener columnas: id, codigo, nombre, usuario, contrasena, es_admin, activo"
            );
        }
    }

    private Set<String> obtenerColumnasTabla(Connection con, String tabla) throws SQLException {
        Set<String> columnas = new HashSet<>();
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getColumns(con.getCatalog(), null, tabla, null)) {
            while (rs.next()) {
                columnas.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return columnas;
    }

    private void manejarAccionFila(int row) {
        if (row < 0 || row >= tblEmpleados.getRowCount()) {
            return;
        }

        Object idObj = tblEmpleados.getValueAt(row, 0);
        Object nombreObj = tblEmpleados.getValueAt(row, 2);

        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "No se encontro el ID del empleado.");
            return;
        }

        int idEmpleado;
        try {
            idEmpleado = Integer.parseInt(String.valueOf(idObj));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID invalido en la fila seleccionada.");
            return;
        }

        String nombreEmpleado = nombreObj == null ? "" : String.valueOf(nombreObj);

        Object[] opciones = {"Editar", "Eliminar", "Cancelar"};
        int accion = JOptionPane.showOptionDialog(
            this,
            "Seleccione una accion para el empleado: " + nombreEmpleado,
            "Accion de empleado",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (accion == 0) {
            abrirFormularioEmpleado(idEmpleado);
        } else if (accion == 1) {
            eliminarEmpleado(idEmpleado, nombreEmpleado);
        }
    }

    private void eliminarEmpleado(int idEmpleado, String nombreEmpleado) {
        int confirmar = JOptionPane.showConfirmDialog(
            this,
            "Desea eliminar al empleado \"" + nombreEmpleado + "\"?",
            "Confirmar eliminacion",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        final String sql = "UPDATE empleados SET activo = 0 WHERE id = ?";
        try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            ps.setInt(1, idEmpleado);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente.");
                cargarEmpleados();
            } else {
                JOptionPane.showMessageDialog(this, "No se elimino ningun empleado.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo eliminar el empleado: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFiltros() {
        txtNombre.setText("");
        txtUsuario.setText("");
        cargarEmpleados();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        btnMenuPrincipal = new javax.swing.JButton();
        panelFiltros = new javax.swing.JPanel();
        lblNombre = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtUsuario = new javax.swing.JTextField();
        btnCrearNuevo = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane();
        tblEmpleados = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Administracion de Empleados");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelHeader.setBackground(new java.awt.Color(0, 0, 153));
        panelHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelHeader.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText("EL JASWER DEL SOFWER");
        panelHeader.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 400, 30));

        btnMenuPrincipal.setBackground(new java.awt.Color(0, 153, 204));
        btnMenuPrincipal.setText("Menu Principal");
        panelHeader.add(btnMenuPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 20, -1, -1));

        getContentPane().add(panelHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 60));

        panelFiltros.setBackground(new java.awt.Color(245, 245, 245));
        panelFiltros.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelFiltros.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblNombre.setText("Nombre:");
        panelFiltros.add(lblNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 100, 20));

        lblUsuario.setText("Usuario:");
        panelFiltros.add(lblUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 20, 100, 20));
        panelFiltros.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 220, 30));
        panelFiltros.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 40, 220, 30));

        btnCrearNuevo.setBackground(new java.awt.Color(0, 153, 0));
        btnCrearNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCrearNuevo.setForeground(new java.awt.Color(255, 255, 255));
        btnCrearNuevo.setText("Crear Nuevo");
        panelFiltros.add(btnCrearNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 40, 120, 30));

        btnBuscar.setBackground(new java.awt.Color(0, 102, 204));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscar.setText("Buscar");
        panelFiltros.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 40, 100, 30));

        btnLimpiar.setBackground(new java.awt.Color(70, 70, 70));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setText("Limpiar");
        panelFiltros.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 40, 100, 30));

        getContentPane().add(panelFiltros, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 900, 100));

        panelTabla.setBackground(new java.awt.Color(255, 255, 255));
        panelTabla.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelTabla.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblEmpleados.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblEmpleados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Codigo", "Nombre", "Usuario", "Accion"
            }
        ));
        scrollTabla.setViewportView(tblEmpleados);

        panelTabla.add(scrollTabla, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 880, 380));

        getContentPane().add(panelTabla, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 900, 400));

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

        java.awt.EventQueue.invokeLater(() -> new FrmListaEmpleados().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCrearNuevo;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnMenuPrincipal;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tblEmpleados;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}

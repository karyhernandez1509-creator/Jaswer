/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author herna
 */
public class FrmProveedor extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmProveedor.class.getName());
    private final Runnable onProveedorGuardado;

    /**
     * Creates new form FrmProveedor
     */
    public FrmProveedor() {
        this(null);
    }

    public FrmProveedor(Runnable onProveedorGuardado) {
        this.onProveedorGuardado = onProveedorGuardado;
        initComponents();
        setLocationRelativeTo(null);
        configurarCombos();
        configurarEventos();
    }

    private void configurarCombos() {
        jComboBox1.setModel(new DefaultComboBoxModel<>(new String[]{
            "RUC",
            "Cedula",
            "Pasaporte",
            "Consumidor Final"
        }));

        jComboBox2.setModel(new DefaultComboBoxModel<>(new String[]{
            "Persona Natural",
            "Sociedad",
            "Contribuyente Especial",
            "Negocio Popular"
        }));

        jComboBox3.setModel(new DefaultComboBoxModel<>(new String[]{
            "SI",
            "NO"
        }));
    }

    private void configurarEventos() {
        jButton2.addActionListener(e -> dispose());
    }

    private void guardarProveedor() {
        String tipoIdentificacion = obtenerTextoCombo(jComboBox1);
        String identificacion = jTextField1.getText().trim();
        String razonSocial = jTextField2.getText().trim();
        String nombreComercial = jTextField3.getText().trim();
        String correo = jTextField5.getText().trim();
        String telefono1 = jTextField4.getText().trim();
        String telefono2 = jTextField6.getText().trim();
        String direccion = jTextField8.getText().trim();
        String ciudad = jTextField9.getText().trim();
        String provincia = jTextField10.getText().trim();
        String pais = jTextField12.getText().trim();
        String tipoContribuyente = obtenerTextoCombo(jComboBox2);
        String obligadoContabilidad = obtenerTextoCombo(jComboBox3);

        String nombreProveedor = !nombreComercial.isEmpty() ? nombreComercial : razonSocial;

        if (nombreProveedor.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Ingresa la razon social o el nombre comercial del proveedor.",
                "Validacion",
                JOptionPane.WARNING_MESSAGE
            );
            jTextField2.requestFocus();
            return;
        }

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar a la base de datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Set<String> columnas = obtenerColumnasTabla(con, "proveedores");
            if (!columnas.contains("nombre")) {
                JOptionPane.showMessageDialog(
                    this,
                    "La tabla proveedores no tiene la columna obligatoria 'nombre'.",
                    "Error de esquema",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (existeProveedor(con, nombreProveedor)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Ya existe un proveedor con ese nombre.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Map<String, Object> valores = new LinkedHashMap<>();
            valores.put("nombre", nombreProveedor);

            agregarSiExiste(columnas, valores, "tipo_identificacion", tipoIdentificacion);
            agregarSiExiste(columnas, valores, "identificacion", identificacion);
            agregarSiExiste(columnas, valores, "razon_social", razonSocial);
            agregarSiExiste(columnas, valores, "nombre_comercial", nombreComercial);
            agregarSiExiste(columnas, valores, "correo", correo);
            agregarTelefonoPrincipal(columnas, valores, telefono1);
            agregarSiExiste(columnas, valores, "telefono2", telefono2);
            agregarSiExiste(columnas, valores, "direccion", direccion);
            agregarSiExiste(columnas, valores, "ciudad", ciudad);
            agregarSiExiste(columnas, valores, "provincia", provincia);
            agregarSiExiste(columnas, valores, "pais", pais);
            agregarSiExiste(columnas, valores, "tipo_contribuyente", tipoContribuyente);
            if (columnas.contains("obligado_contabilidad")) {
                valores.put("obligado_contabilidad", obligadoContabilidad);
            }
            if (columnas.contains("activo")) {
                valores.put("activo", 1);
            }

            String sql = construirInsert("proveedores", valores);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int indice = 1;
                for (Object valor : valores.values()) {
                    ps.setObject(indice++, valor);
                }
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Proveedor guardado correctamente.");
            if (onProveedorGuardado != null) {
                onProveedorGuardado.run();
            }
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el proveedor: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean existeProveedor(Connection con, String nombreProveedor) throws SQLException {
        String sql = "SELECT id FROM proveedores WHERE nombre = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void agregarSiExiste(Set<String> columnas, Map<String, Object> valores, String columna, String valor) {
        if (columnas.contains(columna) && valor != null && !valor.trim().isEmpty()) {
            valores.put(columna, valor.trim());
        }
    }

    private void agregarTelefonoPrincipal(Set<String> columnas, Map<String, Object> valores, String telefono1) {
        if (telefono1 == null || telefono1.trim().isEmpty()) {
            return;
        }

        if (columnas.contains("telefono1")) {
            valores.put("telefono1", telefono1.trim());
            return;
        }

        if (columnas.contains("telefono")) {
            valores.put("telefono", telefono1.trim());
        }
    }

    private String obtenerTextoCombo(javax.swing.JComboBox<String> combo) {
        Object seleccionado = combo.getSelectedItem();
        return seleccionado == null ? "" : seleccionado.toString().trim();
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

    private String construirInsert(String tabla, Map<String, Object> valores) {
        List<String> columnas = new ArrayList<>(valores.keySet());
        String columnasTexto = String.join(", ", columnas);
        String placeholders = String.join(", ", java.util.Collections.nCopies(columnas.size(), "?"));
        return "INSERT INTO " + tabla + " (" + columnasTexto + ") VALUES (" + placeholders + ")";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelHeader.setBackground(new java.awt.Color(0, 0, 153));
        panelHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelHeader.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("CREAR PROVEEDOR");
        panelHeader.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 400, 30));

        getContentPane().add(panelHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 60));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setText("Identificación:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 150, 20));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 350, 25));

        jLabel3.setText("Tipo de Identificación");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 150, 20));
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 350, 25));

        jLabel4.setText("Razón Social:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 150, 20));
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 350, 25));

        jLabel5.setText("Nombre Comercial:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 150, 20));
        jPanel1.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 350, 25));

        jLabel6.setText(" Correo:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 150, 20));
        jPanel1.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 290, 170, 25));

        jLabel7.setText(" Telefono:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 270, 150, 20));
        jPanel1.add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 170, 25));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 204, 255));
        jLabel8.setText("Direccion");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, 150, 20));
        jPanel1.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 170, 25));

        jLabel9.setText("Telefono 2:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 330, 150, 20));
        jLabel9.getAccessibleContext().setAccessibleName("Telefono 3:");

        jPanel1.add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 350, 170, 25));

        jLabel10.setText("Telefono 2:");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 150, 20));

        jLabel11.setText("Direccion:");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 70, 150, 20));
        jPanel1.add(jTextField8, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 90, 350, 25));

        jLabel12.setText("Ciudad:");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 130, 150, 20));
        jPanel1.add(jTextField9, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 150, 350, 25));

        jLabel13.setText("Provincia:");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 190, 150, 20));
        jPanel1.add(jTextField10, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 210, 350, 25));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(51, 204, 255));
        jLabel14.setText("Datos del Contribuye:");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 400, 150, 20));
        jPanel1.add(jTextField12, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 270, 350, 25));

        jLabel16.setText("Pais:");
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 250, 150, 20));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, 350, 25));

        jLabel17.setText("Obligado a llevar contabilidad:");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 480, 170, 20));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(jComboBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 500, 170, 25));

        jLabel18.setText("Tipo Contribuyente:");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 430, 150, 20));

        jButton1.setBackground(new java.awt.Color(0, 153, 0));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Guardar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 550, 120, 40));

        jButton2.setBackground(new java.awt.Color(204, 0, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Cancelar");
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 550, 120, 40));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 880, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        guardarProveedor();
    }//GEN-LAST:event_jButton1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new FrmProveedor().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JPanel panelHeader;
    // End of variables declaration//GEN-END:variables
}

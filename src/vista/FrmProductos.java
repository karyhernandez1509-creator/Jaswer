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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author herna
 */
public class FrmProductos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmProductos.class.getName());

    /**
     * Creates new form FrmProductos
     */
    public FrmProductos() {
        initComponents();
        setLocationRelativeTo(null);
        configurarEventos();
        cargarCombosDesdeBD();
        inicializarSecciones();
    }

    private void configurarEventos() {
        jButton3.addActionListener(e -> guardarProducto());
        jButton2.addActionListener(e -> limpiarFormulario());
        btnNuevoProveedor.addActionListener(e -> abrirFrmProveedor());
    }

    private void cargarCombosDesdeBD() {
        cargarProveedores();
        cargarImpuestos();
    }

    private void cargarProveedores() {
        cbProveedor.removeAllItems();
        cbProveedor.addItem(new ComboItem(-1, "Seleccionar"));
        final String sql = "SELECT id, nombre FROM proveedores WHERE activo = 1 ORDER BY nombre";

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar a la base de datos para cargar proveedores.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    cbProveedor.addItem(new ComboItem(rs.getInt("id"), rs.getString("nombre")));
                }
            }
            cbProveedor.setSelectedIndex(0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los proveedores: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarImpuestos() {
        jComboBox1.removeAllItems();
        final String sql = "SELECT id, nombre, porcentaje FROM impuestos WHERE activo = 1 ORDER BY id";

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar a la base de datos para cargar impuestos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String etiqueta = rs.getString("nombre") + " (" + rs.getBigDecimal("porcentaje") + "%)";
                    jComboBox1.addItem(new ComboItem(rs.getInt("id"), etiqueta));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los impuestos: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void inicializarSecciones() {
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement("Cocina");
        modelo.addElement("Barra");
        modelo.addElement("Otros");
        jComboBox2.setModel(modelo);
    }

    private void guardarProducto() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String costoTexto = jTextField1.getText().trim();
        String stockMinimoTexto = txtStock.getText().trim();
        String precioTexto = txtPrecio.getText().trim();

        ComboItem proveedor = (ComboItem) cbProveedor.getSelectedItem();
        ComboItem impuesto = (ComboItem) jComboBox1.getSelectedItem();
        double costo = parseDoubleOrZero(costoTexto);
        int stockMinimo = parseIntOrZero(stockMinimoTexto);
        double precio = parseDoubleOrZero(precioTexto);

        if (proveedor == null || proveedor.id <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Selecciona un proveedor antes de guardar.",
                "Validacion",
                JOptionPane.WARNING_MESSAGE
            );
            cbProveedor.requestFocus();
            return;
        }

        String seccion = (String) jComboBox2.getSelectedItem();
        int cocina = "Cocina".equalsIgnoreCase(seccion) ? 1 : 0;
        int barra = "Barra".equalsIgnoreCase(seccion) ? 1 : 0;
        int otros = "Otros".equalsIgnoreCase(seccion) ? 1 : 0;

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            Set<String> columnas = obtenerColumnasTabla(con, "productos");
            String colCodigo = buscarColumna(columnas, "codigo", "codigo_producto", "cod_producto");
            String colNombre = buscarColumna(columnas, "nombre", "nombre_producto", "descripcion");
            String colProveedor = buscarColumna(columnas, "proveedor_id", "id_proveedor", "proveedor");
            String colImpuesto = buscarColumna(columnas, "impuesto_id", "id_impuesto", "impuesto");
            String colCosto = buscarColumna(columnas, "costo", "coste", "costo_compra");
            String colPrecio = buscarColumna(columnas, "precio", "precio_venta", "pvp", "valor_venta");
            String colStock = buscarColumna(columnas, "stock", "existencia");
            String colStockMinimo = buscarColumna(columnas, "stock_minimo", "stockminimo", "stock_min");

            if (colCodigo == null || colNombre == null || colCosto == null || colPrecio == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Faltan columnas base compatibles en la tabla productos (codigo/nombre/costo/precio).",
                    "Error de esquema",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (colStock == null && colStockMinimo == null) {
                JOptionPane.showMessageDialog(this, "La tabla productos no tiene columna de stock compatible.");
                return;
            }

            Map<String, Object> valores = new LinkedHashMap<>();
            valores.put(colCodigo, codigo);
            valores.put(colNombre, nombre);
            valores.put(colCosto, costo);
            valores.put(colPrecio, precio);

            if (colProveedor != null && proveedor != null && proveedor.id > 0) {
                valores.put(colProveedor, proveedor.id);
            }

            if (colImpuesto != null && impuesto != null) {
                valores.put(colImpuesto, impuesto.id);
            }

            if (colStock != null) {
                valores.put(colStock, stockMinimo);
            }

            if (colStockMinimo != null && !colStockMinimo.equals(colStock)) {
                valores.put(colStockMinimo, stockMinimo);
            }

            if (columnas.contains("cocina")) {
                valores.put("cocina", cocina);
            }
            if (columnas.contains("barra")) {
                valores.put("barra", barra);
            }
            if (columnas.contains("otros")) {
                valores.put("otros", otros);
            }
            if (columnas.contains("activo")) {
                valores.put("activo", 1);
            }

            String sql = construirInsert("productos", valores);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int index = 1;
                for (Object valor : valores.values()) {
                    ps.setObject(index++, valor);
                }
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Producto guardado correctamente.");
            limpiarFormulario();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el producto: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private double parseDoubleOrZero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private int parseIntOrZero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            return 0;
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

    private String buscarColumna(Set<String> columnas, String... candidatas) {
        for (String candidata : candidatas) {
            if (columnas.contains(candidata.toLowerCase())) {
                return candidata;
            }
        }
        return null;
    }

    private String construirInsert(String tabla, Map<String, Object> valores) {
        List<String> columnas = new ArrayList<>(valores.keySet());
        String columnasTexto = String.join(", ", columnas);
        String placeholders = String.join(", ", java.util.Collections.nCopies(columnas.size(), "?"));
        return "INSERT INTO " + tabla + " (" + columnasTexto + ") VALUES (" + placeholders + ")";
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        jTextField1.setText("0.0000");
        txtStock.setText("0");
        txtPrecio.setText("");
        if (cbProveedor.getItemCount() > 0) {
            cbProveedor.setSelectedIndex(0);
        }
        if (jComboBox1.getItemCount() > 0) {
            jComboBox1.setSelectedIndex(0);
        }
        if (jComboBox2.getItemCount() > 0) {
            jComboBox2.setSelectedIndex(0);
        }
        txtCodigo.requestFocus();
    }

    private void abrirFrmProveedor() {
        FrmProveedor frmProveedor = new FrmProveedor(() -> {
            cargarProveedores();
            cbProveedor.repaint();
        });
        frmProveedor.setVisible(true);
    }

    private static class ComboItem {
        private final int id;
        private final String nombre;

        ComboItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbProveedor = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        panelSeccion = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton2 = new javax.swing.JButton();
        btnNuevoProveedor = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Productos");
        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 0, 153));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setPreferredSize(new java.awt.Dimension(900, 60));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("EL JASWER DEL SOFWER");
        jLabel1.setPreferredSize(new java.awt.Dimension(20, 15));
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 480, 30));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 60));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setPreferredSize(new java.awt.Dimension(880, 470));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setText("Código *");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 100, 20));

        txtCodigo.setPreferredSize(new java.awt.Dimension(250, 30));
        txtCodigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoActionPerformed(evt);
            }
        });
        jPanel2.add(txtCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 200, 30));

        jLabel3.setText("Nombre *");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));
        jPanel2.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 830, 30));

        jLabel4.setText("Proveedor");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        cbProveedor.setBackground(new java.awt.Color(0, 0, 153));
        jPanel2.add(cbProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 300, 30));

        jLabel5.setText("Impuesto");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, -1));

        jComboBox1.setBackground(new java.awt.Color(0, 0, 153));
        jPanel2.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 300, 30));

        jLabel6.setText("Costo *");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, -1, -1));

        jTextField1.setText("0.0000");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 140, 30));

        txtStock.setText("0");
        jPanel2.add(txtStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 360, 140, 30));

        jLabel8.setText("Stock");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, -1, -1));

        jLabel9.setText("Precio");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 400, -1, -1));
        jPanel2.add(txtPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, 140, 30));

        panelSeccion.setBackground(new java.awt.Color(0, 102, 153));
        panelSeccion.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Seleccione una Categoria");
        panelSeccion.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 250, 20));

        jPanel2.add(panelSeccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 470, 280, 60));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 250, 30));

        jButton2.setBackground(new java.awt.Color(204, 0, 0));
        jButton2.setText("Cancelar");
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 30, -1, -1));

        btnNuevoProveedor.setBackground(new java.awt.Color(0, 153, 0));
        btnNuevoProveedor.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnNuevoProveedor.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevoProveedor.setText("+");
        jPanel2.add(btnNuevoProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 140, 45, 30));

        jButton3.setBackground(new java.awt.Color(0, 153, 0));
        jButton3.setText("Guardar");
        jPanel2.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 30, -1, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 900, 720));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void txtCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoActionPerformed

    private void regresarAListaProductos() {
        java.awt.EventQueue.invokeLater(() -> new FrmListaProductos().setVisible(true));
        dispose();
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
        java.awt.EventQueue.invokeLater(() -> new FrmProductos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNuevoProveedor;
    private javax.swing.JComboBox<ComboItem> cbProveedor;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<ComboItem> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel panelSeccion;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}

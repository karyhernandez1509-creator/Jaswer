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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import vista.ButtonRenderer;
/**
 *
 * @author herna
 */
public class FrmListaProductos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmListaProductos.class.getName());

    /**
     * Creates new form FrmListaProductos
     */
    public FrmListaProductos() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
        configurarEventos();
        inicializarFiltros();
        cargarProveedores();
        cargarProductos();
    }

    private void configurarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID_INTERNO", "Codigo", "Nombre", "Seccion", "Stock", "Accion"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        tblProductos.setModel(modelo);
        tblProductos.getColumnModel().getColumn(0).setMinWidth(0);
        tblProductos.getColumnModel().getColumn(0).setMaxWidth(0);
        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(0);
        tblProductos.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        tblProductos.getColumnModel().getColumn(5).setCellEditor(
            new ButtonEditor(new JCheckBox(), this::manejarAccionFila)
        );
    }

    private void configurarEventos() {
        jButton1.addActionListener(e -> regresarAMenuPrincipal());
        btnBuscar.addActionListener(e -> cargarProductos());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnCrearNuevo.addActionListener(e -> {
            FrmProductos frmProductos = new FrmProductos();
            frmProductos.setVisible(true);
            dispose();
        });
    }

    private void regresarAMenuPrincipal() {
        new FrmMenuPrincipal().setVisible(true);
        dispose();
    }

    private void inicializarFiltros() {
        cmbSeccion.removeAllItems();
        cmbSeccion.addItem("Todos");
        cmbSeccion.addItem("Hardware");
        cmbSeccion.addItem("Perifericos");
        cmbSeccion.addItem("Laptop");
        cmbSeccion.addItem("Accesorios");

        cmbProveedor.removeAllItems();
        cmbProveedor.addItem("Todos");
    }

    private void cargarProveedores() {
        final String sql = "SELECT nombre FROM proveedores WHERE activo = 1 ORDER BY nombre";
        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cmbProveedor.addItem(rs.getString("nombre"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los proveedores: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarProductos() {
        DefaultTableModel modelo = (DefaultTableModel) tblProductos.getModel();
        modelo.setRowCount(0);

        String nombreFiltro = txtNombre.getText().trim();
        String codigoFiltro = jTextField1.getText().trim();
        String seccionFiltro = (String) cmbSeccion.getSelectedItem();
        String proveedorFiltro = (String) cmbProveedor.getSelectedItem();

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar a la base de datos.",
                    "Error de conexion",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Set<String> columnas = obtenerColumnasTabla(con, "productos");
            String colId = buscarColumna(columnas, "id", "id_producto");
            String colNombre = buscarColumna(columnas, "nombre", "nombre_producto", "descripcion");
            String colCodigo = buscarColumna(columnas, "codigo", "codigo_producto", "cod_producto");
            String colStock = buscarColumna(columnas, "stock", "existencia", "stock_minimo", "stockminimo");
            String colProveedor = buscarColumna(columnas, "proveedor_id", "id_proveedor", "proveedor");
            String colActivo = buscarColumna(columnas, "activo", "estado");
            String colTipo = buscarColumna(columnas, "tipo", "categoria", "seccion");
            boolean tieneCocina = columnas.contains("cocina");
            boolean tieneBarra = columnas.contains("barra");
            boolean tieneOtros = columnas.contains("otros");

            if (colId == null || colCodigo == null || colNombre == null || colStock == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "La tabla productos no tiene columnas compatibles para listado (id, codigo, nombre, stock).",
                    "Error de esquema",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT p.")
                .append(colId).append(" AS id, p.")
                .append(colCodigo).append(" AS codigo, p.")
                .append(colNombre).append(" AS nombre, p.")
                .append(colStock).append(" AS stock, ");

            if (colTipo != null) {
                sql.append("COALESCE(p.").append(colTipo).append(", 'Sin seccion') AS seccion ");
            } else if (tieneCocina || tieneBarra || tieneOtros) {
                sql.append("CASE ");
                if (tieneCocina) {
                    sql.append("WHEN p.cocina = 1 THEN 'Hardware' ");
                }
                if (tieneBarra) {
                    sql.append("WHEN p.barra = 1 THEN 'Perifericos' ");
                }
                if (tieneOtros) {
                    sql.append("WHEN p.otros = 1 THEN 'Laptop' ");
                }
                sql.append("ELSE 'Sin seccion' END AS seccion ");
            } else {
                sql.append("'Sin seccion' AS seccion ");
            }

            sql.append("FROM productos p ");
            if (colProveedor != null) {
                sql.append("LEFT JOIN proveedores pr ON p.").append(colProveedor).append(" = pr.id ");
            }

            List<Object> parametros = new ArrayList<>();
            List<String> condiciones = new ArrayList<>();

            if (!nombreFiltro.isEmpty()) {
                condiciones.add("p." + colNombre + " LIKE ?");
                parametros.add("%" + nombreFiltro + "%");
            }

            if (!codigoFiltro.isEmpty() && colCodigo != null) {
                condiciones.add("p." + colCodigo + " = ?");
                parametros.add(codigoFiltro);
            }

            if (seccionFiltro != null && !"Todos".equalsIgnoreCase(seccionFiltro)) {
                if (colTipo != null) {
                    condiciones.add("p." + colTipo + " = ?");
                    parametros.add(seccionFiltro);
                } else if ("Hardware".equalsIgnoreCase(seccionFiltro) && tieneCocina) {
                    condiciones.add("p.cocina = 1");
                } else if ("Perifericos".equalsIgnoreCase(seccionFiltro) && tieneBarra) {
                    condiciones.add("p.barra = 1");
                } else if (("Laptop".equalsIgnoreCase(seccionFiltro) || "Accesorios".equalsIgnoreCase(seccionFiltro)) && tieneOtros) {
                    condiciones.add("p.otros = 1");
                }
            }

            if (proveedorFiltro != null && !"Todos".equalsIgnoreCase(proveedorFiltro) && colProveedor != null) {
                condiciones.add("pr.nombre = ?");
                parametros.add(proveedorFiltro);
            }

            if (colActivo != null) {
                condiciones.add("p." + colActivo + " = 1");
            }

            if (!condiciones.isEmpty()) {
                sql.append("WHERE ").append(String.join(" AND ", condiciones)).append(" ");
            }

            sql.append("ORDER BY p.").append(colNombre);

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int index = 1;
                for (Object parametro : parametros) {
                    ps.setObject(index++, parametro);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        modelo.addRow(new Object[]{
                            rs.getObject("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("seccion"),
                            rs.getObject("stock"),
                            "Editar / Eliminar"
                        });
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los productos: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
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

    private String buscarColumna(Set<String> columnas, String... candidatas) {
        for (String candidata : candidatas) {
            if (columnas.contains(candidata.toLowerCase())) {
                return candidata;
            }
        }
        return null;
    }

    private void manejarAccionFila(int row) {
        if (row < 0 || row >= tblProductos.getRowCount()) {
            return;
        }

        Object idObj = tblProductos.getValueAt(row, 0);
        Object nombreObj = tblProductos.getValueAt(row, 2);
        Object stockObj = tblProductos.getValueAt(row, 4);

        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "No se encontro el ID del producto.");
            return;
        }

        int idProducto;
        int stockActual = 0;
        try {
            idProducto = Integer.parseInt(String.valueOf(idObj));
            if (stockObj != null) {
                stockActual = Integer.parseInt(String.valueOf(stockObj));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID o stock invalido en la fila seleccionada.");
            return;
        }

        String nombreActual = nombreObj == null ? "" : String.valueOf(nombreObj);

        Object[] opciones = {"Editar", "Eliminar", "Cancelar"};
        int accion = JOptionPane.showOptionDialog(
            this,
            "Seleccione una accion para el producto: " + nombreActual,
            "Accion de producto",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (accion == 0) {
            editarProducto(idProducto, nombreActual, stockActual);
        } else if (accion == 1) {
            eliminarProducto(idProducto, nombreActual);
        }
    }

    private void editarProducto(int idProducto, String nombreActual, int stockActual) {
        FrmProductos frmProductos = new FrmProductos(idProducto);
        frmProductos.setVisible(true);
        dispose();
    }

    private void eliminarProducto(int idProducto, String nombreProducto) {
        int confirmar = JOptionPane.showConfirmDialog(
            this,
            "Desea eliminar el producto \"" + nombreProducto + "\"?",
            "Confirmar eliminacion",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = Conexion.conectar()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
                return;
            }

            Set<String> columnas = obtenerColumnasTabla(con, "productos");
            String colId = buscarColumna(columnas, "id", "id_producto");
            String colActivo = buscarColumna(columnas, "activo", "estado");

            if (colId == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "La tabla productos no tiene columna ID compatible.",
                    "Error de esquema",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String sql;
            if (colActivo != null) {
                sql = "UPDATE productos SET " + colActivo + " = 0 WHERE " + colId + " = ?";
            } else {
                sql = "DELETE FROM productos WHERE " + colId + " = ?";
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProducto);
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
                    cargarProductos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se elimino ningun producto.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo eliminar el producto: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFiltros() {
        txtNombre.setText("");
        jTextField1.setText("");
        if (cmbSeccion.getItemCount() > 0) {
            cmbSeccion.setSelectedIndex(0);
        }
        if (cmbProveedor.getItemCount() > 0) {
            cmbProveedor.setSelectedIndex(0);
        }
        cargarProductos();
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
        jButton1 = new javax.swing.JButton();
        panelFiltros = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cmbSeccion = new javax.swing.JComboBox<>();
        cmbProveedor = new javax.swing.JComboBox<>();
        txtNombre = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        btnCrearNuevo = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        panelTabla = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelHeader.setBackground(new java.awt.Color(0, 0, 153));
        panelHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelHeader.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("EL JASWER DEL SOFWER");
        panelHeader.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 400, 30));

        jButton1.setBackground(new java.awt.Color(0, 153, 204));
        jButton1.setText("Menu Principal");
        panelHeader.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 20, -1, -1));

        getContentPane().add(panelHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 60));

        panelFiltros.setBackground(new java.awt.Color(245, 245, 245));
        panelFiltros.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelFiltros.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setText("Sección");
        panelFiltros.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 15, 80, 20));

        jLabel3.setText("Proveedor: ");
        panelFiltros.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 15, 100, 20));

        jLabel4.setText("Código:");
        panelFiltros.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 15, 80, 20));

        jLabel5.setText("Nombre del Producto:");
        panelFiltros.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 15, 130, 20));

        cmbSeccion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelFiltros.add(cmbSeccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 35, 180, 30));

        cmbProveedor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelFiltros.add(cmbProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 35, 180, 30));
        panelFiltros.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 35, 180, 30));
        panelFiltros.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 35, 180, 30));

        btnCrearNuevo.setBackground(new java.awt.Color(0, 153, 0));
        btnCrearNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCrearNuevo.setForeground(new java.awt.Color(255, 255, 255));
        btnCrearNuevo.setText("Crear Nuevo");
        panelFiltros.add(btnCrearNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 75, 120, 30));

        btnBuscar.setBackground(new java.awt.Color(0, 102, 204));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscar.setText("Buscar");
        panelFiltros.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 75, 100, 30));

        btnLimpiar.setBackground(new java.awt.Color(70, 70, 70));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setText("Limpiar");
        panelFiltros.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 75, 100, 30));

        getContentPane().add(panelFiltros, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 900, 110));

        panelTabla.setBackground(new java.awt.Color(255, 255, 255));
        panelTabla.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelTabla.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblProductos.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Seccion", "Stcok", "Acción"
            }
        ));
        scrollTabla.setViewportView(tblProductos);

        panelTabla.add(scrollTabla, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 880, 370));

        getContentPane().add(panelTabla, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 900, 390));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        java.awt.EventQueue.invokeLater(() -> new FrmListaProductos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCrearNuevo;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<String> cmbProveedor;
    private javax.swing.JComboBox<String> cmbSeccion;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}

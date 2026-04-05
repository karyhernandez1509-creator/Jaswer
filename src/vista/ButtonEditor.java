/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author herna
 */
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class ButtonEditor extends DefaultCellEditor {

    @FunctionalInterface
    public interface RowAction {
        void onClick(int row);
    }

    protected JButton button;
    private String label;
    private boolean clicked;
    private int selectedRow = -1;
    private final RowAction rowAction;

    public ButtonEditor(JCheckBox checkBox) {
        this(checkBox, null);
    }

    public ButtonEditor(JCheckBox checkBox, RowAction rowAction) {
        super(checkBox);
        this.rowAction = rowAction;
        button = new JButton("Editar / Eliminar");

        button.addActionListener((ActionEvent e) -> {
            if (rowAction != null && selectedRow >= 0) {
                rowAction.onClick(selectedRow);
            }
            fireEditingStopped();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

        label = "Editar / Eliminar";
        button.setText(label);
        clicked = true;
        selectedRow = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        clicked = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        clicked = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}


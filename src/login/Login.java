package login;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Login extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JLabel lblRecuperar;

    public Login() {
        initUI();
    }

    private void initUI() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setMinimumSize(getSize());
        setLocationRelativeTo(null);

        GradientPanel fondo = new GradientPanel();
        fondo.setLayout(new GridBagLayout());

        GlassCard card = new GlassCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel titulo = new JLabel("Iniciar Sesion");
        titulo.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 28));
        titulo.setForeground(new Color(255, 255, 255));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(titulo, gbc);

        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUsuario.setForeground(new Color(245, 245, 245));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblUsuario, gbc);

        txtUsuario = createInputField();
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(txtUsuario, gbc);

        JLabel lblContrasena = new JLabel("Contrasena");
        lblContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblContrasena.setForeground(new Color(245, 245, 245));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblContrasena, gbc);

        txtContrasena = new JPasswordField();
        styleInput(txtContrasena);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(txtContrasena, gbc);

        btnIngresar = new JButton("Ingresar");
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        btnIngresar.setBackground(new Color(191, 36, 36));
        btnIngresar.setOpaque(true);
        btnIngresar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(btnIngresar, gbc);

        lblRecuperar = new JLabel("Recuperar contrasena");
        lblRecuperar.setForeground(new Color(250, 220, 220));
        lblRecuperar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRecuperar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblRecuperar.setToolTipText("Proximamente");
        gbc.gridy = 6;
        gbc.insets = new Insets(2, 0, 0, 0);
        card.add(lblRecuperar, gbc);

        fondo.add(card);
        setContentPane(fondo);
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    private void styleInput(JTextField field) {
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBackground(new Color(255, 255, 255, 28));
        field.setOpaque(true);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(122, 0, 0),
                getWidth(), getHeight(), new Color(225, 45, 45)
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class GlassCard extends JPanel {
        GlassCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 26, 26);
            g2.dispose();
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension(410, 360);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {
        }

        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}

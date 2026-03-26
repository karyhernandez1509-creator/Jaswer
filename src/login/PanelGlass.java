/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;

/**
 *
 * @author herna
 */


import java.awt.*;
import javax.swing.*;

public class PanelGlass extends JPanel {

    public PanelGlass() {
        setOpaque(false); // hace el panel transparente
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Suavizado (antialias)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Color glass (ajusta aquí)
        g2.setColor(new Color(120, 100, 255, 90)); // morado transparente

        // Fondo redondeado
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        // Borde suave
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);

        g2.dispose();

        super.paintComponent(g);
    }
}


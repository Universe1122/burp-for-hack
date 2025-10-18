package org.example.proxy.ui.tab;

import javax.swing.*;
import java.awt.*;

public class ClosableTabComponent extends JPanel {
    public ClosableTabComponent(JTabbedPane tabbedPane, String title) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        JLabel label = new JLabel(title);
        add(label);

        JButton closeButton = new JButton("x");
        closeButton.setMargin(new Insets(0, 5, 0, 5));
        closeButton.setFocusable(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);

        Color originalColor = closeButton.getForeground();

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(originalColor);
            }
        });

        closeButton.addActionListener(e -> {
            int index = tabbedPane.indexOfTabComponent(this);
            if (index != -1) {
                tabbedPane.remove(index);
            }
        });
        add(closeButton);
    }
}
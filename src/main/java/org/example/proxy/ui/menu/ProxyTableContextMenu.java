package org.example.proxy.ui.menu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import org.example.proxy.ui.tab.ProxyTableModel;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ProxyTableContextMenu {
    private final MontoyaApi api;

    public ProxyTableContextMenu(MontoyaApi api) {
        this.api = api;
    }

    public void attach(JTable table, ProxyTableModel model) {
        JPopupMenu popupMenu = new JPopupMenu();
        ProxyTableContextMenuHandler handler = new ProxyTableContextMenuHandler(api);

        // 메뉴 추가
        JMenuItem copyUrl =  new JMenuItem("Copy URL");
        JMenuItem sendToRepeater = new JMenuItem("Send to Repeater");
        JMenu highlight = new JMenu("Highlight");

        popupMenu.add(copyUrl);
        popupMenu.add(sendToRepeater);
        popupMenu.add(highlight);

        HighlightColor[] colors = {
                HighlightColor.BLUE,
                HighlightColor.CYAN,
                HighlightColor.GRAY,
                HighlightColor.GREEN,
                HighlightColor.MAGENTA,
                HighlightColor.NONE,
                HighlightColor.ORANGE,
                HighlightColor.PINK,
                HighlightColor.RED,
                HighlightColor.YELLOW,
        };

        for (HighlightColor color : colors) {
            JMenuItem colorItem = new JMenuItem(color.toString());
            colorItem.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    handler.setHighlight(model, row, color);
                }
            });
            highlight.add(colorItem);
        }

        copyUrl.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                handler.copyToClipboard(model.get(row).getHttpRequest().url());
            }
        });

        sendToRepeater.addActionListener(e -> {
           int row = table.getSelectedRow();
           if (row >= 0) {
               handler.sendToRepeater(model.get(row).getHttpRequest());
           }
        });

        // 우클릭 리스너
        table.addMouseListener(new MouseAdapter() {
            private void showIfPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        // 마우스 우클릭한 행을 선택 상태로 만듦
                        table.getSelectionModel().setSelectionInterval(row, row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showIfPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showIfPopup(e);
            }
        });

        // Send to Repeater 단축키 리스너
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

                boolean isShortcutPressed;
                if (isMac) {
                    isShortcutPressed = e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_R; // ⌘ + R
                } else {
                    isShortcutPressed = e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R; // Ctrl + R
                }

                if (isShortcutPressed) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        handler.sendToRepeater(model.get(row).getHttpRequest());
                    }
                }
            }
        });
    }
}


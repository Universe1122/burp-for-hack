package org.example.proxy.ui.menu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import org.example.proxy.ProxyPacketEntry;

import javax.swing.*;
import java.awt.*;

public class ProxyEntryContextMenu extends JPopupMenu {

    private final MontoyaApi api;
    private final ProxyPacketEntry message;

    public ProxyEntryContextMenu(MontoyaApi api, ProxyPacketEntry message) {
        this.api = api;
        this.message = message;
        // 메뉴 아이템 추가
        add(createSendToRepeaterItem());
        add(createCopyUrlItem());
        addSeparator();
        add(createHighlightMenu());
    }

    private JMenuItem createSendToRepeaterItem() {
        JMenuItem item = new JMenuItem("Send to Repeater");
        item.addActionListener(e -> {
            String host = message.getHttpRequest().httpService().host();
            int port = message.getHttpRequest().httpService().port();
            boolean useHttps = message.getHttpRequest().httpService().secure();
            api.repeater().sendToRepeater(message.getHttpRequest());
            api.logging().logToOutput("Sent to Repeater: " + message.getHttpRequest().url());
        });
        return item;
    }

    private JMenuItem createCopyUrlItem() {
        JMenuItem item = new JMenuItem("Copy URL");
        item.addActionListener(e -> {
            String url = message.getHttpRequest().url();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(url), null);
            api.logging().logToOutput("Copied URL: " + url);
        });
        return item;
    }

    private JMenu createHighlightMenu() {
        JMenu highlightMenu = new JMenu("Highlight");

        for (HighlightColor color : HighlightColor.values()) {
            JMenuItem colorItem = new JMenuItem(color.name());
            colorItem.addActionListener(e -> {
                message.getInterceptedResponse().annotations().setHighlightColor(color);
            });
            highlightMenu.add(colorItem);
        }

        return highlightMenu;
    }

    // 메뉴 표시 함수
    public static void showMenu(MontoyaApi api, ProxyPacketEntry message, java.awt.Component component, int x, int y) {
        SwingUtilities.invokeLater(() -> {
            ProxyEntryContextMenu menu = new ProxyEntryContextMenu(api, message);
            menu.show(component, x, y);
        });
    }
}
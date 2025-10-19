package org.example.proxy.ui.menu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProxyEntryContextMenu extends JPopupMenu {

    private final MontoyaApi api;
    private final HttpRequestResponse message;

    public ProxyEntryContextMenu(MontoyaApi api, HttpRequestResponse message) {
        this.api = api;
        this.message = message;

        // 메뉴 아이템 추가
        add(createSendToRepeaterItem());
        add(createSendToComparerItem());
        add(createCopyUrlItem());
        addSeparator();
        add(createHighlightMenu());
    }

    private JMenuItem createSendToRepeaterItem() {
        JMenuItem item = new JMenuItem("Send to Repeater");
        item.addActionListener(e -> {
            String host = message.request().httpService().host();
            int port = message.request().httpService().port();
            boolean useHttps = message.request().httpService().secure();
            api.repeater().sendToRepeater(message.request());
            api.logging().logToOutput("Sent to Repeater: " + message.request().url());
        });
        return item;
    }

    private JMenuItem createSendToComparerItem() {
        JMenuItem item = new JMenuItem("Send to Comparer");
        item.addActionListener(e -> {
//            api.comparer().sendToComparer(message.request().toString());
            api.logging().logToOutput("Sent to Comparer: " + message.request().url());
        });
        return item;
    }

    private JMenuItem createCopyUrlItem() {
        JMenuItem item = new JMenuItem("Copy URL");
        item.addActionListener(e -> {
            String url = message.request().url();
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
                message.annotations().setHighlightColor(color);

            });
            highlightMenu.add(colorItem);
        }

        return highlightMenu;
    }

    // 메뉴 표시 함수
    public static void showMenu(MontoyaApi api, HttpRequestResponse message, java.awt.Component component, int x, int y) {
        SwingUtilities.invokeLater(() -> {
            ProxyEntryContextMenu menu = new ProxyEntryContextMenu(api, message);
            menu.show(component, x, y);
        });
    }
}
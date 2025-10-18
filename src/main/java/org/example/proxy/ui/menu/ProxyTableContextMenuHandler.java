package org.example.proxy.ui.menu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.example.proxy.ui.tab.ProxyTableModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

import static burp.api.montoya.core.HighlightColor.NONE;

public class ProxyTableContextMenuHandler {
    MontoyaApi api;

    ProxyTableContextMenuHandler(MontoyaApi api) {
        this.api = api;
    }

    public void copyToClipboard(String text) {
        if (text == null) text = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }

    public void sendToRepeater(HttpRequest httpRequest) {
        this.api.repeater().sendToRepeater(httpRequest);
    }

    public void setHighlight(ProxyTableModel model, int rowIndex, HighlightColor color) {
        HttpRequestResponse message = model.get(rowIndex);
        message.annotations().setHighlightColor(Objects.requireNonNullElse(color, NONE));

        // 색깔 변경 갱신
        model.fireTableRowsUpdated(rowIndex, rowIndex); // JTable 갱신
    }

}

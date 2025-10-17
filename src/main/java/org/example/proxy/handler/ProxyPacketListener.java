package org.example.proxy.handler;

import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.ProxyHistoryFilter;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import org.example.proxy.ProxyTableModel;

public class ProxyPacketListener implements ProxyHistoryFilter {
    private final ProxyTableModel tableModel;

    public ProxyPacketListener(ProxyTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public boolean matches(ProxyHttpRequestResponse requestResponse) {
        this.tableModel.add(requestResponse);
        return true;
    }
}

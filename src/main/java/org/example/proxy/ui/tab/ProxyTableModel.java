package org.example.proxy.ui.tab;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import org.example.proxy.ProxyPacketEntry;

import javax.swing.table.AbstractTableModel;
import java.util.*;


public class ProxyTableModel extends AbstractTableModel {

    private final List<ProxyPacketEntry> log = new ArrayList<>();

    private static final String[] COLUMNS = Arrays.stream(ProxyTableColumns.values())
            .map(ProxyTableColumns::getDisplayName)
            .toArray(String[]::new);

    @Override
    public int getRowCount() {
        return log.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        ProxyPacketEntry entry = log.get(rowIndex);
        HttpRequest request = entry.getHttpRequest();
        HttpResponse response = entry.getHttpResponse();

        return switch (columnIndex) {
            case 0 -> rowIndex+1;
            case 1 -> entry.getHost();
            case 2 -> request.method();
            case 3 -> request.path();
            case 4 -> (response != null) ? response.statusCode() : "";
            case 5 -> (response != null) ? entry.getMimeType() : "";
            case 6 -> entry.getExtension();
            case 7 -> entry.getTitle();
            case 8 -> (response != null) ? response.body().length() : "";
            case 9 -> entry.getIp();
            case 10 -> entry.getTime();
            case 11 -> entry.getListenerInterface();
            default -> "";
        };
    }

    public synchronized void add(HttpRequestResponse message, String listenerInterface, InterceptedResponse interceptedResponse) {
        log.add(new ProxyPacketEntry(message, listenerInterface, interceptedResponse));
        fireTableRowsInserted(log.size() - 1, log.size() - 1);
    }

    public synchronized ProxyPacketEntry get(int rowIndex) {
        return (ProxyPacketEntry) log.get(rowIndex);
    }
}
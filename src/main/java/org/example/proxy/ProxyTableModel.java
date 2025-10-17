package org.example.proxy;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class ProxyTableModel extends AbstractTableModel {
    private final List<Map<String, Object>> log = new ArrayList<>();

    private static final String[] COLUMNS = {
            "#", "Host", "Method", "Path", "Status", "Length", "MIME", "Ext", "Title", "IP", "Time"
    };

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
        Map<String, Object> entry = log.get(rowIndex);
        HttpRequestResponse message = (HttpRequestResponse) entry.get("message");
        HttpRequest request = message.request();
        HttpResponse response = message.response();

        return switch (columnIndex) {
            case 0 -> rowIndex+1;
            case 1 -> entry.get("host");
            case 2 -> request.method();
            case 3 -> request.path();
            case 4 -> (response != null) ? response.statusCode() : "";
            case 5 -> (response != null) ? response.body().length() : "";
            case 6 -> (response != null) ? response.mimeType() : "";
            case 7 -> entry.get("extension");
            case 8 -> entry.get("title");
            case 9 -> entry.get("ip");
            case 10 -> entry.get("time");
            default -> "";
        };
    }

    public synchronized void add(HttpRequestResponse message) {
        HttpRequest request = message.request();
        HttpResponse response = message.response();

        String host = "";
        String extension = "";
        String title = "";
        String ip = "";
        String time = "";

        try {
            host = new URL(request.url()).getHost();
        } catch (MalformedURLException e) {
            host = "???";
        }

        // 확장자 추출
        String path = request.path();
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash) {
            extension = path.substring(lastDot + 1);
        }

        // title 추출
        if (response != null && response.body() != null) {
            String body = response.body().toString();
            Matcher matcher = Pattern.compile("(?i)<title>(.*?)</title>").matcher(body);
            if (matcher.find()) {
                title = matcher.group(1).trim();
            }
        }

        // IP 추출 (캐시)
        try {
            InetAddress inet = InetAddress.getByName(host);
            ip = inet.getHostAddress();
        } catch (Exception e) {
            ip = "???";
        }

        // 시간 고정
        time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Map으로 저장
        Map<String, Object> entry = new HashMap<>();
        entry.put("message", message);
        entry.put("host", host);
        entry.put("extension", extension);
        entry.put("title", title);
        entry.put("ip", ip);
        entry.put("time", time);

        log.add(entry);
        fireTableRowsInserted(log.size() - 1, log.size() - 1);
    }

    public synchronized HttpRequestResponse get(int rowIndex) {
        return (HttpRequestResponse) log.get(rowIndex).get("message");
    }
}
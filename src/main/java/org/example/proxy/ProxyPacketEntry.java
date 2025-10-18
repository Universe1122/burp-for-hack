package org.example.proxy;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyPacketEntry {
    public final HttpRequestResponse httpRequestResponse;
    public final String listenerInterface;
    public final String host;
    public final String extension;
    public final String title;
    public final String ip;
    public final String time;

    public ProxyPacketEntry(HttpRequestResponse httpRequestResponse, String listenerInterface) {
        this.httpRequestResponse = httpRequestResponse;
        this.listenerInterface = listenerInterface;
        this.host = this.setHost(httpRequestResponse.request());
        this.extension = this.setExtension(httpRequestResponse.request());
        this.title = this.setHtmlTitle(httpRequestResponse.response());
        this.ip = this.setIp(this.host);
        this.time = this.setTime();
    }

    public String setHost(HttpRequest httpRequest) {
        try {
            return new URL(httpRequest.url()).getHost();
        } catch (MalformedURLException e) {
            return "???";
        }
    }

    public String setExtension(HttpRequest httpRequest) {
        String path = httpRequest.path();
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash) {
            return path.substring(lastDot + 1);
        }

        return "";
    }

    public String setHtmlTitle(HttpResponse httpResponse) {
        if (httpResponse != null && httpResponse.body() != null) {
            String body = httpResponse.body().toString();
            Matcher matcher = Pattern.compile("(?i)<title>(.*?)</title>").matcher(body);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        return "";
    }

    public String setIp(String host) {
        try {
            InetAddress inet = InetAddress.getByName(host);
            return inet.getHostAddress();
        } catch (Exception e) {
            return "???";
        }
    }

    public String setTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public HttpRequestResponse getHttpRequestResponse() {
        return httpRequestResponse;
    }

    public String getHost() {
        return host;
    }

    public String getExtension() {
        return extension;
    }

    public String getTitle() {
        return title;
    }

    public String getIp() {
        return ip;
    }

    public String getTime() {
        return time;
    }

    public String getListenerInterface() {
        return listenerInterface;
    }
}

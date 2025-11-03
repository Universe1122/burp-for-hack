package org.example.proxy.tab.proxylistener;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.MimeType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import org.example.MontoyaApiProvider;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyPacketEntry {
    public final HttpRequestResponse httpRequestResponse;
    public final HttpRequest httpRequest;
    public final HttpResponse httpResponse;
    public final String listenerInterface;
    public final String host;
    public final String extension;
    public final String title;
    public final String ip;
    public final String time;
    public final InterceptedResponse interceptedResponse;
    public final String mimeType;
    public final int MAX_BODY_SIZE = 1024 * 1024 * 5; // 5MB로 제한

    public ProxyPacketEntry(HttpRequestResponse httpRequestResponse, String listenerInterface, InterceptedResponse interceptedResponse) {
        this.httpRequestResponse = httpRequestResponse;
        this.httpRequest = httpRequestResponse.request();
        this.httpResponse = httpRequestResponse.response();
        this.listenerInterface = listenerInterface;
        this.host = this.setHost();
        this.extension = this.setExtension();
        this.ip = this.setIp();
        this.time = this.setTime();
        this.interceptedResponse = interceptedResponse;
        this.mimeType = this.setMimeType();
        this.title = this.setHtmlTitle();
    }

    public String setHost() {
        try {
            return new URL(this.httpRequest.url()).getHost();
        } catch (MalformedURLException e) {}
        return "";
    }

    public String setExtension() {
        try{
            URL url = new URL(this.httpRequest.url());
            String tmpPath = url.getPath().replaceAll("\\\\", "/");
            tmpPath = tmpPath.substring(tmpPath.lastIndexOf("/"));
            int position = tmpPath.lastIndexOf('.');
            if (position >= 0) {
                return tmpPath.substring(position + 1);
            }
        } catch (MalformedURLException e) {}

        return "";
    }

    public String setHtmlTitle() {
        if (this.httpResponse == null || this.httpResponse.body() == null) return "";
        if (this.httpResponse.body().length() > MAX_BODY_SIZE) return "";
        if (this.getMimeType() != MimeType.HTML.toString()) return "";

        String body = new String(this.httpResponse.body().getBytes(), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("<title>(.+?)</title>", Pattern.CASE_INSENSITIVE).matcher(body);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }

    public String setIp() {
        try {
            InetAddress inet = InetAddress.getByName(this.host);
            return inet.getHostAddress();
        } catch (Exception e) {
            return "???";
        }
    }

    public String setMimeType() {
        MimeType mimeType = this.getHttpResponse().inferredMimeType();

        return switch (mimeType) {
            case NONE, UNRECOGNIZED, AMBIGUOUS, IMAGE_UNKNOWN, LEGACY_SER_AMF -> "";
            default -> mimeType.toString();
        };
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

    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    public InterceptedResponse getInterceptedResponse() {
        return interceptedResponse;
    }

    public String getMimeType() {
        return mimeType;
    }
}

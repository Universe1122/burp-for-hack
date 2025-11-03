package org.example.proxy.tab.credential;

import burp.api.montoya.http.message.requests.HttpRequest;
import org.example.proxy.tab.proxylistener.ProxyPacketEntry;

import java.util.Objects;
import java.util.concurrent.*;

public class CredentialWatcherService {
    private final ExecutorService incomingExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<String, CredentialEntry> watchers = new ConcurrentHashMap<>();

    public void handleProxyEntry(ProxyPacketEntry proxyPacketEntry) {
        this.incomingExecutor.submit(() -> this.processEntry(proxyPacketEntry));
    }

    public void add(CredentialEntry credentialEntry) {
        String key = this.keyOf(credentialEntry);
        this.watchers.put(key, credentialEntry);
    }

    public void remove(CredentialEntry credentialEntry) {
        String key = this.keyOf(credentialEntry);
        this.watchers.remove(key);
    }

    public void update(CredentialEntry before, CredentialEntry after) {
        String oldKey = this.keyOf(before);
        String newKey = this.keyOf(after);

        this.watchers.remove(oldKey);
        this.watchers.put(newKey, after);
    }

    private String keyOf(CredentialEntry e) {
        return e.getType().name() + ":" + e.getName();
    }

    private void processEntry(ProxyPacketEntry proxyPacketEntry) {
        this.watchers.forEach((key, credentialEntry) -> {
            HttpRequest httpRequest = proxyPacketEntry.getHttpRequest();

            switch (credentialEntry.getType()) {
                case HEADER -> {
                    String value = httpRequest.headerValue(credentialEntry.getName());
                    credentialEntry.setCurrentValue(value);
                }
                case COOKIE -> {
                    String value = httpRequest.headerValue("Cookie");
                    String[] cookies = value.split(";");

                    for (String cookie: cookies) {
                        cookie = cookie.trim();
                        String[] cookieInfo = cookie.split("=", 2);

                        String cookieName = cookieInfo[0].trim();
                        String cookieValue = cookieInfo[1].trim();

                        if (Objects.equals(cookieName, credentialEntry.getName()) &&
                            !Objects.equals(cookieValue, "") &&
                            !Objects.equals(cookieValue, credentialEntry.getCurrentValue())
                        ) {
                            credentialEntry.setCurrentValue(cookieValue);
                            break;
                        }
                    }
                }
            }
        });
    }
}
package org.example.proxy.tab.credential;

import burp.api.montoya.http.message.Cookie;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.example.proxy.tab.proxylistener.CookieEntry;
import org.example.proxy.tab.proxylistener.ProxyPacketEntry;

import java.lang.reflect.Proxy;
import java.util.List;
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
                    List<CookieEntry> cookieEntries = ProxyPacketEntry.parseCookies(httpRequest);

                    for (CookieEntry cookieEntry: cookieEntries) {
                        if (Objects.equals(cookieEntry.getName(), credentialEntry.getName()) &&
                                !Objects.equals(cookieEntry.getValue(), "") &&
                                !Objects.equals(cookieEntry.getValue(), credentialEntry.getCurrentValue())
                        ) {
                            credentialEntry.setCurrentValue(cookieEntry.getValue());
                            break;
                        }
                    }
                }
            }
        });
    }
}
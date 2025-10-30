package org.example.proxy.tab.proxylistener;

public enum ProxyTableColumns {
    INDEX("#"),
    HOST("Host"),
    METHOD("Method"),
    PATH("Path"),
    STATUS("Status"),
    MIME("MIME"),
    EXT("Ext"),
    TITLE("Title"),
    LENGTH("Length"),
    IP("IP"),
    TIME("Time"),
    LISTENER_INTERFACE("Listener Interface");

    private final String displayName;

    ProxyTableColumns(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
package org.example.proxy;

public enum ProxyTableColumns {
    INDEX("#"),
    HOST("Host"),
    METHOD("Method"),
    PATH("Path"),
    STATUS("Status"),
    LENGTH("Length"),
    MIME("MIME"),
    EXT("Ext"),
    TITLE("Title"),
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
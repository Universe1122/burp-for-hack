package org.example.proxy.ui.tab;

public enum CredentialTableColumns {
    WATCHER_TYPE("Watcher Type"),
    NAME("Name"),
    CURRENT_VALUE("Current Value"),
    LAST_UPDATED_TIME("Last Updated Time");

    private final String displayName;

    CredentialTableColumns(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

package org.example.proxy.tab.proxylistener;

public class CookieEntry {
    private String name;
    private String value;

    public CookieEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return this.name + "=" + this.value + ";";
    }
}

package org.example.proxy.tab.credential;

import java.util.Date;
import java.util.Objects;

public class CredentialEntry {
    public enum Type {
        HEADER,
        COOKIE
    }

    private Type type;
    private String name;
    private String current_value;
    private Date date;

    public CredentialEntry(Type type, String name) {
        this.type = type;
        this.name = name;
        this.current_value = null;
        this.date = null;
    }

    public void setType(Type type) {
        this.type = type;
    }
    public void setType(String type) {
        this.type = Objects.equals(type, CredentialEntry.Type.COOKIE.name()) ? CredentialEntry.Type.COOKIE : CredentialEntry.Type.HEADER;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCurrentValue(String currentValue) {
        this.current_value = currentValue;
    }
    public void setDate() {
        this.date = new Date();
    }

    public Type getType() {
        return this.type;
    }
    public String getName() {
        return this.name;
    }
    public String getCurrentValue() {
        return this.current_value;
    }
    public Date getDate() {
        return this.date;
    }
}

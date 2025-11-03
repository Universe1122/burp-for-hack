package org.example.global;

import org.example.proxy.tab.credential.CredentialTableModel;
import org.example.proxy.tab.proxylistener.ProxyTableModel;

import java.util.ArrayList;
import java.util.List;

public class ModelProvider {
    private static List<ProxyTableModel> proxyTableModels = new ArrayList<>();;
    private static List<CredentialTableModel> credentialTableModels = new ArrayList<>();;

    private ModelProvider() {}

    public static void addProxyTableModel(ProxyTableModel model) {
        proxyTableModels.add(model);
    }

    public static void addCredentialTableModel(CredentialTableModel model) {
        credentialTableModels.add(model);
    }

    public static ProxyTableModel getProxyTableModel(int index) {
        return proxyTableModels.get(index);
    }

    public static List<ProxyTableModel> getProxyTableModels() {
        return proxyTableModels;
    }

    public static CredentialTableModel getCredentialTableModel(int index) {
        return credentialTableModels.get(index);
    }

    public static List<CredentialTableModel> getCredentialTableModels() {
        return credentialTableModels;
    }

    public static void removeProxyTableModel(int index) {
        proxyTableModels.remove(index);
    }

    public static void removeCredentialTableModel(int index) {
        credentialTableModels.remove(index);
    }
}
package org.example.menu;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.Cookie;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import org.example.global.ModelProvider;
import org.example.global.MontoyaApiProvider;
import org.example.proxy.tab.credential.CredentialEntry;
import org.example.proxy.tab.credential.CredentialTableModel;
import org.example.proxy.tab.proxylistener.CookieEntry;
import org.example.proxy.tab.proxylistener.ProxyPacketEntry;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CredentialHelper implements ContextMenuItemsProvider {
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event)
    {
        List<Component> menuItemList = new ArrayList<>();

        menuItemList.add(this.createSyncCredentialMenu(event));

        return menuItemList;
    }

    public JMenu createSyncCredentialMenu(ContextMenuEvent event) {
        if (!event.isFromTool(ToolType.REPEATER)) return null;

        JMenu menu = new JMenu("Sync Credential");
        for (CredentialTableModel credentialTableModel : ModelProvider.getCredentialTableModels()) {
            JMenuItem submenu = new JMenuItem(credentialTableModel.getFilterListenerInterface());

            submenu.addActionListener(e -> {
                Optional<MessageEditorHttpRequestResponse> messageEditorHttpRequestResponse = event.messageEditorRequestResponse();
                if (messageEditorHttpRequestResponse.isEmpty()) return;

                HttpRequestResponse httpRequestResponse = messageEditorHttpRequestResponse.get().requestResponse();

                for (CredentialEntry credentialEntry: credentialTableModel.getCredentialEntries()) {
                    switch(credentialEntry.getType()) {
                        case HEADER -> {
                            HttpRequest httpRequest = httpRequestResponse.request().withHeader(credentialEntry.getName(), credentialEntry.getCurrentValue());
                            messageEditorHttpRequestResponse.get().setRequest(httpRequest);
                        }
                        case COOKIE -> {
                            HttpRequest httpRequest = httpRequestResponse.request();
                            List<CookieEntry> cookieEntries = ProxyPacketEntry.parseCookies(httpRequest);
                            StringBuilder cookieToString = new StringBuilder();

                            for (CookieEntry cookieEntry: cookieEntries) {
                                if(Objects.equals(cookieEntry.getName(), credentialEntry.getName())) {
                                    cookieEntry.setValue(credentialEntry.getCurrentValue());
                                }

                                cookieToString.append(cookieEntry.toString());
                            }

                            httpRequest = httpRequestResponse.request().withHeader("Cookie", cookieToString.toString());
                            messageEditorHttpRequestResponse.get().setRequest(httpRequest);
                        }
                    }
                }
            });

            menu.add(submenu);
        }

        return menu;
    }
}

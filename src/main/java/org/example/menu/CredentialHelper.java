package org.example.menu;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import org.example.global.MontoyaApiProvider;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CredentialHelper implements ContextMenuItemsProvider {
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event)
    {
        if (!event.isFromTool(ToolType.REPEATER)) return null;

        List<Component> menuItemList = new ArrayList<>();

        JMenuItem retrieveRequestItem = new JMenuItem("Print request");
        retrieveRequestItem.addActionListener(e -> {
            Optional<MessageEditorHttpRequestResponse> messageEditorHttpRequestResponse = event.messageEditorRequestResponse();
            if (messageEditorHttpRequestResponse.isEmpty()) return;

            HttpRequestResponse httpRequestResponse = messageEditorHttpRequestResponse.get().requestResponse();
            HttpRequest httpRequest = httpRequestResponse.request().withHeader("X-Test-Header", "1");
            messageEditorHttpRequestResponse.get().setRequest(httpRequest);
        });

        menuItemList.add(retrieveRequestItem);

        return menuItemList;
    }
}

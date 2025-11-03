package org.example.proxy.handler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;
import org.example.global.ModelProvider;
import org.example.global.MontoyaApiProvider;
import org.example.proxy.tab.proxylistener.ProxyTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProxyResponseHandler implements burp.api.montoya.proxy.http.ProxyResponseHandler {
    public ProxyResponseHandler() {}

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        return ProxyResponseReceivedAction.continueWith(interceptedResponse);
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        ModelProvider.getProxyTableModels().forEach(proxyTableModel -> {
            MontoyaApiProvider.get().logging().logToOutput(proxyTableModel.getFilterListenerInterface());

            if(Objects.equals(proxyTableModel.getFilterListenerInterface(), interceptedResponse.listenerInterface())) {
                proxyTableModel.add(
                    HttpRequestResponse.httpRequestResponse(
                        interceptedResponse.initiatingRequest(),
                        (HttpResponse) interceptedResponse
                    ),
                    interceptedResponse.listenerInterface(),
                    interceptedResponse
                );
            }
        });
        MontoyaApiProvider.get().logging().logToOutput("\n\n");
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }
}

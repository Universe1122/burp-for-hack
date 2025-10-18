package org.example.proxy.handler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;
import org.example.proxy.ui.tab.ProxyTableModel;

public class ProxyResponseHandler implements burp.api.montoya.proxy.http.ProxyResponseHandler {

    private final MontoyaApi api;
    private final ProxyTableModel tableModel;

    public ProxyResponseHandler(MontoyaApi api, ProxyTableModel tableModel) {
        this.api = api;
        this.tableModel = tableModel;
    }

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        return ProxyResponseReceivedAction.continueWith(interceptedResponse);
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        this.tableModel.add(
                HttpRequestResponse.httpRequestResponse(
                    interceptedResponse.initiatingRequest(), (HttpResponse) interceptedResponse
                ),
                interceptedResponse.listenerInterface()
        );
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }
}

package org.example.proxy.handler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.ProxyHistoryFilter;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import com.sun.tools.javac.Main;
import org.example.proxy.ProxyTableModel;

public class MyHttpHandler implements HttpHandler {
    private final ProxyTableModel tableModel;
    public MyHttpHandler(ProxyTableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        HttpRequestResponse message = HttpRequestResponse.httpRequestResponse(
                responseReceived.initiatingRequest(), responseReceived
        );

        // ProxyTableModel에 추가
        this.tableModel.add(message);
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}
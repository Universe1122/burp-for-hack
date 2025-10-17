//package org.example.proxy;
//
//import burp.api.montoya.MontoyaApi;
//import burp.api.montoya.http.message.HttpRequestResponse;
//import burp.api.montoya.proxy.ProxyHttpRequestResponse;
//
//import java.util.List;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class TestProxyHistoryWatcher {
//    private final MontoyaApi api;
//    private final ProxyTableModel tableModel;
//    private final ScheduledExecutorService executor;
//    private int lastSize = 0;
//
//    public TestProxyHistoryWatcher(MontoyaApi api, ProxyTableModel tableModel) {
//        this.api = api;
//        this.tableModel = tableModel;
//        this.executor = Executors.newSingleThreadScheduledExecutor();
//    }
//
//    public void start() {
//        // 1초마다 Burp Proxy History 확인
//        executor.scheduleAtFixedRate(() -> {
//            try {
//                List<HttpRequestResponse> history = api.proxy().history();
//
//                if (history.size() > lastSize) {
//                    // 새 항목이 생김
//                    List<HttpRequestResponse> newEntries = history.subList(lastSize, history.size());
//                    lastSize = history.size();
//
//                    // UI 갱신은 EDT에서
//                    for (HttpRequestResponse entry : newEntries) {
//                        api.logging().logToOutput(entry.request().toString());
//                        tableModel.add(entry);
//                    }
//                }
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }, 0, 1, TimeUnit.SECONDS);
//    }
//
//    public void stop() {
//        executor.shutdownNow();
//    }
//}

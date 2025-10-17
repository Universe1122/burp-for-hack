package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import org.example.proxy.ProxyTab;
import org.example.proxy.ProxyTableModel;
import org.example.proxy.handler.MyHttpHandler;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        // set extension name
        api.extension().setName("Hello world extension");

        Logging logging = api.logging();

//        BasicMenuItem alertEventItem = BasicMenuItem.basicMenuItem("Raise critical alert").withAction(() -> api.logging().raiseCriticalEvent("Alert from extension"));
//
//        BasicMenuItem basicMenuItem = MenuItem.basicMenuItem("Unload extension");
//        MenuItem unloadExtensionItem = basicMenuItem.withAction(() -> api.extension().unload());
//
//        Menu menu = Menu.menu("Menu bar").withMenuItems(alertEventItem, unloadExtensionItem);
//
//        api.userInterface().menuBar().registerMenu(menu);

        // 패킷 정보 저장 및 출력 포맷 정의
        ProxyTableModel proxyTableModel = new ProxyTableModel();

        // 새로운 탭 생성
        ProxyTab proxyTab = new ProxyTab(api, proxyTableModel);
        api.userInterface().registerSuiteTab("Custom logger", proxyTab.constructLoggerTab());
        api.http().registerHttpHandler(new MyHttpHandler(proxyTableModel));
        // 패킷 정보 저장하는 handler 등록
//        new ProxyPacketListener(proxyTableModel, api);
//        api.proxy().history(new ProxyPacketListener(proxyTableModel, api));

//        TestProxyHistoryWatcher watcher = new TestProxyHistoryWatcher(api, proxyTableModel);
//        watcher.start();


//        api.proxy().registerRequestHandler(new CustomProxyRequestHandler(api));
//        api.userInterface().registerContextMenuItemsProvider(new ProxyInterfaceContextMenu(api));
//        api.extension().registerUnloadingHandler(new ProxyInterfaceMenu(api));
    }
}

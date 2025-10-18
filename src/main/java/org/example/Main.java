package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import org.example.proxy.ui.tab.ProxyTab;
import org.example.proxy.ui.tab.ProxyTableModel;
import org.example.proxy.handler.ProxyResponseHandler;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("Hello world extension");

        ProxyTableModel proxyTableModel = new ProxyTableModel();
        ProxyTab proxyTab = new ProxyTab(api, proxyTableModel);
        api.userInterface().registerSuiteTab("Custom logger", proxyTab.constructLoggerTab());
        api.proxy().registerResponseHandler(new ProxyResponseHandler(api, proxyTableModel));
    }
}

package org.example.proxy.tab;

import org.example.proxy.tab.credential.CredentialController;
import org.example.proxy.tab.credential.CredentialWatcherService;
import org.example.proxy.tab.proxyhistory.ProxyHistoryController;
import org.example.proxy.tab.proxylistener.ProxyListenerController;
import org.example.proxy.tab.credential.CredentialTableModel;
import org.example.proxy.tab.proxylistener.ProxyTableModel;

import javax.swing.*;
import java.awt.*;

public class SettingController {
    private final JTabbedPane upperTabs;  // 상위 탭 (Proxy History, Settings 등)
    private final JTabbedPane credentialTabs; // credential watcher setting 을 위한 탭
    private final ProxyTableModel globalTableModel;
    private final CredentialTableModel credentialTableModel;

    private final ProxyHistoryController proxyHistoryController;
    private final ProxyListenerController proxyListenerController;
    private final CredentialController credentialController;
    private final CredentialWatcherService credentialWatcherService;

    public SettingController(ProxyTableModel globalTableModel) {
        // 패킷 히스토리 관리
        this.globalTableModel = globalTableModel;
        // 하위 탭 관리
        this.upperTabs = new JTabbedPane();
        this.credentialTabs = new JTabbedPane();
        this.credentialTableModel = new CredentialTableModel();
        this.credentialWatcherService = new CredentialWatcherService();

        this.proxyHistoryController = new ProxyHistoryController(this.globalTableModel);
        this.credentialController = new CredentialController(this.credentialTabs, this.credentialTableModel, this.credentialWatcherService);
        this.proxyListenerController = new ProxyListenerController(this.upperTabs, this.credentialTabs, this.proxyHistoryController, this.credentialController);

        this.globalTableModel.setCredentialWatcherService(this.credentialWatcherService);
    }

    public Component constructLoggerTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(this.upperTabs, BorderLayout.CENTER);

        JPanel settingsPanel = createSettingsPanel();
        this.upperTabs.addTab("Settings", settingsPanel);
//        this.credentialTabs.addTab("+", null);
        return mainPanel;
    }

    // 하위 Settings 탭 구성 (사용자가 하위 탭을 추가할 수 있게)
    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 7, 10, 7));
        JPanel mergePanel = new JPanel();
        mergePanel.setLayout(new BoxLayout(mergePanel, BoxLayout.Y_AXIS));

        mergePanel.add(this.proxyListenerController.createPanel());
        mergePanel.add(this.createSeparatorPanel());
        mergePanel.add(this.credentialController.createPanel());

        settingsPanel.add(mergePanel, BorderLayout.NORTH);

        return settingsPanel;
    }

    private JPanel createSeparatorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        panel.add(separator, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 위/아래 여백
        return panel;
    }
}

package org.example.proxy.tab.proxylistener;

import org.example.proxy.tab.credential.CredentialController;
import org.example.proxy.tab.proxyhistory.ProxyHistoryController;
import org.example.proxy.tab.TabController;

import javax.swing.*;
import java.awt.*;

public class ProxyListenerController {
    private final JTabbedPane upperTabs;
    private final JTabbedPane credentialTabs;

    public ProxyListenerController(JTabbedPane upperTabs, JTabbedPane credentialTabs) {
        this.upperTabs = upperTabs;
        this.credentialTabs = credentialTabs;
    }

    public JPanel createPanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Proxy Listener Setting");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addTabButton = new JButton("새로운 탭 추가");
        JTextField tabNameField = new JTextField("새 탭 이름 입력", 15);
        JTextField filterListenerInterfaceField =  new JTextField("127.0.0.1:8080", 15);
        formPanel.add(tabNameField);
        formPanel.add(filterListenerInterfaceField);
        formPanel.add(addTabButton);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.add(titlePanel);
        topPanel.add(formPanel);

        addTabButton.addActionListener(e -> {
            String newTabName = tabNameField.getText().trim();
            String filterListenerInterface = filterListenerInterfaceField.getText().trim();

            ProxyHistoryController proxyHistoryController = new ProxyHistoryController(filterListenerInterface);
            CredentialController credentialController = new CredentialController(this.credentialTabs);

            if (newTabName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "탭 이름을 입력하세요.");
                return;
            }
            if (filterListenerInterface.isEmpty()) {
                JOptionPane.showMessageDialog(null, "리스너 인터페이스를 입력하세요. ex: 127.0.0.1:8080");
                return;
            }

            JPanel proxyHistoryPanel = proxyHistoryController.createPanel();

            // 사용자 정의 하위 탭 추가
            this.upperTabs.addTab(newTabName, proxyHistoryPanel);
            // X 버튼 추가 (paired with credentialTabs so closing one removes the other)
            this.upperTabs.setTabComponentAt(this.upperTabs.getTabCount() - 1, new TabController(this.upperTabs, this.credentialTabs, newTabName));
            // 바로 새 탭으로 이동
            this.upperTabs.setSelectedComponent(proxyHistoryPanel);

            // credential watcher setting 패널에 새로운 탭 생성
            this.credentialTabs.addTab(newTabName, credentialController.createFormPanel(filterListenerInterface));
        });

        return topPanel;
    }
}

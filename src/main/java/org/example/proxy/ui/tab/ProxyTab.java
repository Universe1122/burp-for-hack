package org.example.proxy.ui.tab;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import org.example.proxy.ProxyPacketEntry;
import org.example.proxy.ui.menu.ProxyEntryContextMenu;
import org.example.proxy.ui.menu.ProxyTableContextMenu;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class ProxyTab {
    private final MontoyaApi api;
    private final JTabbedPane upperTabs;  // 상위 탭 (Proxy History, Settings 등)
    private final ProxyTableModel globalTableModel;

    public ProxyTab(MontoyaApi api, ProxyTableModel globalTableModel) {
        this.api = api;
        // 패킷 히스토리 관리
        this.globalTableModel = globalTableModel;
        // 하위 탭 관리
        this.upperTabs = new JTabbedPane();
    }

    public Component constructLoggerTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(upperTabs, BorderLayout.CENTER);

        JPanel settingsPanel = createSettingsPanel();
        upperTabs.addTab("Settings", settingsPanel);

        return mainPanel;
    }

    // Proxy History UI 구성
    private JPanel createProxyHistoryPanel(String filterListenerInterface) {
        JPanel panel = new JPanel(new BorderLayout());
//        ProxyTableContextMenu proxyTableContextMenu = new ProxyTableContextMenu(this.api);
        JTable table = new JTable(this.globalTableModel);

        // 각 컬럼에 대해 가로폭 지정하기
        int[] columnWidths = {20, 75, 30, 200, 60, 80, 100, 50, 200, 120, 150};

        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

//        proxyTableContextMenu.attach(table, this.globalTableModel);

        TableRowSorter<ProxyTableModel> sorter = new TableRowSorter<>(this.globalTableModel);
        table.setRowSorter(sorter);

        sorter.setComparator(0, Comparator.comparingInt(o -> (Integer) o)); // Index 열
        sorter.setComparator(4, Comparator.comparingInt(o -> parseIntSafe(o))); // Status
        sorter.setComparator(5, Comparator.comparingInt(o -> parseIntSafe(o))); // Length
        sorter.setRowFilter(new RowFilter() {
            // 이용자가 지정한 listener interface에 따라 보여줄 패킷 필터링
            @Override
            public boolean include(Entry entry) {
                int listenerColIndex = ProxyTableColumns.LISTENER_INTERFACE.ordinal();
                String listener = (String) entry.getValue(listenerColIndex);
                return listener.equals(filterListenerInterface);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // 상단: 히스토리 테이블
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setResizeWeight(0.4);
        verticalSplit.setTopComponent(scrollPane);

        // 하단: Request/Response 뷰어 (좌우 배치)
        UserInterface ui = api.userInterface();
        HttpRequestEditor requestViewer = ui.createHttpRequestEditor(READ_ONLY);
        HttpResponseEditor responseViewer = ui.createHttpResponseEditor(READ_ONLY);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplit.setResizeWeight(0.5);
        horizontalSplit.setLeftComponent(requestViewer.uiComponent());
        horizontalSplit.setRightComponent(responseViewer.uiComponent());

        verticalSplit.setBottomComponent(horizontalSplit);
        panel.add(verticalSplit, BorderLayout.CENTER);

        // 테이블 클릭 시 Request/Response 갱신
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    var message = globalTableModel.get(selectedRow);
                    if (message != null) {
                        requestViewer.setRequest(message.getHttpRequest());
                        responseViewer.setResponse(message.getHttpResponse());
                    }
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);

                        ProxyPacketEntry entry = globalTableModel.get(row);

                        ProxyEntryContextMenu.showMenu(api, entry.getHttpRequestResponse(), e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) mouseReleased(e);
            }
        });

        return panel;
    }

    // 하위 Settings 탭 구성 (사용자가 하위 탭을 추가할 수 있게)
    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new BorderLayout());

        JButton addTabButton = new JButton("새로운 탭 추가");
        JTextField tabNameField = new JTextField("새 탭 이름 입력", 15);
        JTextField filterListenerInterfaceField =  new JTextField("리스너 인터페이스 입력", 15);

        JPanel topPanel = new JPanel();
        topPanel.add(tabNameField);
        topPanel.add(filterListenerInterfaceField);
        topPanel.add(addTabButton);

        settingsPanel.add(topPanel, BorderLayout.NORTH);

        addTabButton.addActionListener(e -> {
            String newTabName = tabNameField.getText().trim();
            String filterListenerInterface = filterListenerInterfaceField.getText().trim();
            if (newTabName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "탭 이름을 입력하세요.");
                return;
            }
            if (filterListenerInterface.isEmpty()) {
                JOptionPane.showMessageDialog(null, "리스너 인터페이스를 입력하세요. ex: 127.0.0.1:8080");
                return;
            }

            JPanel proxyHistoryPanel = createProxyHistoryPanel(filterListenerInterface);

            // 사용자 정의 하위 탭 추가
            upperTabs.addTab(newTabName, proxyHistoryPanel);
            // X 버튼 추가
            upperTabs.setTabComponentAt(upperTabs.getTabCount() - 1, new ClosableTabComponent(upperTabs, newTabName));
            // 바로 새 탭으로 이동
            upperTabs.setSelectedComponent(proxyHistoryPanel);
        });

        return settingsPanel;
    }

    private int parseIntSafe(Object value) {
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return -1;
        }
    }
}

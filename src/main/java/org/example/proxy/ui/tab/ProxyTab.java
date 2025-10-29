package org.example.proxy.ui.tab;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import org.example.MontoyaApiProvider;
import org.example.proxy.ProxyPacketEntry;
import org.example.proxy.ui.menu.ProxyEntryContextMenu;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;
import java.util.Objects;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class ProxyTab {
    private final MontoyaApi api;
    private final JTabbedPane upperTabs;  // 상위 탭 (Proxy History, Settings 등)
    private final JTabbedPane credentialTabs; // credential watcher setting 을 위한 탭
    private final ProxyTableModel globalTableModel;
    private final CredentialTableModel credentialTableModel;

    public ProxyTab(MontoyaApi api, ProxyTableModel globalTableModel) {
        this.api = api;
        // 패킷 히스토리 관리
        this.globalTableModel = globalTableModel;
        // 하위 탭 관리
        this.upperTabs = new JTabbedPane();
        this.credentialTabs = new JTabbedPane();
        this.credentialTableModel = new CredentialTableModel();
    }

    public Component constructLoggerTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(this.upperTabs, BorderLayout.CENTER);

        JPanel settingsPanel = createSettingsPanel();
        this.upperTabs.addTab("Settings", settingsPanel);
        this.credentialTabs.addTab("+", null);
//        this.credentialTabs.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                int index = credentialTabs.getSelectedIndex();
//                if (index == 0) {
//                    createCredentialPanel();
//                }
//            }
//        });

        return mainPanel;
    }

    // Proxy History UI 구성
    private JPanel createProxyHistoryPanel(String filterListenerInterface) {
        JPanel panel = new JPanel(new BorderLayout());
//        ProxyTableContextMenu proxyTableContextMenu = new ProxyTableContextMenu(this.api);
        JTable table = new JTable(this.globalTableModel);

        // 여러개의 컬럼을 선택할 수 있도록 수정
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // 각 컬럼에 대해 가로폭 지정하기
        int[] columnWidths = {10, 100, 20, 450, 30, 60, 50, 60, 60, 100, 150};

        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        TableRowSorter<ProxyTableModel> sorter = new TableRowSorter<>(this.globalTableModel);
        table.setRowSorter(sorter);
        sorter.setMaxSortKeys(1);
        sorter.setSortsOnUpdates(true);
        sorter.setComparator(0, Comparator.comparingInt(o -> (Integer) o)); // Index 열
        sorter.setComparator(4, Comparator.comparingInt(o -> parseIntSafe(o))); // Status
        sorter.setComparator(5, Comparator.comparingInt(o -> parseIntSafe(o))); // Length
        // 이용자가 지정한 listener interface에 따라 보여줄 패킷 필터링
        sorter.setRowFilter(new RowFilter() {
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

        // 테이블 선택 시 Request/Response 갱신
        table.getSelectionModel().addListSelectionListener(e -> {
            // 마우스로 클릭하거나 방향키로 이동할 때도 모두 트리거됨
            if (!e.getValueIsAdjusting()) {
                SwingUtilities.invokeLater(() -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow < 0) return;

                    selectedRow = table.convertRowIndexToModel(selectedRow);
                    if (selectedRow < 0) return;

                    ProxyPacketEntry proxyPacketEntry =  globalTableModel.get(selectedRow);
                    if  (proxyPacketEntry == null) return;

                    requestViewer.setRequest(proxyPacketEntry.getHttpRequest());
                    responseViewer.setResponse(proxyPacketEntry.getHttpResponse());
                });
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

                        ProxyEntryContextMenu.showMenu(api, entry, e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) mouseReleased(e);
            }
        });

        // 패킷 하이라이트
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                ProxyTableModel model = (ProxyTableModel) table.getModel();
                ProxyPacketEntry entry = model.get(row);
                HighlightColor color = entry.getInterceptedResponse().annotations().highlightColor();

                if (!isSelected) {
                    if (color != null && color != HighlightColor.NONE) {
                        c.setBackground(highlightColorToAwt(color));
                    } else {
                        c.setBackground(null);
                    }
                }

                return c;
            }
        });

        // send to repeater 단축키 이벤트 핸들러
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

                boolean isShortcutPressed;
                if (isMac) {
                    isShortcutPressed = e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_R; // ⌘ + R
                } else {
                    isShortcutPressed = e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R; // Ctrl + R
                }

                if (!isShortcutPressed) return;

                int row = table.getSelectedRow();
                if (row < 0)  return;

                row = table.convertRowIndexToModel(row);
                api.repeater().sendToRepeater(globalTableModel.get(row).getHttpRequest());
            }
        });

        return panel;
    }

    // 하위 Settings 탭 구성 (사용자가 하위 탭을 추가할 수 있게)
    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 7, 10, 7));
        JPanel mergePanel = new JPanel();
        mergePanel.setLayout(new BoxLayout(mergePanel, BoxLayout.Y_AXIS));

        mergePanel.add(this.createNewProxyFormPanel());
        mergePanel.add(this.createSeparatorPanel());
        mergePanel.add(this.createCredentialSettingPanel());

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

    private JPanel createNewProxyFormPanel() {
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
            this.upperTabs.addTab(newTabName, proxyHistoryPanel);
            // X 버튼 추가 (paired with credentialTabs so closing one removes the other)
            this.upperTabs.setTabComponentAt(this.upperTabs.getTabCount() - 1, new CustomTabController(this.upperTabs, this.credentialTabs, newTabName));
            // 바로 새 탭으로 이동
            this.upperTabs.setSelectedComponent(proxyHistoryPanel);

            // credential watcher setting 패널에 새로운 탭 생성
            this.credentialTabs.addTab(newTabName, this.createCredentialPanel());
        });

        return topPanel;
    }

    private JPanel createCredentialSettingPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Credential Watcher Setting");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        this.credentialTabs.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                MontoyaApiProvider.get().logging().logToOutput("add tab");
            }

            @Override
            public void componentRemoved(ContainerEvent e) {}
        });

        topPanel.add(titlePanel);
        topPanel.add(this.credentialTabs);

        return topPanel;
    }

    private JPanel createCredentialPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 여백
        JButton addWatcher = new JButton("Add watcher");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        buttonPanel.add(addWatcher);
        buttonPanel.add(Box.createVerticalStrut(10)); // 버튼 사이 간격
        buttonPanel.add(edit);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(delete);

        addWatcher.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel), "Add Watcher", true);
            dialog.setSize(300, 140);
            dialog.setLocationRelativeTo(panel);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel typeLabel = new JLabel("Type:");
            String[] types = {CredentialEntry.Type.COOKIE.name(), CredentialEntry.Type.HEADER.name()};
            JComboBox<String> typeCombo = new JComboBox<>(types);
            typePanel.add(typeLabel);
            typePanel.add(typeCombo);

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField(15);
            namePanel.add(nameLabel);
            namePanel.add(nameField);

            // 패널에 추가
            contentPanel.add(typePanel);
            contentPanel.add(namePanel);

            // 버튼
            JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");
            buttonPanelDialog.add(okButton);
            buttonPanelDialog.add(cancelButton);

            okButton.addActionListener(ev -> {
                String type = typeCombo.getSelectedItem().toString();
                String name = nameField.getText().trim();

                if(name.isEmpty()) {
                    return;
                }

                CredentialEntry credentialEntry = new CredentialEntry(
                        Objects.equals(type, CredentialEntry.Type.COOKIE.name()) ? CredentialEntry.Type.COOKIE : CredentialEntry.Type.HEADER, name
                );

                this.credentialTableModel.add(credentialEntry);
                dialog.dispose();
            });

            cancelButton.addActionListener(ev -> dialog.dispose());

            dialog.setLayout(new BorderLayout());
            dialog.add(contentPanel, BorderLayout.CENTER);
            dialog.add(buttonPanelDialog, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        JTable table = new JTable(this.credentialTableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(buttonPanel, BorderLayout.WEST);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;

//        this.credentialTabs.addTab(String.valueOf(this.credentialTabs.getTabCount() - 1), panel);
//        this.credentialTabs.setTabComponentAt(this.credentialTabs.getTabCount() - 1, new CustomTabController(this.credentialTabs, String.valueOf(this.credentialTabs.getTabCount() - 1)));
//        this.credentialTabs.setSelectedIndex(this.credentialTabs.getTabCount() - 1);
    }


    private int parseIntSafe(Object value) {
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return -1;
        }
    }

    private Color highlightColorToAwt(HighlightColor color) {
        return switch (color) {
            case RED -> Color.RED;
            case ORANGE -> Color.ORANGE;
            case YELLOW -> Color.YELLOW;
            case GREEN -> Color.GREEN;
            case CYAN, BLUE -> Color.CYAN;
            case PINK, MAGENTA -> Color.PINK;
            case GRAY -> Color.LIGHT_GRAY;
            default -> Color.WHITE;
        };
    }
}

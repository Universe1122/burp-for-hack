package org.example.proxy.tab.proxyhistory;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import org.example.MontoyaApiProvider;
import org.example.proxy.contextmenu.ProxyEntryContextMenu;
import org.example.proxy.tab.proxylistener.ProxyPacketEntry;
import org.example.proxy.tab.proxylistener.ProxyTableColumns;
import org.example.proxy.tab.proxylistener.ProxyTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class ProxyHistoryController {
    private final ProxyTableModel globalTableModel;

    public ProxyHistoryController(ProxyTableModel globalTableModel) {
        this.globalTableModel = globalTableModel;
    }

    public JPanel createPanel(String filterListenerInterface) {
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
        sorter.setComparator(4, Comparator.comparingInt(o -> this.parseIntSafe(o))); // Status
        sorter.setComparator(5, Comparator.comparingInt(o -> this.parseIntSafe(o))); // Length
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
        UserInterface ui = MontoyaApiProvider.get().userInterface();
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

                    ProxyPacketEntry proxyPacketEntry = this.globalTableModel.get(selectedRow);
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

                        ProxyEntryContextMenu.showMenu(MontoyaApiProvider.get(), entry, e.getComponent(), e.getX(), e.getY());
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
                MontoyaApiProvider.get().repeater().sendToRepeater(globalTableModel.get(row).getHttpRequest());
            }
        });

        return panel;
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

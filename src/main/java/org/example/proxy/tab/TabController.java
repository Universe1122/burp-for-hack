package org.example.proxy.tab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TabController extends JPanel {
    private final JTabbedPane tabbedPane;
    private final JTabbedPane pairedTabbedPane; // optional paired pane to sync/close
    private final JLabel titleLabel;


    public TabController(JTabbedPane _tabbedPane, String title) {
        this(_tabbedPane, null, title);
    }

    public TabController(JTabbedPane _tabbedPane, JTabbedPane pairedPane, String title) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        this.tabbedPane = _tabbedPane;
        this.pairedTabbedPane = pairedPane;
        this.titleLabel = new JLabel(title);

        setOpaque(false);

        add(this.titleLabel);

        JButton closeButton = new JButton("x");
        closeButton.setMargin(new Insets(0, 5, 0, 5));
        closeButton.setFocusable(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);

        Color originalColor = closeButton.getForeground();

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(originalColor);
            }
        });

        this.titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    startEditingTitle();
                }
            }
        });

        closeButton.addActionListener(e -> {
            int index = this.tabbedPane.indexOfTabComponent(this);
            if (index != -1) {
                String title2 = this.tabbedPane.getTitleAt(index);
                this.tabbedPane.remove(index);

                if (this.pairedTabbedPane != null) {
                    int pairedIndex = this.pairedTabbedPane.indexOfTab(title2);
                    if (pairedIndex != -1) {
                        this.pairedTabbedPane.remove(pairedIndex);
                    }
                }
            }
        });
        add(closeButton);
    }

    private void startEditingTitle() {
        int index = this.tabbedPane.indexOfTabComponent(this);
        if (index == -1) return;

        String currentTitle = this.titleLabel.getText();

        // 텍스트필드로 변경
        JTextField editor = new JTextField(currentTitle);
        editor.setBorder(null);
        editor.selectAll();
        remove(titleLabel);
        add(editor, 0); // 라벨 대신 입력창 추가
        revalidate();
        repaint();
        editor.requestFocusInWindow();

        // 입력 완료 (Enter 또는 포커스 잃음)
        ActionListener finishEdit = e -> {
            String newTitle = editor.getText().trim();
            if (newTitle.isEmpty()) newTitle = currentTitle;

            titleLabel.setText(newTitle);
            remove(editor);
            add(titleLabel, 0);
            revalidate();
            repaint();

            tabbedPane.setTitleAt(index, newTitle);
            // sync paired tab title if present (find by old title)
            if (pairedTabbedPane != null) {
                int pairedIndex = pairedTabbedPane.indexOfTab(currentTitle);
                if (pairedIndex != -1) {
                    pairedTabbedPane.setTitleAt(pairedIndex, newTitle);
                }
            }
        };

        editor.addActionListener(finishEdit);
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finishEdit.actionPerformed(null);
            }
        });
    }
}
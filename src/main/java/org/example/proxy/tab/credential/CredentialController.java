package org.example.proxy.tab.credential;

import org.example.MontoyaApiProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Objects;

public class CredentialController {
    private final JTabbedPane credentialTabs;
    private final CredentialTableModel credentialTableModel;

    public CredentialController(JTabbedPane credentialTabs, CredentialTableModel credentialTableModel) {
        this.credentialTabs = credentialTabs;
        this.credentialTableModel = credentialTableModel;
    }

    public JPanel createPanel() {
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

    public JPanel createFormPanel() {
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
    }
}

package org.example.proxy.tab.credential;

import org.example.MontoyaApiProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Objects;
import java.util.Optional;

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

        JTable table = new JTable(this.credentialTableModel);
        JScrollPane scrollPane = new JScrollPane(table);

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
            createCredentialWatchFormPopup(panel, null, null, null);
        });

        delete.addActionListener(e -> {
            int selectRow = table.getSelectedRow();
            if(selectRow != -1) {
                credentialTableModel.delete(selectRow);
            }
        });

        edit.addActionListener(e -> {
            int selectRow = table.getSelectedRow();
            if(selectRow == -1) return;;

            CredentialEntry tmpCredentialEntry = this.credentialTableModel.getCredentialEntry(selectRow);
            this.createCredentialWatchFormPopup(panel, tmpCredentialEntry.getType(), tmpCredentialEntry.getName(), selectRow);
            this.credentialTableModel.fireTableDataChanged();
        });

        panel.add(buttonPanel, BorderLayout.WEST);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void createCredentialWatchFormPopup(JPanel parentPanel, CredentialEntry.Type type, String name, Integer index) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentPanel), "Add Watcher", true);
        dialog.setSize(300, 140);
        dialog.setLocationRelativeTo(parentPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel typeLabel = new JLabel("Type:");
        String[] types = {CredentialEntry.Type.COOKIE.name(), CredentialEntry.Type.HEADER.name()};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        if (type != null) {
            typeCombo.setSelectedItem(type.name());
        }
        typePanel.add(typeLabel);
        typePanel.add(typeCombo);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = name == null ? new JTextField(15) : new JTextField(name,15);
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
            String newType = typeCombo.getSelectedItem().toString();
            String newName = nameField.getText().trim();

            if (newName.isEmpty()) {
                return;
            }

            CredentialEntry credentialEntry = new CredentialEntry(
                    Objects.equals(newType, CredentialEntry.Type.COOKIE.name()) ? CredentialEntry.Type.COOKIE : CredentialEntry.Type.HEADER, newName
            );

            if (index == null) {
                this.credentialTableModel.add(credentialEntry);
            }
            else {
                CredentialEntry tmpCredentialEntry = this.credentialTableModel.getCredentialEntry(index);
                tmpCredentialEntry.setName(newName);
                tmpCredentialEntry.setType(newType);
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(ev -> dialog.dispose());

        dialog.setLayout(new BorderLayout());
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanelDialog, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}

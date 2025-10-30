package org.example.proxy.tab.credential;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CredentialTableModel extends AbstractTableModel {
    private final List<CredentialEntry> credentialEntries = new ArrayList<>();
    private static final String[] COLUMNS = Arrays.stream(CredentialTableColumns.values())
            .map(CredentialTableColumns::getDisplayName)
            .toArray(String[]::new);

    @Override
    public int getRowCount() {
        return this.credentialEntries.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CredentialEntry credentialEntry = credentialEntries.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> credentialEntry.getType();
            case 1 -> credentialEntry.getName();
            case 2 -> credentialEntry.getCurrentValue();
            case 3 -> credentialEntry.getDate();
            default -> null;
        };
    }

    public synchronized void add(CredentialEntry credentialEntry) {
        this.credentialEntries.add(credentialEntry);
        fireTableRowsInserted(credentialEntries.size() - 1, credentialEntries.size() - 1);
    }

    public synchronized void delete(int index) {
        this.credentialEntries.remove(index);
    }

    public synchronized CredentialEntry getCredentialEntry(int rowIndex) {
        return credentialEntries.get(rowIndex);
    }
}

package com.java.swing;

import com.emailclient.MailingListManager;
import com.models.MailingList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MailingListManagerDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private MailingListManager manager;

    public MailingListManagerDialog(JFrame parent) {
        super(parent, "Mailing List Manager", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        manager = MailingListManager.getInstance();
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Name", "Members"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this::onAdd);
        editButton.addActionListener(this::onEdit);
        deleteButton.addActionListener(this::onDelete);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<MailingList> lists = manager.getMailingLists();
        for (MailingList list : lists) {
            tableModel.addRow(new Object[]{list.getName(), String.join(", ", list.getMembers())});
        }
    }

    private void onAdd(ActionEvent e) {
        MailingList list = showEditDialog(null);
        if (list != null) {
            manager.addMailingList(list);
            refreshTable();
        }
    }

    private void onEdit(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String name = (String) tableModel.getValueAt(row, 0);
        MailingList oldList = manager.getMailingListByName(name);
        MailingList updated = showEditDialog(oldList);
        if (updated != null) {
            manager.updateMailingList(name, updated);
            refreshTable();
        }
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String name = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete mailing list '" + name + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeMailingList(name);
            refreshTable();
        }
    }

    private MailingList showEditDialog(MailingList list) {
        JTextField nameField = new JTextField(list == null ? "" : list.getName());
        JTextArea membersArea = new JTextArea(list == null ? "" : String.join("\n", list.getMembers()), 8, 30);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Name:"), BorderLayout.NORTH);
        panel.add(nameField, BorderLayout.CENTER);
        panel.add(new JLabel("Members (one email per line):"), BorderLayout.SOUTH);
        panel.add(new JScrollPane(membersArea), BorderLayout.AFTER_LAST_LINE);
        int result = JOptionPane.showConfirmDialog(this, panel, list == null ? "Add Mailing List" : "Edit Mailing List", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String[] members = membersArea.getText().split("\n");
            MailingList newList = new MailingList();
            newList.setName(name);
            for (String m : members) {
                String email = m.trim();
                if (!email.isEmpty()) newList.addMember(email);
            }
            return newList;
        }
        return null;
    }


} 
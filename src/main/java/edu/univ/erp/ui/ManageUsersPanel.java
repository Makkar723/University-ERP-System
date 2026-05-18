package edu.univ.erp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.domain.UserSummary;
import edu.univ.erp.ui.theme.Theme;

/**
 * Panel for managing users (list and delete).
 * Displays all users in a table and allows admin to delete selected users.
 */
public class ManageUsersPanel extends JPanel {
    private final AdminApi adminApi;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton deleteButton;
    
    public ManageUsersPanel() {
        this.adminApi = new AdminApi();
        initializeUI();
        loadUsers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Manage Users", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Theme.HEADING);
        add(titleLabel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Table
        String[] columnNames = {"User ID", "Username", "Role", "Full Name", "Extra Info"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        userTable.setSelectionForeground(Color.BLACK);
        userTable.getTableHeader().setBackground(Theme.ACCENT);
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.getTableHeader().setFont(userTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadUsers());
        buttonPanel.add(refreshButton);
        
        deleteButton = new JButton("Delete Selected User");
        Theme.styleButton(deleteButton);
        deleteButton.addActionListener(new DeleteUserAction());
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadUsers() {
        var response = adminApi.listAllUsers();
        
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading users",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<UserSummary> users = response.getData();
        
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add users to table
        for (UserSummary user : users) {
            Object[] row = {
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getFullName() != null ? user.getFullName() : "-",
                user.getExtraInfo() != null ? user.getExtraInfo() : "-"
            };
            tableModel.addRow(row);
        }
    }
    
    private class DeleteUserAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = userTable.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ManageUsersPanel.this,
                    "Please select a user to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get user info from selected row
            int userId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String username = (String) tableModel.getValueAt(selectedRow, 1);
            String role = (String) tableModel.getValueAt(selectedRow, 2);
            
            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                ManageUsersPanel.this,
                "Are you sure you want to delete user '" + username + "' (" + role + ")?\n" +
                "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                var response = adminApi.deleteUser(userId);
                
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(ManageUsersPanel.this,
                        "User '" + username + "' has been deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh the table
                    loadUsers();
                } else {
                    String errorMsg = response.getMessage();
                    JOptionPane.showMessageDialog(ManageUsersPanel.this,
                        errorMsg != null ? errorMsg : "Error deleting user",
                        "Cannot Delete User",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}



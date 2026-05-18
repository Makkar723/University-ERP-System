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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.ui.theme.Theme;
import edu.univ.erp.util.EventBus;

/**
 * Panel for viewing and dropping registered sections.
 */
public class MyRegistrationsPanel extends JPanel {
    private final StudentApi studentApi;
    private final MaintenanceApi maintenanceApi;
    private JTable registrationsTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton dropButton;
    
    public MyRegistrationsPanel() {
        this.studentApi = new StudentApi();
        this.maintenanceApi = new MaintenanceApi();
        initializeUI();
        loadRegistrations();
        subscribeToMaintenanceEvents();
        updateButtonStates();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("My Registrations", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Theme.HEADING);
        add(titleLabel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Table columns
        String[] columnNames = {
            "Section ID", "Course Code", "Course Title", "Credits", 
            "Day/Time", "Room", "Semester", "Year"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        registrationsTable = new JTable(tableModel);
        registrationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        registrationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        registrationsTable.getTableHeader().setReorderingAllowed(false);
        registrationsTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        registrationsTable.setSelectionForeground(Color.BLACK);
        registrationsTable.getTableHeader().setBackground(Theme.ACCENT);
        registrationsTable.getTableHeader().setForeground(Color.WHITE);
        registrationsTable.getTableHeader().setFont(registrationsTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        JScrollPane scrollPane = new JScrollPane(registrationsTable);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadRegistrations());
        buttonPanel.add(refreshButton);
        
        dropButton = new JButton("Drop Selected Section");
        Theme.styleButton(dropButton);
        dropButton.addActionListener(new DropAction());
        buttonPanel.add(dropButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Subscribe to maintenance mode change events.
     */
    private void subscribeToMaintenanceEvents() {
        EventBus.getInstance().register("maintenance.changed", payload -> {
            SwingUtilities.invokeLater(() -> updateButtonStates());
        });
    }
    
    /**
     * Update button states based on maintenance mode.
     */
    private void updateButtonStates() {
        var response = maintenanceApi.isMaintenanceOn();
        boolean maintenanceOn = response.isSuccess() && Boolean.TRUE.equals(response.getData());
        if (dropButton != null) {
            dropButton.setEnabled(!maintenanceOn);
            if (maintenanceOn) {
                dropButton.setToolTipText("Disabled: Maintenance mode is enabled");
            } else {
                dropButton.setToolTipText(null);
            }
        }
    }
    
    private void loadRegistrations() {
        Integer currentUserId = SessionManager.getCurrentUser().getUserId();
        var response = studentApi.getRegisteredSections(currentUserId);
        
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading registrations",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<SectionRow> sections = response.getData();
        
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add sections to table
        for (SectionRow section : sections) {
            Object[] row = {
                section.getSectionId(),
                section.getCourseCode() != null ? section.getCourseCode() : "-",
                section.getCourseTitle() != null ? section.getCourseTitle() : "-",
                section.getCourseCredits() != null ? section.getCourseCredits() : 0,
                section.getDayTime() != null ? section.getDayTime() : "-",
                section.getRoom() != null ? section.getRoom() : "-",
                section.getSemester() != null ? section.getSemester() : "-",
                section.getYear() != null ? section.getYear() : 0
            };
            tableModel.addRow(row);
        }
    }
    
    private class DropAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Fail-fast check for maintenance mode
            var maintenanceResponse = maintenanceApi.isMaintenanceOn();
            if (maintenanceResponse.isSuccess() && Boolean.TRUE.equals(maintenanceResponse.getData())) {
                JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                    "Cannot change anything- Maintenance mode is enabled",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int selectedRow = registrationsTable.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                    "Please select a section to drop.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Integer sectionId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String courseCode = (String) tableModel.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(
                MyRegistrationsPanel.this,
                "Are you sure you want to drop section " + sectionId + " (" + courseCode + ")?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Integer currentUserId = SessionManager.getCurrentUser().getUserId();
                var response = studentApi.dropSection(currentUserId, sectionId);
                
                if (response.isSuccess()) {
                    JLabel successLabel = new JLabel("Successfully dropped section " + sectionId + ".");
                    successLabel.setForeground(Theme.SUCCESS_TEXT);
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                        successLabel,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh table
                    loadRegistrations();
                } else {
                    // Preserve existing error message handling
                    String errorMsg = response.getMessage();
                    if (errorMsg != null && errorMsg.contains("Maintenance mode")) {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                            errorMsg,
                            "Maintenance Mode",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                            errorMsg != null ? errorMsg : "Error dropping section",
                            "Cannot Drop",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}



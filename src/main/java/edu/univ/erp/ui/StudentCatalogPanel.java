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

import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.ui.theme.Theme;
import edu.univ.erp.util.EventBus;

/**
 * Panel for browsing course catalog and registering in sections.
 */
public class StudentCatalogPanel extends JPanel {
    private final CatalogApi catalogApi;
    private final StudentApi studentApi;
    private final MaintenanceApi maintenanceApi;
    private JTable sectionTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton registerButton;
    
    public StudentCatalogPanel() {
        this.catalogApi = new CatalogApi();
        this.studentApi = new StudentApi();
        this.maintenanceApi = new MaintenanceApi();
        initializeUI();
        loadSections();
        subscribeToMaintenanceEvents();
        updateButtonStates();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Course Catalog", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Theme.HEADING);
        add(titleLabel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Table columns
        String[] columnNames = {
            "Course Code", "Title", "Credits", "Section ID", 
            "Instructor Name", "Capacity", "Enrolled", "Seats Left", 
            "Day/Time", "Semester", "Year"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sectionTable = new JTable(tableModel);
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        sectionTable.getTableHeader().setReorderingAllowed(false);
        sectionTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        sectionTable.setSelectionForeground(Color.BLACK);
        sectionTable.getTableHeader().setBackground(Theme.ACCENT);
        sectionTable.getTableHeader().setForeground(Color.WHITE);
        sectionTable.getTableHeader().setFont(sectionTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        JScrollPane scrollPane = new JScrollPane(sectionTable);
        scrollPane.setPreferredSize(new Dimension(850, 500));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadSections());
        buttonPanel.add(refreshButton);
        
        registerButton = new JButton("Register in Selected Section");
        Theme.styleButton(registerButton);
        registerButton.addActionListener(new RegisterAction());
        buttonPanel.add(registerButton);
        
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
        if (registerButton != null) {
            registerButton.setEnabled(!maintenanceOn);
            if (maintenanceOn) {
                registerButton.setToolTipText("Disabled: Maintenance mode is enabled");
            } else {
                registerButton.setToolTipText(null);
            }
        }
    }
    
    private void loadSections() {
        var response = catalogApi.listAllSections();
        
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading sections",
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
                section.getCourseCode() != null ? section.getCourseCode() : "-",
                section.getCourseTitle() != null ? section.getCourseTitle() : "-",
                section.getCourseCredits() != null ? section.getCourseCredits() : 0,
                section.getSectionId(),
                section.getInstructorName() != null ? section.getInstructorName() : "-",
                section.getCapacity() != null ? section.getCapacity() : 0,
                section.getEnrolled() != null ? section.getEnrolled() : 0,
                section.getSeatsLeft() != null ? section.getSeatsLeft() : 0,
                section.getDayTime() != null ? section.getDayTime() : "-",
                section.getSemester() != null ? section.getSemester() : "-",
                section.getYear() != null ? section.getYear() : 0
            };
            tableModel.addRow(row);
        }
    }
    
    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Fail-fast check for maintenance mode
            var maintenanceResponse = maintenanceApi.isMaintenanceOn();
            if (maintenanceResponse.isSuccess() && Boolean.TRUE.equals(maintenanceResponse.getData())) {
                JOptionPane.showMessageDialog(StudentCatalogPanel.this,
                    "Cannot change anything- Maintenance mode is enabled",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int selectedRow = sectionTable.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(StudentCatalogPanel.this,
                    "Please select a section to register.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Integer sectionId = (Integer) tableModel.getValueAt(selectedRow, 3);
            String courseCode = (String) tableModel.getValueAt(selectedRow, 0);
            Integer seatsLeft = (Integer) tableModel.getValueAt(selectedRow, 7);
            
            if (seatsLeft <= 0) {
                JOptionPane.showMessageDialog(StudentCatalogPanel.this,
                    "This section is full. Cannot register.",
                    "Section Full",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(
                StudentCatalogPanel.this,
                "Register in section " + sectionId + " (" + courseCode + ")?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Integer currentUserId = SessionManager.getCurrentUser().getUserId();
                var response = studentApi.registerSection(currentUserId, sectionId);
                
                if (response.isSuccess()) {
                    JLabel successLabel = new JLabel("Successfully registered in section " + sectionId + ".");
                    successLabel.setForeground(Theme.SUCCESS_TEXT);
                    JOptionPane.showMessageDialog(StudentCatalogPanel.this,
                        successLabel,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh table
                    loadSections();
                } else {
                    // Preserve existing error message handling
                    String errorMsg = response.getMessage();
                    if (errorMsg != null && errorMsg.contains("Maintenance mode")) {
                        JOptionPane.showMessageDialog(StudentCatalogPanel.this,
                            errorMsg,
                            "Maintenance Mode",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(StudentCatalogPanel.this,
                            errorMsg != null ? errorMsg : "Error registering",
                            "Cannot Register",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}



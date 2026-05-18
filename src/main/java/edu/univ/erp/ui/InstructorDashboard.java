package edu.univ.erp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.ServiceRegistry;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.ui.theme.Theme;
import edu.univ.erp.util.EventBus;

/**
 * Instructor dashboard showing assigned sections and gradebook access.
 */
public class InstructorDashboard extends JFrame {
    private final UserAuth currentUser;
    private final LogoutCallback logoutCallback;
    private final InstructorApi instructorApi;
    private final CourseDao courseDao; // Still needed for table model
    private final MaintenanceService maintenanceService; // Used by MaintenanceBanner
    private JTable sectionsTable;
    private SectionsTableModel tableModel;
    private JButton openGradebookButton;
    private MaintenanceBanner maintenanceBanner;
    
    public interface LogoutCallback {
        void onLogout();
    }
    
    public InstructorDashboard(UserAuth userAuth, LogoutCallback callback) {
        this.currentUser = userAuth;
        this.logoutCallback = callback;
        this.instructorApi = new InstructorApi();
        this.courseDao = new CourseDao(); // Still needed for table model enrichment
        this.maintenanceService = ServiceRegistry.getMaintenanceService(); // Used by MaintenanceBanner
        
        // Verify role
        if (!"instructor".equals(userAuth.getRole())) {
            JOptionPane.showMessageDialog(null,
                "Access denied. Instructor role required.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Instructor role required");
        }
        
        initializeUI();
        refreshSections();
        subscribeToMaintenanceEvents();
        // Initial update of banner visibility
        if (maintenanceBanner != null) {
            maintenanceBanner.updateVisibility();
        }
    }
    
    private void initializeUI() {
        setTitle("University ERP - Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Content panel (main content)
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (Instructor)");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setForeground(Theme.HEADING);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Top right panel with logo and logout button
        JPanel topRightPanel = new JPanel(new BorderLayout(5, 0));
        
        // Load and display corner image
        try {
            java.net.URL imageURL = getClass().getResource("iiitd-corner.png");
            if (imageURL != null) {
                ImageIcon imageIcon = new ImageIcon(imageURL);
                JLabel imageLabel = new JLabel(imageIcon);
                topRightPanel.add(imageLabel, BorderLayout.EAST);
            }
        } catch (Exception e) {
            System.err.println("Could not load corner image: " + e.getMessage());
        }
        
        JButton logoutButton = new JButton("Logout");
        Theme.styleButton(logoutButton);
        logoutButton.addActionListener(e -> {
            if (logoutCallback != null) {
                logoutCallback.onLogout();
            }
        });
        topRightPanel.add(logoutButton, BorderLayout.WEST);
        headerPanel.add(topRightPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Sections panel
        JPanel sectionsPanel = new JPanel(new BorderLayout(10, 10));
        JLabel sectionsLabel = new JLabel("My Sections");
        sectionsLabel.setFont(new Font(sectionsLabel.getFont().getName(), Font.BOLD, 14));
        sectionsPanel.add(sectionsLabel, BorderLayout.NORTH);
        
        // Table
        tableModel = new SectionsTableModel();
        sectionsTable = new JTable(tableModel);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.setRowHeight(25);
        sectionsTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        sectionsTable.setSelectionForeground(Color.BLACK);
        sectionsTable.getTableHeader().setBackground(Theme.ACCENT);
        sectionsTable.getTableHeader().setForeground(Color.WHITE);
        sectionsTable.getTableHeader().setFont(sectionsTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        sectionsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        openGradebookButton = new JButton("Open Gradebook");
        Theme.styleButton(openGradebookButton);
        openGradebookButton.addActionListener(e -> openGradebook());
        buttonsPanel.add(openGradebookButton);
        
        JButton refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> refreshSections());
        buttonsPanel.add(refreshButton);
        
        sectionsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        contentPanel.add(sectionsPanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Maintenance banner (bottom of screen)
        maintenanceBanner = new MaintenanceBanner(maintenanceService);
        mainPanel.add(maintenanceBanner, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Subscribe to maintenance mode change events for real-time UI updates.
     */
    private void subscribeToMaintenanceEvents() {
        EventBus.getInstance().register("maintenance.changed", payload -> {
            SwingUtilities.invokeLater(() -> {
                if (maintenanceBanner != null) {
                    maintenanceBanner.updateVisibility();
                }
                updateButtonStates();
            });
        });
    }
    
    /**
     * Update button states based on maintenance mode.
     */
    private void updateButtonStates() {
        boolean maintenanceOn = maintenanceService.isMaintenanceOn();
        // Note: Gradebook buttons will be handled in GradebookFrame
        // This is a central place to trigger updates if needed
    }
    
    private void refreshSections() {
        var response = instructorApi.getMySectionsAsDomain();
        
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading sections",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<Section> sections = response.getData();
        tableModel.setSections(sections, courseDao);
        if (sections.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "You are not assigned to any sections.",
                "No Sections",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void openGradebook() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a section first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Section section = tableModel.getSectionAt(selectedRow);
        if (section == null) {
            return;
        }
        
        // Open gradebook frame
        GradebookFrame gradebookFrame = new GradebookFrame(section.getSectionId());
        gradebookFrame.setVisible(true);
    }
    
    public void showFrame() {
        setVisible(true);
    }
    
    public void hideFrame() {
        setVisible(false);
    }
    
    /**
     * Table model for sections.
     */
    private static class SectionsTableModel extends AbstractTableModel {
        private List<Section> sections;
        private CourseDao courseDao;
        private final String[] columnNames = {
            "Section ID", "Course Code", "Course Title", "Semester", "Year", "Day/Time", "Room"
        };
        
        public void setSections(List<Section> sections, CourseDao courseDao) {
            this.sections = sections;
            this.courseDao = courseDao;
            fireTableDataChanged();
        }
        
        public Section getSectionAt(int row) {
            if (sections == null || row < 0 || row >= sections.size()) {
                return null;
            }
            return sections.get(row);
        }
        
        @Override
        public int getRowCount() {
            return sections == null ? 0 : sections.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            if (sections == null || row >= sections.size()) {
                return null;
            }
            
            Section section = sections.get(row);
            
            switch (column) {
                case 0:
                    return section.getSectionId();
                case 1:
                    try {
                        Course course = courseDao.findById(section.getCourseId());
                        return course.getCode();
                    } catch (Exception e) {
                        return "N/A";
                    }
                case 2:
                    try {
                        Course course = courseDao.findById(section.getCourseId());
                        return course.getTitle();
                    } catch (Exception e) {
                        return "N/A";
                    }
                case 3:
                    return section.getSemester() != null ? section.getSemester() : "";
                case 4:
                    return section.getYear() != null ? section.getYear() : "";
                case 5:
                    return section.getDayTime() != null ? section.getDayTime() : "";
                case 6:
                    return section.getRoom() != null ? section.getRoom() : "";
                default:
                    return null;
            }
        }
    }
}


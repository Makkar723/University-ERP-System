package edu.univ.erp.ui;

import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.api.reports.ReportsApi;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.dao.StudentDao;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.util.EventBus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import edu.univ.erp.ui.theme.Theme;

/**
 * Student dashboard with tabs for catalog, registrations, timetable, grades, and transcript export.
 */
public class StudentDashboard extends JFrame {
    private final UserAuth currentUser;
    private final LogoutCallback logoutCallback;
    private final MaintenanceService maintenanceService; // Used by MaintenanceBanner
    private final MaintenanceApi maintenanceApi;
    private final ReportsApi reportsApi;
    private JTabbedPane tabbedPane;
    private MaintenanceBanner maintenanceBanner;
    
    public interface LogoutCallback {
        void onLogout();
    }
    
    public StudentDashboard(UserAuth userAuth, LogoutCallback callback) {
        this.currentUser = userAuth;
        this.logoutCallback = callback;
        // MaintenanceBanner still uses MaintenanceService directly
        this.maintenanceService = edu.univ.erp.service.ServiceRegistry.getMaintenanceService();
        this.maintenanceApi = new MaintenanceApi();
        this.reportsApi = new ReportsApi();
        
        // Verify role
        if (!"student".equals(userAuth.getRole())) {
            JOptionPane.showMessageDialog(null,
                "Access denied. Student role required.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Student role required");
        }
        
        initializeUI();
        subscribeToMaintenanceEvents();
        // Initial update of banner visibility
        if (maintenanceBanner != null) {
            maintenanceBanner.updateVisibility();
        }
    }
    
    private void initializeUI() {
        setTitle("University ERP - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Content panel (main content)
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Get student info for display
        final String[] displayName = {currentUser.getUsername()};
        StudentDao studentDao = new edu.univ.erp.data.dao.StudentDao();
        studentDao.findByUserId(currentUser.getUserId()).ifPresent(student -> {
            if (student.getRollNo() != null) {
                displayName[0] = student.getRollNo() + " - " + currentUser.getUsername();
            }
        });
        
        JLabel welcomeLabel = new JLabel("Welcome, " + displayName[0] + " (Student)");
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
        logoutButton.setBackground(Theme.BUTTON_BG);
        logoutButton.setForeground(Theme.BUTTON_FG);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.addActionListener(e -> {
            if (logoutCallback != null) {
                logoutCallback.onLogout();
            }
        });
        topRightPanel.add(logoutButton, BorderLayout.WEST);
        headerPanel.add(topRightPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Catalog tab
        StudentCatalogPanel catalogPanel = new StudentCatalogPanel();
        tabbedPane.addTab("Catalog", catalogPanel);
        
        // My Registrations tab
        MyRegistrationsPanel registrationsPanel = new MyRegistrationsPanel();
        tabbedPane.addTab("My Registrations", registrationsPanel);
        
        // Timetable tab
        TimetablePanel timetablePanel = new TimetablePanel();
        tabbedPane.addTab("Timetable", timetablePanel);
        
        // Grades tab
        GradesPanel gradesPanel = new GradesPanel();
        tabbedPane.addTab("Grades", gradesPanel);
        
        // Export Transcript button in a panel
        JPanel exportPanel = new JPanel(new BorderLayout());
        JLabel exportLabel = new JLabel("Export your transcript as a CSV file:");
        exportLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        exportPanel.add(exportLabel, BorderLayout.NORTH);
        
        JButton exportButton = new JButton("Export Transcript (CSV)");
        exportButton.setBackground(Theme.BUTTON_BG);
        exportButton.setForeground(Theme.BUTTON_FG);
        exportButton.setFocusPainted(false);
        exportButton.setBorderPainted(false);
        exportButton.setOpaque(true);
        exportButton.setPreferredSize(new Dimension(200, 40));
        exportButton.addActionListener(e -> exportTranscript());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(exportButton);
        exportPanel.add(buttonPanel, BorderLayout.CENTER);
        
        tabbedPane.addTab("Export Transcript", exportPanel);
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Maintenance banner (bottom of screen)
        maintenanceBanner = new MaintenanceBanner(maintenanceService);
        mainPanel.add(maintenanceBanner, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Handle window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (logoutCallback != null) {
                    logoutCallback.onLogout();
                }
            }
        });
    }
    
    private void exportTranscript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript As");
        fileChooser.setSelectedFile(new java.io.File("transcript.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            
            var response = reportsApi.exportTranscriptCsv(currentUser.getUserId(), file);
            
            if (response.isSuccess()) {
                JLabel successLabel = new JLabel("Transcript exported successfully to:\n" + file.getAbsolutePath());
                successLabel.setForeground(Theme.SUCCESS_TEXT);
                JOptionPane.showMessageDialog(this,
                    successLabel,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    response.getMessage() != null ? response.getMessage() : "Error exporting transcript",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void showFrame() {
        setVisible(true);
    }
    
    public void hideFrame() {
        setVisible(false);
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
        
        // Update buttons in all tabs
        // Note: Individual panels will handle their own button disabling
        // This is a central place to trigger updates if needed
    }
}


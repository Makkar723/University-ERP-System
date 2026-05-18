package edu.univ.erp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.ui.AdminDashboard;
import edu.univ.erp.ui.InstructorDashboard;
import edu.univ.erp.ui.LoginFrame;
import edu.univ.erp.ui.StudentDashboard;

/** Main ERP app entry */
public class Main {
    private static LoginFrame loginFrame;
    private static AdminDashboard adminDashboard;
    private static StudentDashboard studentDashboard;
    private static InstructorDashboard instructorDashboard;
    
    public static void main(String[] args) {
        // Setup look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Apply theme colors
            UIManager.put("Button.focusedBorderColor", edu.univ.erp.ui.theme.Theme.ACCENT);
            UIManager.put("Component.focusColor", edu.univ.erp.ui.theme.Theme.ACCENT);
            UIManager.put("Table.selectionBackground", edu.univ.erp.ui.theme.Theme.ACCENT_LIGHT);
            UIManager.put("TableHeader.background", edu.univ.erp.ui.theme.Theme.ACCENT);
            UIManager.put("TableHeader.foreground", java.awt.Color.WHITE);
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("[WARN] FlatLaf not available, using system default");
        }

        System.out.println("[INFO] Univ ERP app started");

        // Init UI on EDT
        SwingUtilities.invokeLater(() -> {
            initializeApplication();
        });
    }
    
    private static void initializeApplication() {
        // Create login window
        loginFrame = new LoginFrame(userAuth -> {
            // After login success
            String role = userAuth.getRole();
            if ("admin".equals(role)) {
                showAdminDashboard(userAuth);
            } else if ("student".equals(role)) {
                showStudentDashboard(userAuth);
            } else if ("instructor".equals(role)) {
                showInstructorDashboard(userAuth);
            } else {
                JOptionPane.showMessageDialog(
                    loginFrame,
                    "Unknown role: " + role,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        // Window closing hook
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        loginFrame.showFrame();
    }
    
    private static void showAdminDashboard(UserAuth userAuth) {
        loginFrame.hideFrame();
        
        if (adminDashboard != null) {
            adminDashboard.dispose();
        }
        
        adminDashboard = new AdminDashboard(userAuth, () -> {
            // Handle logout
            logout();
        });
        
        adminDashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        adminDashboard.showFrame();
    }
    
    private static void showStudentDashboard(UserAuth userAuth) {
        loginFrame.hideFrame();
        
        if (studentDashboard != null) {
            studentDashboard.dispose();
        }
        
        studentDashboard = new StudentDashboard(userAuth, () -> {
            // Handle logout
            logout();
        });
        
        studentDashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        studentDashboard.showFrame();
    }
    
    private static void showInstructorDashboard(UserAuth userAuth) {
        loginFrame.hideFrame();
        
        if (instructorDashboard != null) {
            instructorDashboard.dispose();
        }
        
        instructorDashboard = new InstructorDashboard(userAuth, () -> {
            // Handle logout
            logout();
        });
        
        instructorDashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        instructorDashboard.showFrame();
    }
    
    private static void logout() {
        // Clear user session
        SessionManager.clear();
        
        if (adminDashboard != null) {
            adminDashboard.hideFrame();
            adminDashboard.dispose();
            adminDashboard = null;
        }
        
        if (studentDashboard != null) {
            studentDashboard.hideFrame();
            studentDashboard.dispose();
            studentDashboard = null;
        }
        
        if (instructorDashboard != null) {
            instructorDashboard.hideFrame();
            instructorDashboard.dispose();
            instructorDashboard = null;
        }
        
        loginFrame.showFrame();
    }
    
    private static void shutdown() {
        // Clear user session
        SessionManager.clear();
        
        // Close DB connections
        edu.univ.erp.data.DataSourceFactory.closeAll();
        System.exit(0);
    }
}


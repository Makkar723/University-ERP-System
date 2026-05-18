package edu.univ.erp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.ui.theme.Theme;

/** Login window UI */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private LoginCallback callback;
    private final AuthApi authApi;
    
    public interface LoginCallback {
        void onLoginSuccess(UserAuth userAuth);
    }
    
    public LoginFrame(LoginCallback callback) {
        this.callback = callback;
        this.authApi = new AuthApi();
        initializeUI();
    }
    
    
    private void initializeUI() {
        setTitle("IIITD ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main padded panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Banner with logo
        JPanel bannerPanel = new JPanel(new BorderLayout());
        
        // Load scaled logo
        try {
            java.net.URL imageURL = getClass().getResource("iiitd.png");
            if (imageURL != null) {
                ImageIcon originalIcon = new ImageIcon(imageURL);
                java.awt.Image originalImage = originalIcon.getImage();
                // Scale image to 50% of original size
                java.awt.Image scaledImage = originalImage.getScaledInstance(
                    originalIcon.getIconWidth() / 2, 
                    originalIcon.getIconHeight() / 2, 
                    java.awt.Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                JLabel imageLabel = new JLabel(scaledIcon);
                imageLabel.setHorizontalAlignment(JLabel.CENTER);
                bannerPanel.add(imageLabel, BorderLayout.CENTER);
            } else {
                System.err.println("Warning: Could not find iiitd.png resource. Image URL is null.");
                System.err.println("Resource path: " + getClass().getPackage().getName().replace('.', '/') + "/iiitd.png");
            }
        } catch (Exception e) {
            System.err.println("Could not load image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Title and underline
        JPanel titleContainer = new JPanel(new BorderLayout());
        
        // Title text label
        JLabel titleLabel = new JLabel("IIITD ERP System", JLabel.CENTER);
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 18));
        titleLabel.setForeground(Theme.ACCENT);
        titleContainer.add(titleLabel, BorderLayout.CENTER);
        
        // Colored separator bar
        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.ACCENT);
        separator.setBackground(Theme.ACCENT);
        JPanel separatorPanel = new JPanel(new BorderLayout());
        separatorPanel.add(separator, BorderLayout.CENTER);
        separatorPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        titleContainer.add(separatorPanel, BorderLayout.SOUTH);
        
        bannerPanel.add(titleContainer, BorderLayout.SOUTH);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        mainPanel.add(bannerPanel, BorderLayout.NORTH);
        
        // Login form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username text field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_DARK, 2));
            }
            @Override
            public void focusLost(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        });
        formPanel.add(usernameField, gbc);
        
        // Password input field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_DARK, 2));
            }
            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        });
        formPanel.add(passwordField, gbc);
        
        // Login submit button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        loginButton = new JButton("Login");
        loginButton.setBackground(Theme.BUTTON_BG);
        loginButton.setForeground(Theme.BUTTON_FG);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        loginButton.setPreferredSize(new Dimension(0, 35));
        loginButton.addActionListener(new LoginAction());
        formPanel.add(loginButton, gbc);
        
        // Status message label
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 5, 5, 5);
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Enter key trigger
        passwordField.addActionListener(new LoginAction());
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        add(mainPanel);
    }
    
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                return;
            }
            
            loginButton.setEnabled(false);
            statusLabel.setText("Logging in...");
            statusLabel.setForeground(Color.BLUE);
            
            // Do auth in background
            SwingUtilities.invokeLater(() -> {
                var response = authApi.login(username, password);
                
                if (response.isSuccess()) {
                    // Session already set
                    UserAuth userAuth = SessionManager.getCurrentUser();
                    
                    statusLabel.setText("Login successful!");
                    statusLabel.setForeground(Theme.SUCCESS_TEXT);
                    
                    // Clear input fields
                    usernameField.setText("");
                    passwordField.setText("");
                    
                    // Notify success callback
                    if (callback != null && userAuth != null) {
                        callback.onLoginSuccess(userAuth);
                    }
                } else {
                    // Keep error handling
                    String errorMsg = response.getMessage();
                    if (errorMsg != null && errorMsg.contains("Incorrect username or password")) {
                        statusLabel.setText("Incorrect username or password");
                    } else if (errorMsg != null) {
                        statusLabel.setText(errorMsg);
                    } else {
                        statusLabel.setText("Login error");
                    }
                    statusLabel.setForeground(Color.RED);
                    passwordField.setText("");
                    passwordField.requestFocus();
                }
                
                loginButton.setEnabled(true);
            });
        }
    }
    
    public void showFrame() {
        setVisible(true);
        usernameField.requestFocus();
    }
    
    public void hideFrame() {
        setVisible(false);
    }
}


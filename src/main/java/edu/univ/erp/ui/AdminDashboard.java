package edu.univ.erp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.InstructorDao;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.SectionMeeting;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.ServiceRegistry;
import edu.univ.erp.ui.admin.AdminCoursesPanel;
import edu.univ.erp.ui.admin.AdminSectionsPanel; // Used by MaintenanceBanner
import edu.univ.erp.ui.theme.Theme;
import edu.univ.erp.util.Constants;
import edu.univ.erp.util.EventBus;
import edu.univ.erp.util.SectionScheduleUtil;
import edu.univ.erp.util.Validators;

public class AdminDashboard extends JFrame {
    private final UserAuth currentUser;
    private JButton addInstructorButton;
    private JButton addStudentButton;
    private JButton createCourseButton;
    private JButton createSectionButton;
    private JButton assignInstructorButton;
    private JCheckBox maintenanceCheckBox;
    private JLabel maintenanceStatusLabel;
    private JButton logoutButton;
    private JTextArea statusArea;
    private final LogoutCallback logoutCallback;
    private final AdminApi adminApi;
    private final CatalogApi catalogApi;
    private final MaintenanceApi maintenanceApi;
    private final MaintenanceService maintenanceService;
    
    public interface LogoutCallback {
        void onLogout();
    }
    
    public AdminDashboard(UserAuth userAuth, LogoutCallback callback) {
        this.currentUser = userAuth;
        this.logoutCallback = callback;
        this.adminApi = new AdminApi();
        this.catalogApi = new CatalogApi();
        this.maintenanceApi = new MaintenanceApi();
        this.maintenanceService = ServiceRegistry.getMaintenanceService();
        initializeUI();
        updateMaintenanceStatus();
    }
    
    private void initializeUI() {
        setTitle("University ERP - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (Admin)");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setForeground(Theme.HEADING);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JPanel topRightPanel = new JPanel(new BorderLayout(5, 0));
        
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
        
        logoutButton = new JButton("Logout");
        Theme.styleButton(logoutButton);
        logoutButton.addActionListener(e -> {
            if (logoutCallback != null) {
                logoutCallback.onLogout();
            }
        });
        topRightPanel.add(logoutButton, BorderLayout.WEST);
        headerPanel.add(topRightPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel userMgmtLabel = new JLabel("User Management");
        userMgmtLabel.setFont(new Font(userMgmtLabel.getFont().getName(), Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 3;
        contentPanel.add(userMgmtLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.33;
        
        addInstructorButton = new JButton("Add Instructor");
        Theme.styleButton(addInstructorButton);
        addInstructorButton.setPreferredSize(new Dimension(0, 40));
        addInstructorButton.addActionListener(e -> showAddUserDialog("instructor"));
        contentPanel.add(addInstructorButton, gbc);
        
        gbc.gridx = 1;
        addStudentButton = new JButton("Add Student");
        Theme.styleButton(addStudentButton);
        addStudentButton.setPreferredSize(new Dimension(0, 40));
        addStudentButton.addActionListener(e -> showAddUserDialog("student"));
        contentPanel.add(addStudentButton, gbc);
        
        gbc.gridx = 2;
        JButton manageUsersButton = new JButton("Manage Users");
        Theme.styleButton(manageUsersButton);
        manageUsersButton.setPreferredSize(new Dimension(0, 40));
        manageUsersButton.addActionListener(e -> showManageUsersDialog());
        contentPanel.add(manageUsersButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 5, 5, 5);
        JLabel courseMgmtLabel = new JLabel("Course & Section Management");
        courseMgmtLabel.setFont(new Font(courseMgmtLabel.getFont().getName(), Font.BOLD, 14));
        contentPanel.add(courseMgmtLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 0.33;
        
        createCourseButton = new JButton("Create Course");
        Theme.styleButton(createCourseButton);
        createCourseButton.setPreferredSize(new Dimension(0, 40));
        createCourseButton.addActionListener(e -> showCreateCourseDialog());
        contentPanel.add(createCourseButton, gbc);
        
        gbc.gridx = 1;
        createSectionButton = new JButton("Create Section");
        Theme.styleButton(createSectionButton);
        createSectionButton.setPreferredSize(new Dimension(0, 40));
        createSectionButton.addActionListener(e -> showCreateSectionDialog());
        contentPanel.add(createSectionButton, gbc);
        
        gbc.gridx = 2;
        assignInstructorButton = new JButton("Assign Instructor");
        Theme.styleButton(assignInstructorButton);
        assignInstructorButton.setPreferredSize(new Dimension(0, 40));
        assignInstructorButton.addActionListener(e -> showAssignInstructorDialog());
        contentPanel.add(assignInstructorButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 1;
        JButton manageCoursesButton = new JButton("Manage Courses");
        Theme.styleButton(manageCoursesButton);
        manageCoursesButton.setPreferredSize(new Dimension(0, 40));
        manageCoursesButton.addActionListener(e -> showCoursesPanel());
        contentPanel.add(manageCoursesButton, gbc);
        
        gbc.gridx = 1;
        JButton manageSectionsButton = new JButton("Manage Sections");
        Theme.styleButton(manageSectionsButton);
        manageSectionsButton.setPreferredSize(new Dimension(0, 40));
        manageSectionsButton.addActionListener(e -> showSectionsPanel());
        contentPanel.add(manageSectionsButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        JPanel maintenancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        maintenancePanel.add(new JLabel("Maintenance Mode:"));
        
        maintenanceCheckBox = new JCheckBox();
        maintenanceCheckBox.setSelected(false);
        maintenanceCheckBox.addActionListener(e -> toggleMaintenance());
        maintenancePanel.add(maintenanceCheckBox);
        
        maintenanceStatusLabel = new JLabel("OFF");
        maintenanceStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        maintenancePanel.add(maintenanceStatusLabel);
        
        contentPanel.add(maintenancePanel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        statusArea = new JTextArea(10, 30);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusArea.setText("Status messages will appear here...\n");
        JScrollPane scrollPane = new JScrollPane(statusArea);
        contentPanel.add(scrollPane, gbc);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void showAddUserDialog(String role) {
        JDialog dialog = new JDialog(this, "Add " + capitalize(role), true);
        dialog.setSize(450, role.equals("student") ? 360 : 320);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField usernameField = new JTextField(20);
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        final JTextField rollNoField;
        final JComboBox<String> programCombo;
        final JSpinner yearSpinner;
        
        if (role.equals("student")) {
            gbc.gridx = 0; gbc.gridy = row++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            panel.add(new JLabel("Roll Number:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            rollNoField = new JTextField(20);
            panel.add(rollNoField, gbc);
            
            gbc.gridx = 0; gbc.gridy = row++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            panel.add(new JLabel("Program:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            programCombo = new JComboBox<>(sortedArray(Constants.STUDENT_BRANCHES));
            programCombo.setEditable(false);
            panel.add(programCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = row++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            panel.add(new JLabel("Year (1-4):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
            panel.add(yearSpinner, gbc);
        } else {
            programCombo = null;
            // Instructor - department field
            rollNoField = null;
            yearSpinner = null;
            
            gbc.gridx = 0; gbc.gridy = row++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            panel.add(new JLabel("Department:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            JComboBox<String> departmentCombo = new JComboBox<>(sortedArray(Constants.INSTRUCTOR_DEPTS));
            departmentCombo.setEditable(false);
            panel.add(departmentCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = row++;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(15, 5, 5, 5);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton createButton = new JButton("Create");
            Theme.styleButton(createButton);
            JButton cancelButton = new JButton("Cancel");
            Theme.styleButton(cancelButton);
            
            createButton.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String department = (String) departmentCombo.getSelectedItem();
                
                if (username.isEmpty() || password.isEmpty() || department == null || department.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Please fill all fields", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Validators.validateInstructorDept(department);
                var response = adminApi.createInstructorUser(username, password, department);
                if (response.isSuccess()) {
                    appendStatus("Instructor '" + username + "' created successfully!");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        response.getMessage() != null ? response.getMessage() : "Error creating instructor", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(createButton);
            buttonPanel.add(cancelButton);
            panel.add(buttonPanel, gbc);
            
            dialog.add(panel);
            dialog.setVisible(true);
            return;
        }
        
        // Buttons for student
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String rollNo = rollNoField.getText().trim();
            String program = programCombo != null ? (String) programCombo.getSelectedItem() : null;
            int year = (Integer) yearSpinner.getValue();
            
            if (username.isEmpty() || password.isEmpty() || program == null || program.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Username, password, and program are required", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Validators.validateStudentBranch(program);
            var response = adminApi.createStudentUser(username, password, 
                rollNo.isEmpty() ? null : rollNo, 
                program, 
                year);
            if (response.isSuccess()) {
                appendStatus("Student '" + username + "' created successfully!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    response.getMessage() != null ? response.getMessage() : "Error creating student", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showCreateCourseDialog() {
        JDialog dialog = new JDialog(this, "Create Course", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField codeField = new JTextField(20);
        panel.add(codeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField titleField = new JTextField(20);
        panel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        panel.add(creditsSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            int credits = (Integer) creditsSpinner.getValue();
            
            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Course code and title are required", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Validators.validateCourseCode(code);
            var response = adminApi.createCourse(code, title, credits);
            if (response.isSuccess() && response.getData() != null) {
                int courseId = response.getData().getCourseId();
                appendStatus("Course '" + code + "' (ID: " + courseId + ") created successfully!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    response.getMessage() != null ? response.getMessage() : "Error creating course", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showCreateSectionDialog() {
        JDialog dialog = new JDialog(this, "Create Section", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Course dropdown
        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> courseComboBox = new JComboBox<>();
        courseComboBox.addItem(new ComboBoxItem("-- Select Course --", null));
        
        // Populate courses via API
        var coursesResponse = catalogApi.listCourses();
        if (coursesResponse.isSuccess() && coursesResponse.getData() != null) {
            List<edu.univ.erp.api.types.CourseRow> courses = coursesResponse.getData();
            for (edu.univ.erp.api.types.CourseRow course : courses) {
                String displayText = course.getCode() + " - " + 
                    (course.getTitle() != null ? course.getTitle() : "");
                courseComboBox.addItem(new ComboBoxItem(displayText, course.getCourseId()));
            }
        } else {
            JOptionPane.showMessageDialog(dialog,
                coursesResponse.getMessage() != null ? coursesResponse.getMessage() : "Error loading courses",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        panel.add(courseComboBox, gbc);
        
        // Instructor dropdown (optional)
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Instructor (optional):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> instructorComboBox = new JComboBox<>();
        instructorComboBox.addItem(new ComboBoxItem("-- No Instructor --", null));
        
        // Populate instructors
        try {
            InstructorDao instructorDao = new InstructorDao();
            AuthDao authDao = new AuthDao();
            List<Instructor> instructors = instructorDao.listAll();
            for (Instructor instructor : instructors) {
                try {
                    UserAuth user = authDao.findById(instructor.getUserId());
                    String displayText = user.getUsername() + 
                        (instructor.getDepartment() != null ? " (" + instructor.getDepartment() + ")" : "");
                    instructorComboBox.addItem(new ComboBoxItem(displayText, instructor.getUserId()));
                } catch (Exception e) {
                    // Skip if user not found
                    System.err.println("Instructor user not found: " + instructor.getUserId());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Error loading instructors: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        panel.add(instructorComboBox, gbc);
        
        // Meetings editor
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Schedule:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        
        // Meetings table model
        List<SectionMeeting> meetings = new ArrayList<>();
        MeetingsTableModel meetingsModel = new MeetingsTableModel(meetings);
        JTable meetingsTable = new JTable(meetingsModel);
        meetingsTable.setFillsViewportHeight(true);
        JScrollPane meetingsScrollPane = new JScrollPane(meetingsTable);
        meetingsScrollPane.setPreferredSize(new Dimension(400, 150));
        panel.add(meetingsScrollPane, gbc);
        
        // Add/Remove meeting buttons
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        JPanel meetingButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMeetingButton = new JButton("Add Meeting");
        Theme.styleButton(addMeetingButton);
        JButton removeMeetingButton = new JButton("Remove Selected");
        Theme.styleButton(removeMeetingButton);
        meetingButtonsPanel.add(addMeetingButton);
        meetingButtonsPanel.add(removeMeetingButton);
        panel.add(meetingButtonsPanel, gbc);
        
        // Add meeting dialog components (inline)
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel addMeetingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addMeetingPanel.setBorder(BorderFactory.createTitledBorder("Add New Meeting"));
        
        JComboBox<DayOfWeek> dayCombo = new JComboBox<>(new DayOfWeek[]{
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        });
        dayCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DayOfWeek) {
                    DayOfWeek day = (DayOfWeek) value;
                    String[] names = {"Mon", "Tue", "Wed", "Thu", "Fri"};
                    setText(names[day.getValue() - 1]);
                }
                return this;
            }
        });
        
        JSpinner startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
        JSpinner startMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 30));
        JSpinner endHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
        JSpinner endMinSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 30, 30));
        
        addMeetingPanel.add(new JLabel("Day:"));
        addMeetingPanel.add(dayCombo);
        addMeetingPanel.add(new JLabel("Start:"));
        addMeetingPanel.add(startHourSpinner);
        addMeetingPanel.add(new JLabel(":"));
        addMeetingPanel.add(startMinSpinner);
        addMeetingPanel.add(new JLabel("End:"));
        addMeetingPanel.add(endHourSpinner);
        addMeetingPanel.add(new JLabel(":"));
        addMeetingPanel.add(endMinSpinner);
        panel.add(addMeetingPanel, gbc);
        
        // Room
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField roomField = new JTextField(20);
        panel.add(roomField, gbc);
        
        // Capacity
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
        panel.add(capacitySpinner, gbc);
        
        // Semester
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField semesterField = new JTextField(20);
        panel.add(semesterField, gbc);
        
        // Year
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2024, 2020, 2030, 1));
        panel.add(yearSpinner, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        // Add meeting button action
        addMeetingButton.addActionListener(e -> {
            try {
                DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
                int startHour = (Integer) startHourSpinner.getValue();
                int startMin = (Integer) startMinSpinner.getValue();
                int endHour = (Integer) endHourSpinner.getValue();
                int endMin = (Integer) endMinSpinner.getValue();
                
                LocalTime start = LocalTime.of(startHour, startMin);
                LocalTime end = LocalTime.of(endHour, endMin);
                
                // Validate
                if (!end.isAfter(start)) {
                    JOptionPane.showMessageDialog(dialog,
                        "End time must be after start time",
                        "Invalid Time",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                SectionMeeting meeting = new SectionMeeting(day, start, end);
                meetings.add(meeting);
                meetingsModel.fireTableDataChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error adding meeting: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Remove meeting button action
        removeMeetingButton.addActionListener(e -> {
            int selectedRow = meetingsTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < meetings.size()) {
                meetings.remove(selectedRow);
                meetingsModel.fireTableDataChanged();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Please select a meeting to remove",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        createButton.addActionListener(e -> {
            // Get Course ID from dropdown
            ComboBoxItem selectedCourse = (ComboBoxItem) courseComboBox.getSelectedItem();
            if (selectedCourse == null || selectedCourse.getId() == null) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a course.",
                    "No Course Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int courseId = selectedCourse.getId();
            
            // Get Instructor User ID from dropdown (optional)
            ComboBoxItem selectedInstructor = (ComboBoxItem) instructorComboBox.getSelectedItem();
            Integer instructorId = null;
            if (selectedInstructor != null && selectedInstructor.getId() != null) {
                instructorId = selectedInstructor.getId();
            }
            
            // Validate meetings
            if (meetings.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "At least one meeting is required",
                    "No Meetings",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Format schedule string
            String dayTime = SectionScheduleUtil.formatSchedule(meetings);
            
            String room = roomField.getText().trim();
            int capacity = (Integer) capacitySpinner.getValue();
            String semester = semesterField.getText().trim();
            int year = (Integer) yearSpinner.getValue();
            
            if (room.isEmpty() || semester.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Room and semester are required", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            var response = adminApi.createSection(courseId, instructorId, dayTime, room, capacity, semester, year);
            if (response.isSuccess() && response.getData() != null) {
                int sectionId = response.getData().getSectionId();
                appendStatus("Section (ID: " + sectionId + ") created successfully!");
                dialog.dispose();
            } else {
                String errorMsg = response.getMessage();
                if (errorMsg != null && errorMsg.contains("Invalid schedule")) {
                    JOptionPane.showMessageDialog(dialog,
                        errorMsg,
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        errorMsg != null ? errorMsg : "Error creating section", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Table model for section meetings.
     */
    private static class MeetingsTableModel extends AbstractTableModel {
        private final List<SectionMeeting> meetings;
        private final String[] columnNames = {"Day", "Start Time", "End Time"};
        
        public MeetingsTableModel(List<SectionMeeting> meetings) {
            this.meetings = meetings;
        }
        
        @Override
        public int getRowCount() {
            return meetings.size();
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            SectionMeeting meeting = meetings.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                    return dayNames[meeting.getDay().getValue() - 1];
                case 1:
                    return String.format("%02d:%02d", meeting.getStart().getHour(), meeting.getStart().getMinute());
                case 2:
                    return String.format("%02d:%02d", meeting.getEnd().getHour(), meeting.getEnd().getMinute());
                default:
                    return null;
            }
        }
    }
    
    private void showAssignInstructorDialog() {
        JDialog dialog = new JDialog(this, "Assign Instructor to Section", true);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Section dropdown
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Section:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> sectionComboBox = new JComboBox<>();
        sectionComboBox.addItem(new ComboBoxItem("-- Select Section --", null));
        
        // Populate sections via API
        var sectionsResponse = adminApi.listAllSections();
        var coursesResponse = catalogApi.listCourses();
        if (sectionsResponse.isSuccess() && coursesResponse.isSuccess()) {
            List<edu.univ.erp.api.types.SectionRow> sections = sectionsResponse.getData();
            List<edu.univ.erp.api.types.CourseRow> courses = coursesResponse.getData();
            // Build course map for lookup
            java.util.Map<Integer, edu.univ.erp.api.types.CourseRow> courseMap = new java.util.HashMap<>();
            for (edu.univ.erp.api.types.CourseRow course : courses) {
                courseMap.put(course.getCourseId(), course);
            }
            // Add sections
            for (edu.univ.erp.api.types.SectionRow section : sections) {
                edu.univ.erp.api.types.CourseRow course = courseMap.get(section.getCourseId());
                String displayText = "Section " + section.getSectionId() + " - " + 
                    (course != null ? course.getCode() : "?") + " (" + 
                    (course != null && course.getTitle() != null ? course.getTitle() : "?") + ")";
                sectionComboBox.addItem(new ComboBoxItem(displayText, section.getSectionId()));
            }
        } else {
            JOptionPane.showMessageDialog(dialog,
                sectionsResponse.getMessage() != null ? sectionsResponse.getMessage() : "Error loading sections",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        panel.add(sectionComboBox, gbc);
        
        // Instructor dropdown
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> instructorComboBox = new JComboBox<>();
        instructorComboBox.addItem(new ComboBoxItem("-- Select Instructor --", null));
        
        // Populate instructors via API
        var usersResponse2 = adminApi.listAllUsers();
        if (usersResponse2.isSuccess() && usersResponse2.getData() != null) {
            List<edu.univ.erp.domain.UserSummary> users = usersResponse2.getData();
            for (edu.univ.erp.domain.UserSummary user : users) {
                if ("instructor".equals(user.getRole())) {
                    String displayText = user.getUsername();
                    if (user.getExtraInfo() != null && user.getExtraInfo().contains("Dept:")) {
                        displayText += " (" + user.getExtraInfo().replace("Dept: ", "") + ")";
                    }
                    instructorComboBox.addItem(new ComboBoxItem(displayText, user.getUserId()));
                }
            }
        } else {
            JOptionPane.showMessageDialog(dialog,
                usersResponse2.getMessage() != null ? usersResponse2.getMessage() : "Error loading instructors",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        panel.add(instructorComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton assignButton = new JButton("Assign");
        Theme.styleButton(assignButton);
        JButton cancelButton = new JButton("Cancel");
        Theme.styleButton(cancelButton);
        
        assignButton.addActionListener(e -> {
            // Get Section ID from dropdown
            ComboBoxItem selectedSection = (ComboBoxItem) sectionComboBox.getSelectedItem();
            if (selectedSection == null || selectedSection.getId() == null) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a section.",
                    "No Section Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int sectionId = selectedSection.getId();
            
            // Get Instructor User ID from dropdown
            ComboBoxItem selectedInstructor = (ComboBoxItem) instructorComboBox.getSelectedItem();
            if (selectedInstructor == null || selectedInstructor.getId() == null) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select an instructor.",
                    "No Instructor Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int instructorId = selectedInstructor.getId();
            
            var response = adminApi.assignInstructor(sectionId, instructorId);
            if (response.isSuccess()) {
                appendStatus("Instructor '" + selectedInstructor.getDisplayText() + 
                    "' assigned to section " + sectionId);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    response.getMessage() != null ? response.getMessage() : "Error assigning instructor", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void toggleMaintenance() {
        boolean newState = maintenanceCheckBox.isSelected();
        var currentResponse = maintenanceApi.isMaintenanceOn();
        boolean currentState = currentResponse.isSuccess() && Boolean.TRUE.equals(currentResponse.getData());
        
        // If turning ON, show confirmation dialog
        if (newState && !currentState) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to turn Maintenance Mode ON? While ON, students and instructors will be read-only.",
                "Confirm Maintenance Mode",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                // User cancelled, revert checkbox
                maintenanceCheckBox.setSelected(false);
                return;
            }
        }
        
        // Set maintenance mode via API
        var response = maintenanceApi.setMaintenance(newState);
        
        if (response.isSuccess()) {
            // Post event to EventBus for real-time UI updates
            EventBus.getInstance().post("maintenance.changed", Boolean.valueOf(newState));
            
            updateMaintenanceStatus();
            
            // Show success message
            String message = "Maintenance mode set to " + (newState ? "ON" : "OFF") + " by " + currentUser.getUsername();
            appendStatus(message);
                                            
            JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                response.getMessage() != null ? response.getMessage() : "Error updating maintenance mode", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            maintenanceCheckBox.setSelected(!maintenanceCheckBox.isSelected());
        }
    }
    
    private void updateMaintenanceStatus() {
        var response = maintenanceApi.isMaintenanceOn();
        if (response.isSuccess()) {
            boolean isOn = Boolean.TRUE.equals(response.getData());
            maintenanceCheckBox.setSelected(isOn);
            maintenanceStatusLabel.setText(isOn ? "ON" : "OFF");
            maintenanceStatusLabel.setForeground(isOn ? new Color(0xB00020) : Color.BLACK);
        } else {
            maintenanceStatusLabel.setText("Error");
            maintenanceStatusLabel.setForeground(Color.RED);
        }
    }
    
    private void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }
    
    private void showManageUsersDialog() {
        JDialog dialog = new JDialog(this, "Manage Users", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        ManageUsersPanel manageUsersPanel = new ManageUsersPanel();
        dialog.add(manageUsersPanel);
        
        dialog.setVisible(true);
    }

    private void showCoursesPanel() {
        JFrame frame = new JFrame("Course Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(this);
        
        AdminCoursesPanel coursesPanel = new AdminCoursesPanel(adminApi);
        frame.add(coursesPanel);
        frame.setVisible(true);
    }
    
    private void showSectionsPanel() {
        JFrame frame = new JFrame("Section Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLocationRelativeTo(this);
        
        AdminSectionsPanel sectionsPanel = new AdminSectionsPanel(adminApi);
        frame.add(sectionsPanel);
        frame.setVisible(true);
    }

    private String[] sortedArray(Set<String> values) {
        return values.stream()
            .sorted()
            .toArray(String[]::new);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public void showFrame() {
        setVisible(true);
    }
    
    public void hideFrame() {
        setVisible(false);
    }
}

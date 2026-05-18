package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.data.dao.InstructorDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.SectionMeeting;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.ui.theme.Theme;
import edu.univ.erp.util.SectionScheduleUtil;

public class AdminSectionsPanel extends JPanel {
    private final AdminApi adminApi;
    private final CatalogApi catalogApi;
    private final SectionDao sectionDao;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final InstructorDao instructorDao;
    private final AuthDao authDao;
    private JTable sectionsTable;
    private SectionsTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignInstructorButton;
    private JButton refreshButton;
    
    public AdminSectionsPanel(AdminApi adminApi) {
        this.adminApi = adminApi;
        this.catalogApi = new CatalogApi();
        this.sectionDao = new SectionDao();
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.instructorDao = new InstructorDao();
        this.authDao = new AuthDao();
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Section Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Theme.HEADING);
        add(titleLabel, BorderLayout.NORTH);
        
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
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addButton = new JButton("Add Section");
        Theme.styleButton(addButton);
        addButton.addActionListener(e -> showSectionDialog(null));
        buttonPanel.add(addButton);
        
        editButton = new JButton("Edit Section");
        Theme.styleButton(editButton);
        editButton.addActionListener(e -> {
            int selectedRow = sectionsTable.getSelectedRow();
            if (selectedRow >= 0) {
                Section section = tableModel.getSectionAt(selectedRow);
                showSectionDialog(section);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a section to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(editButton);
        
        deleteButton = new JButton("Delete Section");
        Theme.styleButton(deleteButton);
        deleteButton.addActionListener(e -> deleteSelectedSection());
        buttonPanel.add(deleteButton);
        
        assignInstructorButton = new JButton("Assign Instructor");
        Theme.styleButton(assignInstructorButton);
        assignInstructorButton.addActionListener(e -> showAssignInstructorDialog());
        buttonPanel.add(assignInstructorButton);
        
        refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadData());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        var response = adminApi.listAllSections();
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading sections",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<edu.univ.erp.api.types.SectionRow> sectionRows = response.getData();
        List<Section> sections = new ArrayList<>();
        for (edu.univ.erp.api.types.SectionRow row : sectionRows) {
            try {
                Section section = sectionDao.findById(row.getSectionId());
                sections.add(section);
            } catch (Exception e) {
            }
        }
        tableModel.setSections(sections);
    }
    
    private void showSectionDialog(Section section) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            section == null ? "Create Section" : "Edit Section", true);
        dialog.setSize(800, 650);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row++;
        panel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> courseComboBox = new JComboBox<>();
        courseComboBox.addItem(new ComboBoxItem("-- Select Course --", null));
        
        var coursesResponse = catalogApi.listCourses();
        if (coursesResponse.isSuccess() && coursesResponse.getData() != null) {
            List<edu.univ.erp.api.types.CourseRow> courses = coursesResponse.getData();
            for (edu.univ.erp.api.types.CourseRow course : courses) {
                String displayText = course.getCode() + " - " + course.getTitle();
                ComboBoxItem item = new ComboBoxItem(displayText, course.getCourseId());
                courseComboBox.addItem(item);
                if (section != null && section.getCourseId().equals(course.getCourseId())) {
                    courseComboBox.setSelectedItem(item);
                }
            }
        } else {
            JOptionPane.showMessageDialog(dialog,
                coursesResponse.getMessage() != null ? coursesResponse.getMessage() : "Error loading courses",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        panel.add(courseComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Instructor (optional):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> instructorComboBox = new JComboBox<>();
        instructorComboBox.addItem(new ComboBoxItem("-- No Instructor --", null));
        
        var usersResponse = adminApi.listAllUsers();
        if (usersResponse.isSuccess() && usersResponse.getData() != null) {
            List<edu.univ.erp.domain.UserSummary> users = usersResponse.getData();
            for (edu.univ.erp.domain.UserSummary user : users) {
                if ("instructor".equals(user.getRole())) {
                    String displayText = user.getUsername();
                    if (user.getExtraInfo() != null && user.getExtraInfo().contains("Dept:")) {
                        displayText += " (" + user.getExtraInfo().replace("Dept: ", "") + ")";
                    }
                    ComboBoxItem item = new ComboBoxItem(displayText, user.getUserId());
                    instructorComboBox.addItem(item);
                    if (section != null && section.getInstructorId() != null &&
                        section.getInstructorId().equals(user.getUserId())) {
                        instructorComboBox.setSelectedItem(item);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(dialog,
                usersResponse.getMessage() != null ? usersResponse.getMessage() : "Error loading instructors",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        panel.add(instructorComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Schedule:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        
        List<SectionMeeting> meetings = new ArrayList<>();
        if (section != null) {
            meetings.addAll(section.getMeetings());
        }
        MeetingsTableModel meetingsModel = new MeetingsTableModel(meetings);
        JTable meetingsTable = new JTable(meetingsModel);
        meetingsTable.setFillsViewportHeight(true);
        JScrollPane meetingsScrollPane = new JScrollPane(meetingsTable);
        meetingsScrollPane.setPreferredSize(new Dimension(400, 150));
        panel.add(meetingsScrollPane, gbc);
        
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
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
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
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField roomField = new JTextField(20);
        if (section != null) {
            roomField.setText(section.getRoom());
        }
        panel.add(roomField, gbc);
        
        // Capacity
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(
            section != null ? section.getCapacity() : 30, 0, 200, 1));
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
        if (section != null) {
            semesterField.setText(section.getSemester());
        }
        panel.add(semesterField, gbc);
        
        // Year
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(
            section != null ? section.getYear() : 2024, 2000, 2100, 1));
        panel.add(yearSpinner, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton(section == null ? "Create" : "Save");
        Theme.styleButton(saveButton);
        JButton cancelButton = new JButton("Cancel");
        Theme.styleButton(cancelButton);
        
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
        
        saveButton.addActionListener(e -> {
            ComboBoxItem selectedCourse = (ComboBoxItem) courseComboBox.getSelectedItem();
                if (selectedCourse == null || selectedCourse.getId() == null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please select a course.",
                        "No Course Selected",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int courseId = selectedCourse.getId();
                
                ComboBoxItem selectedInstructor = (ComboBoxItem) instructorComboBox.getSelectedItem();
                Integer instructorId = null;
                if (selectedInstructor != null && selectedInstructor.getId() != null) {
                    instructorId = selectedInstructor.getId();
                }
                
                if (meetings.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "At least one meeting is required",
                        "No Meetings",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
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
                
                if (section == null) {
                    var createResponse = adminApi.createSection(courseId, instructorId, dayTime, room, capacity, semester, year);
                    if (createResponse.isSuccess()) {
                        JOptionPane.showMessageDialog(dialog,
                            "Section created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadData();
                    } else {
                        String errorMsg = createResponse.getMessage();
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
                } else {
                    var updateResponse = adminApi.updateSection(section.getSectionId(), courseId, instructorId,
                        dayTime, room, capacity, semester, year);
                    if (updateResponse.isSuccess()) {
                        JOptionPane.showMessageDialog(dialog,
                            "Section updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadData();
                    } else {
                        String errorMsg = updateResponse.getMessage();
                        if (errorMsg != null && errorMsg.contains("Invalid schedule")) {
                            JOptionPane.showMessageDialog(dialog,
                                errorMsg,
                                "Validation Error",
                                JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                errorMsg != null ? errorMsg : "Error updating section",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteSelectedSection() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a section to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Section section = tableModel.getSectionAt(selectedRow);
        
        // Check for enrolled students
        int enrollmentCount = enrollmentDao.countBySection(section.getSectionId());
        if (enrollmentCount > 0) {
            String message = String.format(
                "Section has enrolled students (%d); cannot delete.\n\n" +
                "Drop students first or use force delete (cascade).\n\n" +
                "Force delete will also delete:\n" +
                "- All enrollments in this section\n" +
                "- All grades for those enrollments\n\n" +
                "This action cannot be undone!",
                enrollmentCount);
            
            int option = JOptionPane.showConfirmDialog(this,
                message,
                "Cannot Delete Section",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                // Show force delete confirmation
                JTextField confirmField = new JTextField(20);
                Object[] message2 = {
                    "Type 'DELETE' to confirm force delete:",
                    confirmField
                };
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    message2,
                    "Confirm Force Delete",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.OK_OPTION &&
                    "DELETE".equals(confirmField.getText().trim())) {
                    var response = adminApi.deleteSection(section.getSectionId(), true);
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                            "Section deleted successfully (cascade).",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            response.getMessage() != null ? response.getMessage() : "Error deleting section",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            return;
        }
        
        // No enrollments - safe to delete
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete section (ID: " + section.getSectionId() + ")?\nThis cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            var response = adminApi.deleteSection(section.getSectionId(), false);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                    "Section deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                    response.getMessage() != null ? response.getMessage() : "Error deleting section",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAssignInstructorDialog() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a section to assign an instructor.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Section section = tableModel.getSectionAt(selectedRow);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Assign Instructor", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Section ID: " + section.getSectionId()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<ComboBoxItem> instructorComboBox = new JComboBox<>();
        instructorComboBox.addItem(new ComboBoxItem("-- No Instructor --", null));
        
        var usersResponse2 = adminApi.listAllUsers();
        if (usersResponse2.isSuccess() && usersResponse2.getData() != null) {
            List<edu.univ.erp.domain.UserSummary> users = usersResponse2.getData();
            for (edu.univ.erp.domain.UserSummary user : users) {
                if ("instructor".equals(user.getRole())) {
                    String displayText = user.getUsername();
                    if (user.getExtraInfo() != null && user.getExtraInfo().contains("Dept:")) {
                        displayText += " (" + user.getExtraInfo().replace("Dept: ", "") + ")";
                    }
                    ComboBoxItem item = new ComboBoxItem(displayText, user.getUserId());
                    instructorComboBox.addItem(item);
                    if (section.getInstructorId() != null &&
                        section.getInstructorId().equals(user.getUserId())) {
                        instructorComboBox.setSelectedItem(item);
                    }
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
        JButton saveButton = new JButton("Assign");
        Theme.styleButton(saveButton);
        JButton cancelButton = new JButton("Cancel");
        Theme.styleButton(cancelButton);
        
        saveButton.addActionListener(e -> {
            ComboBoxItem selected = (ComboBoxItem) instructorComboBox.getSelectedItem();
            Integer instructorId = (selected != null && selected.getId() != null) ? selected.getId() : null;
            
            if (instructorId != null) {
                var response = adminApi.assignInstructor(section.getSectionId(), instructorId);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Instructor assignment updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        response.getMessage() != null ? response.getMessage() : "Error assigning instructor",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                var response = adminApi.updateSection(section.getSectionId(), section.getCourseId(), null,
                    section.getDayTime(), section.getRoom(), section.getCapacity(),
                    section.getSemester(), section.getYear());
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Instructor assignment updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        response.getMessage() != null ? response.getMessage() : "Error updating section",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private class SectionsTableModel extends AbstractTableModel {
        private List<Section> sections;
        private final String[] columnNames = {"Section ID", "Course Code", "Course Title", "Instructor",
            "Day/Time", "Room", "Capacity", "Enrolled", "Semester", "Year"};
        
        public void setSections(List<Section> sections) {
            this.sections = sections;
            fireTableDataChanged();
        }
        
        public Section getSectionAt(int row) {
            return sections.get(row);
        }
        
        @Override
        public int getRowCount() {
            return sections != null ? sections.size() : 0;
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
            Section section = sections.get(rowIndex);
            try {
                Course course = courseDao.findById(section.getCourseId());
                switch (columnIndex) {
                    case 0: return section.getSectionId();
                    case 1: return course.getCode();
                    case 2: return course.getTitle();
                    case 3:
                        if (section.getInstructorId() != null) {
                            try {
                                UserAuth user = authDao.findById(section.getInstructorId());
                                return user.getUsername();
                            } catch (Exception e) {
                                return "ID: " + section.getInstructorId();
                            }
                        }
                        return "None";
                    case 4: return section.getDayTime();
                    case 5: return section.getRoom();
                    case 6: return section.getCapacity();
                    case 7: return enrollmentDao.countBySection(section.getSectionId());
                    case 8: return section.getSemester();
                    case 9: return section.getYear();
                    default: return null;
                }
            } catch (Exception e) {
                return "Error";
            }
        }
    }
    
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
            
    private static class ComboBoxItem {
        private final String display;
        private final Integer id;
        
        public ComboBoxItem(String display, Integer id) {
            this.display = display;
            this.id = id;
        }
        
        public Integer getId() {
            return id;
        }
        
        @Override
        public String toString() {
            return display;
        }
    }
}


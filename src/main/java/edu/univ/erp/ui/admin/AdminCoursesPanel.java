package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.ui.theme.Theme;
import edu.univ.erp.util.Validators;

public class AdminCoursesPanel extends JPanel {
    private final AdminApi adminApi;
    private final CourseDao courseDao;
    private JTable coursesTable;
    private CoursesTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    
    public AdminCoursesPanel(AdminApi adminApi) {
        this.adminApi = adminApi;
        this.courseDao = new CourseDao();
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Theme.HEADING);
        add(titleLabel, BorderLayout.NORTH);
        
        tableModel = new CoursesTableModel();
        coursesTable = new JTable(tableModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.setRowHeight(25);
        coursesTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        coursesTable.setSelectionForeground(Color.BLACK);
        coursesTable.getTableHeader().setBackground(Theme.ACCENT);
        coursesTable.getTableHeader().setForeground(Color.WHITE);
        coursesTable.getTableHeader().setFont(coursesTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addButton = new JButton("Add Course");
        Theme.styleButton(addButton);
        addButton.addActionListener(e -> showCourseDialog(null));
        buttonPanel.add(addButton);
        
        editButton = new JButton("Edit Course");
        Theme.styleButton(editButton);
        editButton.addActionListener(e -> {
            int selectedRow = coursesTable.getSelectedRow();
            if (selectedRow >= 0) {
                Course course = tableModel.getCourseAt(selectedRow);
                showCourseDialog(course);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a course to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(editButton);
        
        deleteButton = new JButton("Delete Course");
        Theme.styleButton(deleteButton);
        deleteButton.addActionListener(e -> deleteSelectedCourse());
        buttonPanel.add(deleteButton);
        
        refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadData());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        var response = adminApi.listCourses();
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading courses",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<edu.univ.erp.api.types.CourseRow> courseRows = response.getData();
        List<Course> courses = new java.util.ArrayList<>();
        for (edu.univ.erp.api.types.CourseRow row : courseRows) {
            try {
                Course course = courseDao.findById(row.getCourseId());
                courses.add(course);
            } catch (Exception e) {
            }
        }
        tableModel.setCourses(courses);
    }
    
    private void showCourseDialog(Course course) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            course == null ? "Create Course" : "Edit Course", true);
        dialog.setSize(400, 250);
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
        if (course != null) {
            codeField.setText(course.getCode());
        }
        panel.add(codeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField titleField = new JTextField(20);
        if (course != null) {
            titleField.setText(course.getTitle());
        }
        panel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(
            course != null ? course.getCredits() : 3, 1, 10, 1));
        panel.add(creditsSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton(course == null ? "Create" : "Save");
        Theme.styleButton(saveButton);
        JButton cancelButton = new JButton("Cancel");
        Theme.styleButton(cancelButton);
        
        saveButton.addActionListener(e -> {
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
            if (course == null) {
                var createResponse = adminApi.createCourse(code, title, credits);
                if (createResponse.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Course created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        createResponse.getMessage() != null ? createResponse.getMessage() : "Error creating course",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                var updateResponse = adminApi.updateCourse(course.getCourseId(), code, title, credits);
                if (updateResponse.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Course updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        updateResponse.getMessage() != null ? updateResponse.getMessage() : "Error updating course",
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
    
    private void deleteSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a course to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Course course = tableModel.getCourseAt(selectedRow);
        
        int sectionCount = courseDao.countSections(course.getCourseId());
        if (sectionCount > 0) {
            String message = String.format(
                "Cannot delete course '%s': %d section(s) exist.\n\n" +
                "Delete sections first or use force delete (cascade).\n\n" +
                "Force delete will also delete:\n" +
                "- All enrollments in those sections\n" +
                "- All grades for those enrollments\n" +
                "- All sections of this course\n\n" +
                "This action cannot be undone!",
                course.getCode(), sectionCount);
            
            int option = JOptionPane.showConfirmDialog(this,
                message,
                "Cannot Delete Course",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
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
                    var response = adminApi.deleteCourse(course.getCourseId(), true);
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                            "Course deleted successfully (cascade).",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            response.getMessage() != null ? response.getMessage() : "Error deleting course",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete course '" + course.getCode() + "'?\nThis cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            var response = adminApi.deleteCourse(course.getCourseId(), false);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                    "Course deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                    response.getMessage() != null ? response.getMessage() : "Error deleting course",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class CoursesTableModel extends AbstractTableModel {
        private List<Course> courses;
        private final String[] columnNames = {"Course ID", "Code", "Title", "Credits", "Sections"};
        
        public void setCourses(List<Course> courses) {
            this.courses = courses;
            fireTableDataChanged();
        }
        
        public Course getCourseAt(int row) {
            return courses.get(row);
        }
        
        @Override
        public int getRowCount() {
            return courses != null ? courses.size() : 0;
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
            Course course = courses.get(rowIndex);
            switch (columnIndex) {
                case 0: return course.getCourseId();
                case 1: return course.getCode();
                case 2: return course.getTitle();
                case 3: return course.getCredits();
                case 4: return courseDao.countSections(course.getCourseId());
                default: return null;
            }
        }
    }
}


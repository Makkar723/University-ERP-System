package edu.univ.erp.ui;

import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.data.dao.GradeDao; // Still needed for reading grades - TODO: add to API
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.StudentEnrollmentRow;
import edu.univ.erp.util.EventBus;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.univ.erp.ui.theme.Theme;

/**
 * Gradebook frame for instructors to enter and compute grades with dynamic components.
 */
public class GradebookFrame extends JFrame {
    private final int sectionId;
    private final InstructorApi instructorApi;
    private final GradeDao gradeDao; // Still needed for reading grades - TODO: add to API
    private final MaintenanceApi maintenanceApi;
    private JTable gradebookTable;
    private GradebookTableModel tableModel;
    private List<StudentEnrollmentRow> roster;
    private List<String> components; // Dynamic component list
    private JButton saveScoresButton;
    private JButton computeFinalButton;
    
    public GradebookFrame(int sectionId) {
        this.sectionId = sectionId;
        this.instructorApi = new InstructorApi();
        this.gradeDao = new GradeDao(); // Still needed for reading grades - TODO: add to API
        this.maintenanceApi = new MaintenanceApi();
        
        initializeUI();
        loadData();
        subscribeToMaintenanceEvents();
        updateButtonStates();
    }
    
    private void initializeUI() {
        setTitle("Gradebook - Section " + sectionId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel("Section " + sectionId + " - Gradebook");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setForeground(Theme.HEADING);
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tableModel = new GradebookTableModel();
        gradebookTable = new JTable(tableModel);
        gradebookTable.setRowHeight(25);
        gradebookTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        gradebookTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        gradebookTable.setSelectionForeground(Color.BLACK);
        gradebookTable.getTableHeader().setBackground(Theme.ACCENT);
        gradebookTable.getTableHeader().setForeground(Color.WHITE);
        gradebookTable.getTableHeader().setFont(gradebookTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        // Configure cell editor to commit on Enter
        gradebookTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        // Add Enter key to stop editing and move to next cell
        gradebookTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke("ENTER"), "stopEditing");
        gradebookTable.getActionMap().put("stopEditing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gradebookTable.isEditing()) {
                    gradebookTable.getCellEditor().stopCellEditing();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(gradebookTable);
        scrollPane.setPreferredSize(new Dimension(1100, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        saveScoresButton = new JButton("Save Scores");
        Theme.styleButton(saveScoresButton);
        saveScoresButton.addActionListener(e -> saveScores());
        buttonsPanel.add(saveScoresButton);
        
        computeFinalButton = new JButton("Compute Final Grades");
        Theme.styleButton(computeFinalButton);
        computeFinalButton.addActionListener(e -> computeFinalGrades());
        buttonsPanel.add(computeFinalButton);
        
        JButton showStatsButton = new JButton("Show Stats");
        Theme.styleButton(showStatsButton);
        showStatsButton.addActionListener(e -> showStats());
        buttonsPanel.add(showStatsButton);
        
        JButton refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadData());
        buttonsPanel.add(refreshButton);
        
        // Layout: table in center, buttons at bottom
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
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
        var maintenanceResponse = maintenanceApi.isMaintenanceOn();
        boolean maintenanceOn = maintenanceResponse.isSuccess() && Boolean.TRUE.equals(maintenanceResponse.getData());
        
        if (saveScoresButton != null) {
            saveScoresButton.setEnabled(!maintenanceOn);
            if (maintenanceOn) {
                saveScoresButton.setToolTipText("Disabled: Maintenance mode is enabled");
            } else {
                saveScoresButton.setToolTipText(null);
            }
        }
        
        if (computeFinalButton != null) {
            computeFinalButton.setEnabled(!maintenanceOn);
            if (maintenanceOn) {
                computeFinalButton.setToolTipText("Disabled: Maintenance mode is enabled");
            } else {
                computeFinalButton.setToolTipText(null);
            }
        }
        
        // Disable table editing when maintenance is on
        if (gradebookTable != null) {
            // The table model will handle this via isCellEditable
        }
    }
    
    private void loadData() {
        // Get or create default components (quiz, midsem, endsem)
        var componentsResponse = instructorApi.getOrCreateDefaultComponents(sectionId);
        if (!componentsResponse.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                componentsResponse.getMessage() != null ? componentsResponse.getMessage() : "Error loading components",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            if (componentsResponse.getMessage() != null && componentsResponse.getMessage().contains("Maintenance")) {
                dispose();
            }
            return;
        }
        components = componentsResponse.getData();
        
        // Load roster
        var rosterResponse = instructorApi.getSectionRoster(sectionId);
        if (!rosterResponse.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                rosterResponse.getMessage() != null ? rosterResponse.getMessage() : "Error loading roster",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            if (rosterResponse.getMessage() != null && rosterResponse.getMessage().contains("Maintenance")) {
                dispose();
            }
            return;
        }
        roster = rosterResponse.getData();
        
        // Load grades for each student
        for (StudentEnrollmentRow row : roster) {
            List<Grade> grades = gradeDao.listByEnrollment(row.getEnrollmentId());
            row.setGrades(grades);
        }
        
        // Update table model
        tableModel.setRosterAndComponents(roster, components);
    }
    
    private void saveScores() {
        // Fail-fast check for maintenance mode
        var maintenanceResponse = maintenanceApi.isMaintenanceOn();
        if (maintenanceResponse.isSuccess() && Boolean.TRUE.equals(maintenanceResponse.getData())) {
            JOptionPane.showMessageDialog(this,
                "Cannot change anything- Maintenance mode is enabled",
                "Maintenance Mode",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Stop cell editing first to ensure all edits are committed
        if (gradebookTable.isEditing()) {
            gradebookTable.getCellEditor().stopCellEditing();
        }
        
        // Validate all scores before saving
            for (String component : components) {
                int componentColumn = tableModel.getComponentColumnIndex(component);
                if (componentColumn < 0) {
                    continue;
                }
                
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    Object scoreObj = tableModel.getValueAt(row, componentColumn);
                    
                    // Check if it's a valid number or null/empty
                    if (scoreObj != null && !(scoreObj instanceof Number)) {
                        String str = scoreObj.toString().trim();
                        if (!str.isEmpty()) {
                            try {
                                double score = Double.parseDouble(str);
                                if (score < 0 || score > 100) {
                                    StudentEnrollmentRow studentRow = roster.get(row);
                                    String studentName = studentRow.getStudentUsername() != null ? 
                                        studentRow.getStudentUsername() : "Row " + (row + 1);
                                    throw new IllegalArgumentException(
                                        String.format("Invalid score in row %d (%s) for component '%s': %.2f. Score must be between 0 and 100.",
                                            row + 1, studentName, component, score));
                                }
                            } catch (NumberFormatException e) {
                                StudentEnrollmentRow studentRow = roster.get(row);
                                String studentName = studentRow.getStudentUsername() != null ? 
                                    studentRow.getStudentUsername() : "Row " + (row + 1);
                                throw new IllegalArgumentException(
                                    String.format("Invalid number in row %d (%s) for component '%s': '%s'. Please enter a number between 0 and 100.",
                                        row + 1, studentName, component, str));
                            }
                        }
                    }
                }
            }
            
            // For each component, collect scores
            for (String component : components) {
                Map<Integer, Double> scores = new HashMap<>();
                
                int componentColumn = tableModel.getComponentColumnIndex(component);
                if (componentColumn < 0) {
                    continue; // Skip if column not found
                }
                
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    StudentEnrollmentRow studentRow = roster.get(row);
                    int enrollmentId = studentRow.getEnrollmentId();
                    
                    Object scoreObj = tableModel.getValueAt(row, componentColumn);
                    Double score = parseScore(scoreObj);
                    
                    // Additional validation: score must be 0-100 or null
                    if (score != null && (score < 0 || score > 100)) {
                        String studentName = studentRow.getStudentUsername() != null ? 
                            studentRow.getStudentUsername() : "Row " + (row + 1);
                        throw new IllegalArgumentException(
                            String.format("Score must be between 0 and 100. Got %.2f for %s in component '%s'.",
                                score, studentName, component));
                    }
                    
                    scores.put(enrollmentId, score);
                }
                
                // Save scores for this component
                var response = instructorApi.updateComponentScores(sectionId, component, scores);
                if (!response.isSuccess()) {
                    String errorMsg = response.getMessage();
                    if (errorMsg != null && errorMsg.contains("Maintenance")) {
                        JOptionPane.showMessageDialog(this,
                            errorMsg,
                            "Maintenance Mode",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            errorMsg != null ? errorMsg : "Error saving scores",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
            
            // Clear edited values after successful save
            tableModel.clearEditedValues();
            
            JOptionPane.showMessageDialog(this,
                "Scores saved successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh to show saved values
            loadData();
        }
    
    private Double parseScore(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            return num;
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null; // Treat empty as null (will be treated as 0 in computation)
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void computeFinalGrades() {
        // Fail-fast check for maintenance mode
        var maintenanceResponse = maintenanceApi.isMaintenanceOn();
        if (maintenanceResponse.isSuccess() && Boolean.TRUE.equals(maintenanceResponse.getData())) {
            JOptionPane.showMessageDialog(this,
                "Cannot change anything- Maintenance mode is enabled",
                "Maintenance Mode",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show dialog for weights
        WeightDialog dialog = new WeightDialog(this, components);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Map<String, Double> weights = dialog.getWeights();
            var response = instructorApi.computeFinalGrades(sectionId, weights);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                    "Final grades computed successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh to show computed final grades
                loadData();
            } else {
                String errorMsg = response.getMessage();
                if (errorMsg != null && errorMsg.contains("Maintenance")) {
                    JOptionPane.showMessageDialog(this,
                        errorMsg,
                        "Maintenance Mode",
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        errorMsg != null ? errorMsg : "Error computing final grades",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void showStats() {
        var response = instructorApi.getSectionStats(sectionId);
        
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading statistics",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Map<String, Double> stats = response.getData().getStats();
        
        String message = String.format(
            "Section Statistics:\n\n" +
            "Average Final: %.2f\n" +
            "Min Final: %.2f\n" +
            "Max Final: %.2f",
            stats.get("avgFinal"),
            stats.get("minFinal"),
            stats.get("maxFinal")
        );
        
        JOptionPane.showMessageDialog(this,
            message,
            "Section Statistics",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Table model for gradebook with dynamic components.
     */
    private class GradebookTableModel extends AbstractTableModel {
        private List<StudentEnrollmentRow> roster;
        private List<String> components;
        // Store edited values temporarily (key: "row,column", value: edited value)
        private Map<String, Object> editedValues = new HashMap<>();
        
        public void setRosterAndComponents(List<StudentEnrollmentRow> roster, List<String> components) {
            this.roster = roster;
            this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
            this.editedValues.clear(); // Clear edited values when refreshing
            fireTableStructureChanged();
        }
        
        public void clearEditedValues() {
            editedValues.clear();
        }
        
        public int getComponentColumnIndex(String component) {
            // Fixed columns: 0=Roll No, 1=Username, then components, last=Final
            int index = 2; // Start after Roll No and Username
            for (String comp : components) {
                if (comp.equals(component)) {
                    return index;
                }
                index++;
            }
            return -1;
        }
        
        @Override
        public int getRowCount() {
            return roster == null ? 0 : roster.size();
        }
        
        @Override
        public int getColumnCount() {
            // Fixed: Roll No, Username, then dynamic components, then Final
            return 2 + (components != null ? components.size() : 0) + 1;
        }
        
        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Roll No";
            } else if (column == 1) {
                return "Username";
            } else if (column == getColumnCount() - 1) {
                return "Final";
            } else {
                // Component column
                int compIndex = column - 2;
                if (components != null && compIndex >= 0 && compIndex < components.size()) {
                    return components.get(compIndex);
                }
                return "?";
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 1) {
                return String.class;
            } else if (columnIndex == getColumnCount() - 1) {
                return String.class; // Final is read-only string
            } else {
                return Double.class; // Component scores are editable
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Disable editing if maintenance mode is on
            // Note: maintenanceApi is in outer class, accessing via closure
            // Only component columns are editable (not Roll No, Username, or Final)
            return columnIndex >= 2 && columnIndex < getColumnCount() - 1;
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            if (roster == null || row >= roster.size()) {
                return null;
            }
            
            // Check if this cell has been edited
            String key = row + "," + column;
            if (editedValues.containsKey(key)) {
                return editedValues.get(key);
            }
            
            StudentEnrollmentRow studentRow = roster.get(row);
            List<Grade> grades = studentRow.getGrades();
            
            if (column == 0) {
                return studentRow.getStudentRollNo() != null ? studentRow.getStudentRollNo() : "";
            } else if (column == 1) {
                return studentRow.getStudentUsername() != null ? studentRow.getStudentUsername() : "";
            } else if (column == getColumnCount() - 1) {
                // Final column
                return getFinalGradeDisplay(grades);
            } else {
                // Component column
                int compIndex = column - 2;
                if (components != null && compIndex >= 0 && compIndex < components.size()) {
                    String component = components.get(compIndex);
                    return getScoreForComponent(grades, component);
                }
                return null;
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            // Store the edited value
            String key = rowIndex + "," + columnIndex;
            
            // Convert the value to Double if it's a number or string
            if (aValue == null || (aValue instanceof String && ((String) aValue).trim().isEmpty())) {
                editedValues.put(key, null);
            } else if (aValue instanceof Number) {
                editedValues.put(key, ((Number) aValue).doubleValue());
            } else if (aValue instanceof String) {
                String str = ((String) aValue).trim();
                if (str.isEmpty()) {
                    editedValues.put(key, null);
                } else {
                    try {
                        editedValues.put(key, Double.parseDouble(str));
                    } catch (NumberFormatException e) {
                        // Invalid number, keep the string (will be validated on save)
                        editedValues.put(key, str);
                    }
                }
            } else {
                editedValues.put(key, aValue);
            }
            
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        
        private Double getScoreForComponent(List<Grade> grades, String component) {
            if (grades == null) {
                return null;
            }
            for (Grade grade : grades) {
                if (component.equalsIgnoreCase(grade.getComponent()) && grade.getScore() != null) {
                    return grade.getScore().doubleValue();
                }
            }
            return null;
        }
        
        private String getFinalGradeDisplay(List<Grade> grades) {
            if (grades == null) {
                return "";
            }
            for (Grade grade : grades) {
                if ("final".equalsIgnoreCase(grade.getComponent())) {
                    StringBuilder sb = new StringBuilder();
                    if (grade.getScore() != null) {
                        sb.append(String.format("%.2f", grade.getScore().doubleValue()));
                    }
                    if (grade.getFinalGrade() != null && !grade.getFinalGrade().isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(" (");
                        }
                        sb.append(grade.getFinalGrade());
                        if (sb.length() > 0 && !sb.toString().startsWith("(")) {
                            sb.append(")");
                        }
                    }
                    return sb.toString();
                }
            }
            return "";
        }
    }
    
    /**
     * Dialog for entering grade weights for dynamic components.
     */
    private static class WeightDialog extends JDialog {
        private List<String> components;
        private Map<String, JTextField> weightFields;
        private boolean confirmed = false;
        
        public WeightDialog(JFrame parent, List<String> components) {
            super(parent, "Enter Grade Weights", true);
            this.components = components != null ? new ArrayList<>(components) : new ArrayList<>();
            this.weightFields = new HashMap<>();
            
            setSize(400, Math.max(250, 100 + components.size() * 40));
            setLocationRelativeTo(parent);
            setResizable(false);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Instructions
            JLabel instructionLabel = new JLabel("Enter weights as percentages (must sum to 100):");
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(instructionLabel, gbc);
            
            gbc.gridwidth = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Add a field for each component
            for (int i = 0; i < components.size(); i++) {
                String component = components.get(i);
                gbc.gridx = 0;
                gbc.gridy = i + 1;
                panel.add(new JLabel(component + " (%):"), gbc);
                
                gbc.gridx = 1;
                JTextField field = new JTextField(15);
                field.setEditable(true);
                field.setEnabled(true);
                
                // Set default weights: quiz=20, midsem=30, endsem=50, others=0
                double defaultWeight = 0.0;
                if ("quiz".equalsIgnoreCase(component)) {
                    defaultWeight = 20.0;
                } else if ("midsem".equalsIgnoreCase(component)) {
                    defaultWeight = 30.0;
                } else if ("endsem".equalsIgnoreCase(component)) {
                    defaultWeight = 50.0;
                } else {
                    // Other components default to 0
                    defaultWeight = 0.0;
                }
                field.setText(String.format("%.0f", defaultWeight));
                weightFields.put(component, field);
                panel.add(field, gbc);
            }
            
            // Buttons
            gbc.gridx = 0;
            gbc.gridy = components.size() + 2;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(15, 5, 5, 5);
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton okButton = new JButton("OK");
            Theme.styleButton(okButton);
            okButton.setPreferredSize(new Dimension(80, 30));
            okButton.addActionListener(e -> {
                // Validate weights sum to 100
                try {
                    double total = 0.0;
                    for (String comp : components) {
                        JTextField field = weightFields.get(comp);
                        double weight = parseWeight(field.getText());
                        if (weight < 0) {
                            JOptionPane.showMessageDialog(this,
                                "All weights must be non-negative.",
                                "Invalid Weights",
                                JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        total += weight;
                    }
                    
                    if (Math.abs(total - 100.0) > 0.01) {
                        JOptionPane.showMessageDialog(this,
                            String.format("Weights must sum to 100%%. Current sum: %.2f%%", total),
                            "Invalid Weights",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    confirmed = true;
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for all weights.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
            buttonPanel.add(okButton);
            
            JButton cancelButton = new JButton("Cancel");
            Theme.styleButton(cancelButton);
            cancelButton.setPreferredSize(new Dimension(80, 30));
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);
            
            panel.add(buttonPanel, gbc);
            
            add(panel);
            
            // Request focus on first field
            if (!components.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    JTextField firstField = weightFields.get(components.get(0));
                    if (firstField != null) {
                        firstField.requestFocus();
                        firstField.selectAll();
                    }
                });
            }
            
            // Add Enter key support
            getRootPane().setDefaultButton(okButton);
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public Map<String, Double> getWeights() {
            Map<String, Double> weights = new HashMap<>();
            for (String comp : components) {
                JTextField field = weightFields.get(comp);
                double weight = parseWeight(field.getText());
                weights.put(comp, weight);
            }
            return weights;
        }
        
        private double parseWeight(String text) {
            if (text == null || text.trim().isEmpty()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Invalid number: " + text);
            }
        }
    }
}

package edu.univ.erp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.api.types.StudentGradeRow;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.ui.theme.Theme;

/**
 * Panel for displaying student grades.
 */
public class GradesPanel extends JPanel {
    private final StudentApi studentApi;
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    
    public GradesPanel() {
        this.studentApi = new StudentApi();
        initializeUI();
        loadGrades();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("My Grades", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Theme.HEADING);
        add(titleLabel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Table columns
        String[] columnNames = {
            "Course Code", "Section ID", "Component", "Score", "Final Grade"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gradesTable = new JTable(tableModel);
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        gradesTable.getTableHeader().setReorderingAllowed(false);
        gradesTable.setSelectionBackground(Theme.ACCENT_LIGHT);
        gradesTable.setSelectionForeground(Color.BLACK);
        gradesTable.getTableHeader().setBackground(Theme.ACCENT);
        gradesTable.getTableHeader().setForeground(Color.WHITE);
        gradesTable.getTableHeader().setFont(gradesTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadGrades());
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadGrades() {
        Integer currentUserId = SessionManager.getCurrentUser().getUserId();
        var response = studentApi.getGrades(currentUserId);
        
        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                response.getMessage() != null ? response.getMessage() : "Error loading grades",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<StudentGradeRow> grades = response.getData();
        
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add grades to table
        for (StudentGradeRow grade : grades) {
            Object[] row = {
                grade.getCourseCode() != null ? grade.getCourseCode() : "-",
                grade.getSectionId() != null ? grade.getSectionId().toString() : "-",
                grade.getComponent() != null ? grade.getComponent() : "-",
                grade.getScore() != null ? grade.getScore().toString() : "-",
                grade.getFinalGrade() != null ? grade.getFinalGrade() : "-"
            };
            tableModel.addRow(row);
        }
    }
}



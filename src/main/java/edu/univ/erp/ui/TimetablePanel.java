package edu.univ.erp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.DayOfWeek;
import java.time.LocalTime;
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
import javax.swing.table.AbstractTableModel;

import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.SectionMeeting;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.util.SectionScheduleUtil;

/**
 * Timetable panel showing a Mon-Fri grid with 30-minute time slots.
 */
public class TimetablePanel extends JPanel {
    private static final LocalTime START_TIME = LocalTime.of(8, 0);
    private static final LocalTime END_TIME = LocalTime.of(18, 0);
    
    private JTable timetableTable;
    private TimetableTableModel tableModel;
    private JButton refreshButton;
    
    public TimetablePanel() {
        initializeUI();
        loadTimetable();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("My Timetable");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTimetable());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table
        tableModel = new TimetableTableModel();
        timetableTable = new JTable(tableModel);
        timetableTable.setRowHeight(30);
        timetableTable.setDefaultRenderer(Object.class, new TimetableCellRenderer());
        timetableTable.setEnabled(false); // Read-only
        
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadTimetable() {
        UserAuth currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "Not logged in",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("Loading timetable for user: " + currentUser.getUserId());
        
        // Get registered sections via API
        StudentApi studentApi = new StudentApi();
        var sectionsResponse = studentApi.getRegisteredSectionsAsDomain(currentUser.getUserId());
        
        if (!sectionsResponse.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                sectionsResponse.getMessage() != null ? sectionsResponse.getMessage() : "Error loading timetable",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<Section> sections = sectionsResponse.getData();
        System.out.println("Found " + sections.size() + " registered sections");
        
        // Load course info via API
        CatalogApi catalogApi = new CatalogApi();
        var coursesResponse = catalogApi.listCourses();
        
        Map<Integer, Course> courseMap = new HashMap<>();
        if (coursesResponse.isSuccess() && coursesResponse.getData() != null) {
            List<CourseRow> courseRows = coursesResponse.getData();
            // Create Course objects from CourseRow DTOs for compatibility
            for (CourseRow courseRow : courseRows) {
                Course course = new Course();
                course.setCourseId(courseRow.getCourseId());
                course.setCode(courseRow.getCode());
                course.setTitle(courseRow.getTitle());
                course.setCredits(courseRow.getCredits());
                courseMap.put(courseRow.getCourseId(), course);
            }
        }
        
        // Build timetable data
        tableModel.loadSections(sections, courseMap);
        timetableTable.repaint();
    }
    
    /**
     * Table model for timetable grid.
     */
    private static class TimetableTableModel extends AbstractTableModel {
        private static final String[] COLUMN_NAMES = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        private static final int DAY_COLUMNS = 5; // Mon-Fri
        
        private String[][] cellData; // [row][col] where col 0 = time, cols 1-5 = days
        private int rowCount;
        
        public TimetableTableModel() {
            initializeSlots();
        }
        
        private void initializeSlots() {
            // Calculate number of 30-minute slots
            int minutes = (int) java.time.Duration.between(START_TIME, END_TIME).toMinutes();
            rowCount = minutes / 30;
            
            cellData = new String[rowCount][COLUMN_NAMES.length];
            // Initialize time column
            LocalTime currentTime = START_TIME;
            for (int i = 0; i < rowCount; i++) {
                cellData[i][0] = String.format("%02d:%02d", currentTime.getHour(), currentTime.getMinute());
                for (int j = 1; j < COLUMN_NAMES.length; j++) {
                    cellData[i][j] = "";
                }
                currentTime = currentTime.plusMinutes(30);
            }
        }
        
        public void loadSections(List<Section> sections, Map<Integer, Course> courseMap) {
            // Clear existing data (keep time column)
            for (int i = 0; i < rowCount; i++) {
                for (int j = 1; j < COLUMN_NAMES.length; j++) {
                    cellData[i][j] = "";
                }
            }
            
            System.out.println("Loading " + sections.size() + " sections into timetable");
            
            // Fill in sections
            for (Section section : sections) {
                Course course = courseMap.get(section.getCourseId());
                String courseCode = course != null ? course.getCode() : "?";
                String label = courseCode + " (S" + section.getSectionId() + ")";
                
                System.out.println("Processing section " + section.getSectionId() + 
                    ", dayTime: " + section.getDayTime());
                
                // Parse meetings
                List<SectionMeeting> meetings;
                try {
                    meetings = SectionScheduleUtil.parseSchedule(section.getDayTime());
                    System.out.println("  Parsed " + meetings.size() + " meetings");
                } catch (Exception e) {
                    System.err.println("Error parsing schedule for section " + section.getSectionId() + 
                        ": " + e.getMessage());
                    System.err.println("  Schedule string: '" + section.getDayTime() + "'");
                    continue;
                }
                
                if (meetings.isEmpty()) {
                    System.out.println("  No meetings found for section " + section.getSectionId());
                    continue;
                }
                
                for (SectionMeeting meeting : meetings) {
                    DayOfWeek day = meeting.getDay();
                    // Skip weekends
                    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                        continue;
                    }
                    
                    int dayColumn = day.getValue(); // MON=1, TUE=2, etc.
                    if (dayColumn < 1 || dayColumn > 5) {
                        continue;
                    }
                    
                    LocalTime start = meeting.getStart();
                    LocalTime end = meeting.getEnd();
                    
                    System.out.println("  Meeting: " + day + " " + start + "-" + end);
                    
                    // Find rows for this time range
                    int startRow = findRowForTime(start);
                    int endRow = findRowForTime(end);
                    
                    System.out.println("    Start row: " + startRow + ", End row: " + endRow);
                    
                    if (startRow < 0 || endRow < 0) {
                        System.err.println("    WARNING: Could not find rows for time range");
                        continue;
                    }
                    
                    // Fill cells - endRow is the slot that starts at the end time,
                    // so we should NOT include it (meeting ends at start of that slot)
                    // Example: 09:00-10:30 should fill rows for 09:00, 09:30, 10:00 but NOT 10:30
                    for (int row = startRow; row < endRow; row++) {
                        String existing = cellData[row][dayColumn];
                        if (existing == null || existing.isEmpty()) {
                            cellData[row][dayColumn] = label;
                        } else {
                            // Conflict detected - combine labels
                            if (!existing.contains(label)) {
                                cellData[row][dayColumn] = existing + " / " + label;
                            }
                        }
                    }
                }
            }
            
            fireTableDataChanged();
        }
        
        private int findRowForTime(LocalTime time) {
            LocalTime currentTime = START_TIME;
            for (int i = 0; i < rowCount; i++) {
                // Check if time matches this slot exactly
                if (time.equals(currentTime)) {
                    return i;
                }
                // Check if time falls within this 30-minute slot
                LocalTime nextTime = currentTime.plusMinutes(30);
                if (time.isAfter(currentTime) && time.isBefore(nextTime)) {
                    return i;
                }
                currentTime = nextTime;
            }
            return -1;
        }
        
        @Override
        public int getRowCount() {
            return rowCount;
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= rowCount || columnIndex < 0 || columnIndex >= COLUMN_NAMES.length) {
                return "";
            }
            return cellData[rowIndex][columnIndex];
        }
    }
    
    /**
     * Custom cell renderer to highlight conflicts and make timetable more readable.
     */
    private static class TimetableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (column == 0) {
                // Time column
                c.setBackground(Color.LIGHT_GRAY);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                String text = value != null ? value.toString() : "";
                if (text.isEmpty()) {
                    c.setBackground(Color.WHITE);
                } else if (text.contains(" / ")) {
                    // Conflict detected
                    c.setBackground(new Color(255, 200, 200)); // Light red
                    c.setForeground(Color.RED);
                } else {
                    // Normal class
                    c.setBackground(new Color(200, 255, 200)); // Light green
                    c.setForeground(Color.BLACK);
                }
            }
            
            return c;
        }
    }
}

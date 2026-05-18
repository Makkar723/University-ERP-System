package edu.univ.erp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.data.dao.GradeDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.data.dao.StudentDao;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.StudentEnrollmentRow;

// instructor operations service
public class InstructorService {
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final StudentDao studentDao;
    private final GradeDao gradeDao;
    private final MaintenanceService maintenanceService;
    private final AuthDao authDao;
    
    public InstructorService(SectionDao sectionDao, EnrollmentDao enrollmentDao,
                            StudentDao studentDao, GradeDao gradeDao,
                            MaintenanceService maintenanceService, AuthDao authDao) {
        this.sectionDao = sectionDao;
        this.enrollmentDao = enrollmentDao;
        this.studentDao = studentDao;
        this.gradeDao = gradeDao;
        this.maintenanceService = maintenanceService;
        this.authDao = authDao;
    }
    
    /**
     * Get all sections assigned to the current instructor.
     */
    public List<Section> getMySections() {
        AccessController.requireInstructor();
        int instructorId = SessionManager.getCurrentUser().getUserId();
        return sectionDao.listSectionsByInstructor(instructorId);
    }
    
    /**
     * Get the roster (student enrollment rows) for a section.
     * Only returns students with status = 'enrolled'.
     */
    public List<StudentEnrollmentRow> getSectionRoster(int sectionId) {
        AccessController.requireInstructor();
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        List<StudentEnrollmentRow> rows = enrollmentDao.listStudentRowsBySection(sectionId);
        
        // Fill in usernames from auth_db
        for (StudentEnrollmentRow row : rows) {
            try {
                var user = authDao.findById(row.getStudentUserId());
                row.setStudentUsername(user.getUsername());
            } catch (Exception e) {
                // If user not found, leave username as null
                row.setStudentUsername("N/A");
            }
        }
        
        return rows;
    }
    
    /**
     * Get list of components for a section (excluding "final").
     */
    public List<String> getComponentsForSection(int sectionId) {
        AccessController.requireInstructor();
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        return gradeDao.listComponentsForSection(sectionId);
    }
    
    /**
     * Get or create default components (quiz, midsem, endsem) for a section.
     * Ensures these three components always exist when gradebook is opened.
     * Returns the full list of components (defaults + any additional).
     */
    public List<String> getOrCreateDefaultComponents(int sectionId) {
        AccessController.requireInstructor();
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        List<String> existing = gradeDao.listComponentsForSection(sectionId);
        
        // Default components that must exist
        List<String> defaults = List.of("quiz", "midsem", "endsem");
        List<String> missing = new ArrayList<>();
        
        for (String defaultComp : defaults) {
            boolean found = false;
            for (String existingComp : existing) {
                if (defaultComp.equalsIgnoreCase(existingComp)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(defaultComp);
            }
        }
        
        // Create missing default components
        for (String component : missing) {
            gradeDao.createComponentForSection(sectionId, component);
        }
        
        // Return updated list (re-query to get all components including newly created)
        List<String> allComponents = gradeDao.listComponentsForSection(sectionId);
        
        // Ensure defaults are in the list (in case they were just created)
        for (String defaultComp : defaults) {
            boolean found = false;
            for (String comp : allComponents) {
                if (defaultComp.equalsIgnoreCase(comp)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                allComponents.add(defaultComp);
            }
        }
        
        return allComponents;
    }
    
    /**
     * Add a new component to a section.
     * This does not create grade rows immediately; they will be created when scores are entered.
     */
    public void addComponentToSection(int sectionId, String componentName) {
        AccessController.requireInstructor();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // Check ownership
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        // Validate component name
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name is required.");
        }
        
        String normalizedName = componentName.trim();
        
        // Check if reserved
        if ("final".equalsIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Component name 'final' is reserved.");
        }
        
        // Check if already exists
        List<String> existing = gradeDao.listComponentsForSection(sectionId);
        for (String existingComp : existing) {
            if (existingComp.equalsIgnoreCase(normalizedName)) {
                throw new IllegalArgumentException("Component '" + normalizedName + "' already exists.");
            }
        }
        
        // Component will be created when first score is entered via updateComponentScores
        // No immediate action needed
    }
    
    /**
     * Remove a component from a section (deletes all grade rows for that component).
     */
    public void removeComponentFromSection(int sectionId, String componentName) {
        AccessController.requireInstructor();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // Check ownership
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        // Validate component name
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name is required.");
        }
        
        String normalizedName = componentName.trim();
        
        // Do not allow deletion of "final"
        if ("final".equalsIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Cannot delete reserved component 'final'.");
        }
        
        gradeDao.deleteComponentForSection(sectionId, normalizedName);
    }
    
    /**
     * Rename a component in a section.
     */
    public void renameComponentForSection(int sectionId, String oldName, String newName) {
        AccessController.requireInstructor();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // Check ownership
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        // Validate names
        if (oldName == null || oldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Old component name is required.");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New component name is required.");
        }
        
        String normalizedOld = oldName.trim();
        String normalizedNew = newName.trim();
        
        // Do not allow renaming "final"
        if ("final".equalsIgnoreCase(normalizedOld)) {
            throw new IllegalArgumentException("Cannot rename reserved component 'final'.");
        }
        
        // Check if new name is reserved
        if ("final".equalsIgnoreCase(normalizedNew)) {
            throw new IllegalArgumentException("Component name 'final' is reserved.");
        }
        
        // Check if new name already exists
        List<String> existing = gradeDao.listComponentsForSection(sectionId);
        for (String existingComp : existing) {
            if (existingComp.equalsIgnoreCase(normalizedNew) && !existingComp.equalsIgnoreCase(normalizedOld)) {
                throw new IllegalArgumentException("Component '" + normalizedNew + "' already exists.");
            }
        }
        
        gradeDao.renameComponentForSection(sectionId, normalizedOld, normalizedNew);
    }
    
    /**
     * Update component scores for multiple students in a section.
     * All scores must be in range 0-100 or null.
     * @param sectionId The section ID
     * @param component The component name
     * @param enrollmentIdToScore Map of enrollment ID to score (null values are allowed to clear scores, treated as 0)
     */
    public void updateComponentScores(int sectionId, String component, Map<Integer, Double> enrollmentIdToScore) {
        AccessController.requireInstructor();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // Check ownership
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        // Validate component name
        if (component == null || component.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name is required.");
        }
        
        // Do not allow updating "final" component via this method
        if ("final".equalsIgnoreCase(component.trim())) {
            throw new IllegalArgumentException("Cannot update 'final' component directly. Use computeFinalGrades instead.");
        }
        
        // Update scores for each enrollment
        for (Map.Entry<Integer, Double> entry : enrollmentIdToScore.entrySet()) {
            Integer enrollmentId = entry.getKey();
            Double score = entry.getValue();
            
            // Strict validation: score must be 0-100 or null
            if (score != null) {
                if (score < 0.0 || score > 100.0) {
                    throw new IllegalArgumentException(
                        String.format("Score must be between 0 and 100. Got: %.2f for enrollment %d", 
                            score, enrollmentId));
                }
            }
            
            gradeDao.upsertGrade(enrollmentId, component.trim(), score, null);
        }
    }
    
    /**
     * Compute final grades for all students in a section using weighted components.
     * @param sectionId The section ID
     * @param componentToWeightPercent Map of component name to weight percentage (e.g., "Quiz" -> 15.0 means 15%)
     */
    public void computeFinalGrades(int sectionId, Map<String, Double> componentToWeightPercent) {
        AccessController.requireInstructor();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // Check ownership
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        // Validate weights map is not empty
        if (componentToWeightPercent == null || componentToWeightPercent.isEmpty()) {
            throw new IllegalArgumentException("At least one component weight is required.");
        }
        
        // Validate all weights are non-negative
        for (Map.Entry<String, Double> entry : componentToWeightPercent.entrySet()) {
            if (entry.getValue() < 0.0) {
                throw new IllegalArgumentException("All weights must be non-negative. Got negative weight for: " + entry.getKey());
            }
        }
        
        // Validate weights sum to 100% (with small tolerance)
        double totalWeight = componentToWeightPercent.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        if (Math.abs(totalWeight - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                String.format("Weights must sum to 100%%. Current sum: %.2f%%", totalWeight));
        }
        
        // Get all enrollments for the section
        List<StudentEnrollmentRow> roster = getSectionRoster(sectionId);
        
        // Process each enrollment
        // Note: Each upsertGrade call is atomic. For better consistency across all students,
        // consider wrapping in a transaction if needed. Current implementation processes
        // all enrollments sequentially.
        for (StudentEnrollmentRow row : roster) {
            int enrollmentId = row.getEnrollmentId();
            
            // Get all grades for this enrollment
            List<Grade> grades = gradeDao.listByEnrollment(enrollmentId);
            
            // Build a map of component -> score (0-100)
            Map<String, Double> componentScores = new HashMap<>();
            for (Grade grade : grades) {
                if (grade.getScore() != null && !"final".equalsIgnoreCase(grade.getComponent())) {
                    componentScores.put(grade.getComponent(), grade.getScore().doubleValue());
                }
            }
            
            // Compute weighted final score
            double finalNumeric = 0.0;
            for (Map.Entry<String, Double> entry : componentToWeightPercent.entrySet()) {
                String component = entry.getKey();
                double weightPercent = entry.getValue(); // e.g., 15, 35, 50
                
                // Get score for this component (treat null/missing as 0)
                double score = componentScores.getOrDefault(component, 0.0);
                
                // Add weighted contribution: score * (weightPercent / 100.0)
                finalNumeric += score * (weightPercent / 100.0);
            }
            
            // Determine letter grade
            String letter;
            if (finalNumeric >= 85) {
                letter = "A";
            } else if (finalNumeric >= 70) {
                letter = "B";
            } else if (finalNumeric >= 55) {
                letter = "C";
            } else if (finalNumeric >= 40) {
                letter = "D";
            } else {
                letter = "F";
            }
            
            // Store final grade (component = "final", score = finalNumeric, final_grade = letter)
            gradeDao.upsertGrade(enrollmentId, "final", finalNumeric, letter);
        }
    }
    
    /**
     * Get section statistics (optional helper method).
     * Returns a map with average scores per component and final grade statistics.
     * Keys: avgFinal, minFinal, maxFinal, and avg<ComponentName> for each component.
     */
    public Map<String, Double> getSectionStats(int sectionId) {
        AccessController.requireInstructor();
        AccessController.requireInstructorOwnsSection(sectionId, sectionDao);
        
        List<StudentEnrollmentRow> roster = getSectionRoster(sectionId);
        List<String> components = getComponentsForSection(sectionId);
        
        if (roster.isEmpty()) {
            Map<String, Double> empty = new HashMap<>();
            empty.put("avgFinal", 0.0);
            empty.put("minFinal", 0.0);
            empty.put("maxFinal", 0.0);
            for (String comp : components) {
                empty.put("avg" + comp, 0.0);
            }
            return empty;
        }
        
        // Map to store sums and counts per component
        Map<String, Double> componentSums = new HashMap<>();
        Map<String, Integer> componentCounts = new HashMap<>();
        for (String comp : components) {
            componentSums.put(comp, 0.0);
            componentCounts.put(comp, 0);
        }
        
        double sumFinal = 0.0;
        double minFinal = Double.MAX_VALUE;
        double maxFinal = Double.MIN_VALUE;
        int finalCount = 0;
        int studentCount = roster.size();
        
        for (StudentEnrollmentRow row : roster) {
            List<Grade> grades = gradeDao.listByEnrollment(row.getEnrollmentId());
            
            // Process component scores
            for (Grade grade : grades) {
                if (grade.getScore() != null && !"final".equalsIgnoreCase(grade.getComponent())) {
                    String comp = grade.getComponent();
                    if (componentSums.containsKey(comp)) {
                        componentSums.put(comp, componentSums.get(comp) + grade.getScore().doubleValue());
                        componentCounts.put(comp, componentCounts.get(comp) + 1);
                    }
                } else if ("final".equalsIgnoreCase(grade.getComponent()) && grade.getScore() != null) {
                    double finalScore = grade.getScore().doubleValue();
                    sumFinal += finalScore;
                    if (finalScore < minFinal) minFinal = finalScore;
                    if (finalScore > maxFinal) maxFinal = finalScore;
                    finalCount++;
                }
            }
        }
        
        Map<String, Double> stats = new HashMap<>();
        
        // Add averages for each component
        for (String comp : components) {
            int count = componentCounts.get(comp);
            double avg = count > 0 ? componentSums.get(comp) / count : 0.0;
            stats.put("avg" + comp, avg);
        }
        
        // Add final statistics
        stats.put("avgFinal", finalCount > 0 ? sumFinal / finalCount : 0.0);
        stats.put("minFinal", minFinal == Double.MAX_VALUE ? 0.0 : minFinal);
        stats.put("maxFinal", maxFinal == Double.MIN_VALUE ? 0.0 : maxFinal);
        
        return stats;
    }
}

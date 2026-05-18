package edu.univ.erp.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.data.dao.GradeDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;

// student operations service
public class StudentService {
    private final EnrollmentDao enrollmentDao;
    private final SectionDao sectionDao;
    private final CourseDao courseDao;
    private final GradeDao gradeDao;
    private final MaintenanceService maintenanceService;
    
    // drop deadline placeholder
    private static final boolean DROP_ALLOWED = true;
    
    public StudentService(EnrollmentDao enrollmentDao, SectionDao sectionDao, 
                         CourseDao courseDao, GradeDao gradeDao, MaintenanceService maintenanceService) {
        this.enrollmentDao = enrollmentDao;
        this.sectionDao = sectionDao;
        this.courseDao = courseDao;
        this.gradeDao = gradeDao;
        this.maintenanceService = maintenanceService;
    }
    
    // register in section
    public void registerInSection(int studentUserId, int sectionId) {
        AccessController.requireStudent();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // verify user matches
        if (SessionManager.getCurrentUser() == null || 
            !SessionManager.getCurrentUser().getUserId().equals(studentUserId)) {
            throw new AccessDeniedException("You can only register for yourself.");
        }
        
        // verify section exists
        Section section = sectionDao.findById(sectionId);
        
        // check duplicate enrollment
        if (enrollmentDao.exists(studentUserId, sectionId)) {
            throw new IllegalStateException("Already registered in this section.");
        }
        
        // check capacity
        int enrolled = enrollmentDao.countBySection(sectionId);
        int capacity = sectionDao.getCapacity(sectionId);
        
        if (enrolled >= capacity) {
            throw new IllegalStateException("Section is full. Capacity: " + capacity + ", Enrolled: " + enrolled);
        }
        
        // create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentUserId);
        enrollment.setSectionId(sectionId);
        enrollment.setStatus("enrolled");
        
        enrollmentDao.createEnrollment(enrollment);
    }
    
    // drop section
    public void dropSection(int studentUserId, int sectionId) {
        AccessController.requireStudent();
        AccessController.requireNotInMaintenance(maintenanceService);
        
        // verify user matches
        if (SessionManager.getCurrentUser() == null || 
            !SessionManager.getCurrentUser().getUserId().equals(studentUserId)) {
            throw new AccessDeniedException("You can only drop sections for yourself.");
        }
        
        // check drop deadline
        if (!DROP_ALLOWED) {
            throw new IllegalStateException("Drop deadline has passed.");
        }
        
        // verify enrollment exists
        if (!enrollmentDao.exists(studentUserId, sectionId)) {
            throw new IllegalStateException("You are not enrolled in this section.");
        }
        
        // delete enrollment
        enrollmentDao.deleteEnrollment(studentUserId, sectionId);
    }
    
    // list registered sections
    public List<Section> listRegisteredSections(int studentUserId) {
        AccessController.requireStudent();
        
        List<Enrollment> enrollments = enrollmentDao.listByStudent(studentUserId);
        List<Section> sections = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            if ("enrolled".equals(enrollment.getStatus())) {
                try {
                    Section section = sectionDao.findById(enrollment.getSectionId());
                    sections.add(section);
                } catch (Exception e) {
                    // skip deleted sections
                    System.err.println("Section not found: " + enrollment.getSectionId());
                }
            }
        }
        
        return sections;
    }
    
    // list all enrollments
    public List<Enrollment> listEnrollments(int studentUserId) {
        AccessController.requireStudent();
        return enrollmentDao.listByStudent(studentUserId);
    }
    
    // list student grades
    public List<Grade> listGradesForStudent(int studentUserId) {
        AccessController.requireStudent();
        
        List<Enrollment> enrollments = enrollmentDao.listByStudent(studentUserId);
        List<Grade> allGrades = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeDao.listByEnrollment(enrollment.getEnrollmentId());
            allGrades.addAll(grades);
        }
        
        return allGrades;
    }
    
    // export transcript CSV
    public void exportTranscriptCsv(int studentUserId, File targetFile) throws IOException {
        AccessController.requireStudent();
        
        List<Enrollment> enrollments = enrollmentDao.listByStudent(studentUserId);
        
        try (FileWriter writer = new FileWriter(targetFile)) {
            // write header
            writer.append("CourseCode,CourseTitle,Credits,Semester,Year,FinalGrade\n");
            
            for (Enrollment enrollment : enrollments) {
                try {
                    Section section = sectionDao.findById(enrollment.getSectionId());
                    Course course = courseDao.findById(section.getCourseId());
                    
                    // find final grade
                    List<Grade> grades = gradeDao.listByEnrollment(enrollment.getEnrollmentId());
                    String finalGrade = "-";
                    
                    for (Grade grade : grades) {
                        if ("final".equalsIgnoreCase(grade.getComponent()) && 
                            grade.getFinalGrade() != null) {
                            finalGrade = grade.getFinalGrade();
                            break;
                        }
                    }
                    
                    // write row
                    writer.append(course.getCode()).append(",")
                          .append("\"").append(course.getTitle() != null ? course.getTitle() : "").append("\"").append(",")
                          .append(String.valueOf(course.getCredits() != null ? course.getCredits() : 0)).append(",")
                          .append(section.getSemester() != null ? section.getSemester() : "").append(",")
                          .append(String.valueOf(section.getYear() != null ? section.getYear() : 0)).append(",")
                          .append(finalGrade).append("\n");
                    
                } catch (Exception e) {
                    // skip missing data
                    System.err.println("Error processing enrollment " + enrollment.getEnrollmentId() + ": " + e.getMessage());
                }
            }
        }
    }
}

package edu.univ.erp.api.student;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.api.types.StudentGradeRow;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.ServiceRegistry;
import edu.univ.erp.service.StudentService;


public class StudentApi {
    private static final Logger logger = LoggerFactory.getLogger(StudentApi.class);
    private final StudentService studentService;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;

    public StudentApi() {
        this.studentService = ServiceRegistry.getStudentService();
        this.courseDao = new CourseDao();
        this.sectionDao = new SectionDao();
        this.enrollmentDao = new EnrollmentDao();
    }

    public ApiResponse<Void> registerSection(int studentUserId, int sectionId) {
        try {
            studentService.registerInSection(studentUserId, sectionId);
            return ApiResponse.ok((Void) null);
        } catch (IllegalStateException ex) {
            // Preserve existing error messages like "Section is full", "Already registered", etc.
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            // Preserve existing error messages like "Cannot change anything- Maintenance mode is enabled"
            return ApiResponse.error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error registering section", ex);
            return ApiResponse.error("Error registering section: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> dropSection(int studentUserId, int sectionId) {
        try {
            studentService.dropSection(studentUserId, sectionId);
            return ApiResponse.ok((Void) null);
        } catch (IllegalStateException ex) {
            // Preserve existing error messages like "You are not enrolled", "Drop deadline has passed"
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            // Preserve existing error messages like "Cannot change anything- Maintenance mode is enabled"
            return ApiResponse.error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error dropping section", ex);
            return ApiResponse.error("Error dropping section: " + ex.getMessage());
        }
    }

    public ApiResponse<List<SectionRow>> getRegisteredSections(int studentUserId) {
        try {
            List<Section> sections = studentService.listRegisteredSections(studentUserId);
            List<SectionRow> sectionRows = new ArrayList<>();
            
            for (Section section : sections) {
                SectionRow row = toSectionRow(section);
                sectionRows.add(row);
            }
            
            return ApiResponse.ok(sectionRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting registered sections", ex);
            return ApiResponse.error("Error getting registered sections: " + ex.getMessage());
        }
    }

    public ApiResponse<List<Section>> getRegisteredSectionsAsDomain(int studentUserId) {
        try {
            List<Section> sections = studentService.listRegisteredSections(studentUserId);
            return ApiResponse.ok(sections);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting registered sections", ex);
            return ApiResponse.error("Error getting registered sections: " + ex.getMessage());
        }
    }

    public ApiResponse<List<StudentGradeRow>> getGrades(int studentUserId) {
        try {
            List<Grade> grades = studentService.listGradesForStudent(studentUserId);
            
            Map<Integer, String[]> enrollmentInfo = new HashMap<>();
            List<Enrollment> enrollments = enrollmentDao.listByStudent(studentUserId);
            for (Enrollment enrollment : enrollments) {
                try {
                    Section section = sectionDao.findById(enrollment.getSectionId());
                    Course course = courseDao.findById(section.getCourseId());
                    enrollmentInfo.put(enrollment.getEnrollmentId(), 
                        new String[]{course.getCode(), String.valueOf(section.getSectionId())});
                } catch (Exception e) {
                }
            }
            
            List<StudentGradeRow> gradeRows = new ArrayList<>();
            for (Grade grade : grades) {
                String[] info = enrollmentInfo.get(grade.getEnrollmentId());
                String courseCode = info != null ? info[0] : "-";
                Integer sectionId = info != null ? Integer.parseInt(info[1]) : null;
                
                StudentGradeRow row = new StudentGradeRow();
                row.setCourseCode(courseCode);
                row.setSectionId(sectionId);
                row.setComponent(grade.getComponent());
                row.setScore(grade.getScore() != null ? grade.getScore().doubleValue() : null);
                row.setFinalGrade(grade.getFinalGrade());
                gradeRows.add(row);
            }
            
            return ApiResponse.ok(gradeRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting grades", ex);
            return ApiResponse.error("Error getting grades: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> downloadTranscriptCsv(int studentUserId, File targetFile) {
        try {
            studentService.exportTranscriptCsv(studentUserId, targetFile);
            return ApiResponse.ok((Void) null);
        } catch (IOException ex) {
            logger.error("Error exporting transcript CSV", ex);
            return ApiResponse.error("Error exporting transcript: " + ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error exporting transcript", ex);
            return ApiResponse.error("Error exporting transcript: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> downloadTranscriptPdf(int studentUserId, File targetFile) {
        // PDF export not yet implemented in service
        return ApiResponse.error("PDF transcript export is not yet implemented");
    }

    private SectionRow toSectionRow(Section section) {
        SectionRow row = new SectionRow();
        row.setSectionId(section.getSectionId());
        row.setCourseId(section.getCourseId());
        row.setDayTime(section.getDayTime());
        row.setRoom(section.getRoom());
        row.setCapacity(section.getCapacity());
        row.setSemester(section.getSemester());
        row.setYear(section.getYear());

        // Enrich with course information
        try {
            Course course = courseDao.findById(section.getCourseId());
            row.setCourseCode(course.getCode());
            row.setCourseTitle(course.getTitle());
            row.setCourseCredits(course.getCredits());
        } catch (Exception e) {
            row.setCourseCode("N/A");
            row.setCourseTitle("N/A");
            row.setCourseCredits(null);
        }

        return row;
    }
}


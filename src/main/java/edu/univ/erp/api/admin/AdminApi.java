package edu.univ.erp.api.admin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.domain.UserSummary;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.ServiceRegistry;

/**
 * API service layer for admin operations.
 
 */
public class AdminApi {
    private static final Logger logger = LoggerFactory.getLogger(AdminApi.class);
    private final AdminService adminService;
    private final MaintenanceService maintenanceService;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final AuthDao authDao;

    public AdminApi() {
        this.adminService = ServiceRegistry.getAdminService();
        this.maintenanceService = ServiceRegistry.getMaintenanceService();
        
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.authDao = new AuthDao();
    }

    // ========== User Management ==========

    
    public ApiResponse<UserSummary> createStudentUser(String username, String rawPassword, String rollNo, String program, int year) {
        try {
            adminService.createStudentUser(username, rawPassword, rollNo, program, year);
            
            // Get the created user to return as DTO
            UserAuth user = authDao.findByUsername(username).orElse(null);
            if (user != null) {
                UserSummary summary = new UserSummary();
                summary.setUserId(user.getUserId());
                summary.setUsername(user.getUsername());
                summary.setRole(user.getRole());
                summary.setFullName(null);
                summary.setExtraInfo("Roll: " + rollNo + ", Program: " + program + ", Year: " + year);
                return ApiResponse.ok(summary);
            }
            
            return ApiResponse.ok((UserSummary) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error creating student user", ex);
            return ApiResponse.error("Error creating student user: " + ex.getMessage());
        }
    }

    public ApiResponse<UserSummary> createInstructorUser(String username, String rawPassword, String department) {
        try {
            adminService.createInstructorUser(username, rawPassword, department);
            
            // Get the created user to return as DTO
            UserAuth user = authDao.findByUsername(username).orElse(null);
            if (user != null) {
                UserSummary summary = new UserSummary();
                summary.setUserId(user.getUserId());
                summary.setUsername(user.getUsername());
                summary.setRole(user.getRole());
                summary.setFullName(null);
                summary.setExtraInfo("Dept: " + department);
                return ApiResponse.ok(summary);
            }
            
            return ApiResponse.ok((UserSummary) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error creating instructor user", ex);
            return ApiResponse.error("Error creating instructor user: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> deleteUser(int userId) {
        try {
            adminService.deleteUser(userId);
            return ApiResponse.ok((Void) null);
        } catch (IllegalStateException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error deleting user", ex);
            return ApiResponse.error("Error deleting user: " + ex.getMessage());
        }
    }

    public ApiResponse<List<UserSummary>> listAllUsers() {
        try {
            List<UserSummary> users = adminService.listAllUsers();
            return ApiResponse.ok(users);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error listing users", ex);
            return ApiResponse.error("Error listing users: " + ex.getMessage());
        }
    }

    // ========== Course Management ==========

    public ApiResponse<CourseRow> createCourse(String code, String title, int credits) {
        try {
            int courseId = adminService.createCourse(code, title, credits);
            
            CourseRow row = new CourseRow();
            row.setCourseId(courseId);
            row.setCode(code.toUpperCase().trim());
            row.setTitle(title.trim());
            row.setCredits(credits);
            
            return ApiResponse.ok(row);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error creating course", ex);
            return ApiResponse.error("Error creating course: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> updateCourse(int courseId, String code, String title, int credits) {
        try {
            adminService.updateCourse(courseId, code, title, credits);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error updating course", ex);
            return ApiResponse.error("Error updating course: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> deleteCourse(int courseId, boolean force) {
        try {
            adminService.deleteCourse(courseId, force);
            return ApiResponse.ok((Void) null);
        } catch (RuntimeException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error deleting course", ex);
            return ApiResponse.error("Error deleting course: " + ex.getMessage());
        }
    }

    public ApiResponse<List<CourseRow>> listCourses() {
        try {
            List<Course> courses = adminService.listCourses();
            List<CourseRow> courseRows = new ArrayList<>();
            
            for (Course course : courses) {
                CourseRow row = new CourseRow();
                row.setCourseId(course.getCourseId());
                row.setCode(course.getCode());
                row.setTitle(course.getTitle());
                row.setCredits(course.getCredits());
                courseRows.add(row);
            }
            
            return ApiResponse.ok(courseRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error listing courses", ex);
            return ApiResponse.error("Error listing courses: " + ex.getMessage());
        }
    }

    // ========== Section Management ==========

    public ApiResponse<SectionRow> createSection(int courseId, Integer instructorUserId, String dayTime, String room, 
                                                 int capacity, String semester, int year) {
        try {
            int sectionId = adminService.createSection(courseId, instructorUserId, dayTime, room, capacity, semester, year);
            
            SectionRow row = new SectionRow();
            row.setSectionId(sectionId);
            row.setCourseId(courseId);
            row.setDayTime(dayTime);
            row.setRoom(room);
            row.setCapacity(capacity);
            row.setSemester(semester);
            row.setYear(year);
            
            // Enrich with course and instructor info
            try {
                Course course = courseDao.findById(courseId);
                row.setCourseCode(course.getCode());
                row.setCourseTitle(course.getTitle());
            } catch (Exception e) {
                row.setCourseCode("N/A");
                row.setCourseTitle("N/A");
            }
            
            if (instructorUserId != null) {
                try {
                    UserAuth instructor = authDao.findById(instructorUserId);
                    row.setInstructorName(instructor.getUsername());
                } catch (Exception e) {
                    row.setInstructorName("ID: " + instructorUserId);
                }
            } else {
                row.setInstructorName("None");
            }
            
            return ApiResponse.ok(row);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error creating section", ex);
            return ApiResponse.error("Error creating section: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> updateSection(int sectionId, int courseId, Integer instructorId, String dayTime,
                                          String room, int capacity, String semester, int year) {
        try {
            adminService.updateSection(sectionId, courseId, instructorId, dayTime, room, capacity, semester, year);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error updating section", ex);
            return ApiResponse.error("Error updating section: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> deleteSection(int sectionId, boolean force) {
        try {
            adminService.deleteSection(sectionId, force);
            return ApiResponse.ok((Void) null);
        } catch (RuntimeException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error deleting section", ex);
            return ApiResponse.error("Error deleting section: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> assignInstructor(int sectionId, int instructorUserId) {
        try {
            adminService.assignInstructorToSection(sectionId, instructorUserId);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error assigning instructor", ex);
            return ApiResponse.error("Error assigning instructor: " + ex.getMessage());
        }
    }

    public ApiResponse<List<SectionRow>> listAllSections() {
        try {
            List<Section> sections = adminService.listAllSections();
            List<SectionRow> sectionRows = new ArrayList<>();
            
            for (Section section : sections) {
                SectionRow row = toSectionRow(section);
                sectionRows.add(row);
            }
            
            return ApiResponse.ok(sectionRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error listing sections", ex);
            return ApiResponse.error("Error listing sections: " + ex.getMessage());
        }
    }

    // ========== Maintenance ==========

    public ApiResponse<Boolean> isMaintenanceOn() {
        try {
            boolean isOn = maintenanceService.isMaintenanceOn();
            return ApiResponse.ok(isOn);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error checking maintenance mode", ex);
            return ApiResponse.error("Error checking maintenance mode: " + ex.getMessage());
        }
    }

    public ApiResponse<Boolean> toggleMaintenance(boolean on) {
        try {
            edu.univ.erp.domain.UserAuth currentUser = edu.univ.erp.auth.SessionManager.getCurrentUser();
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                return ApiResponse.error("Access denied");
            }
            
            maintenanceService.setMaintenance(on, currentUser.getUserId());
            return ApiResponse.ok(on);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error toggling maintenance mode", ex);
            return ApiResponse.error("Error toggling maintenance mode: " + ex.getMessage());
        }
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

        // Enrich with instructor information
        if (section.getInstructorId() != null) {
            try {
                UserAuth instructor = authDao.findById(section.getInstructorId());
                row.setInstructorName(instructor.getUsername());
            } catch (Exception e) {
                row.setInstructorName("ID: " + section.getInstructorId());
            }
        } else {
            row.setInstructorName("None");
        }

        // Enrich with enrollment count
        try {
            int enrolled = enrollmentDao.countBySection(section.getSectionId());
            row.setEnrolled(enrolled);
            int capacity = section.getCapacity() != null ? section.getCapacity() : 0;
            row.setSeatsLeft(capacity - enrolled);
        } catch (Exception e) {
            row.setEnrolled(0);
            row.setSeatsLeft(0);
        }

        return row;
    }
}


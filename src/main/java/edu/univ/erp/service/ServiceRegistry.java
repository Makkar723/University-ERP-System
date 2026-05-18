package edu.univ.erp.service;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.data.dao.GradeDao;
import edu.univ.erp.data.dao.InstructorDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.data.dao.SettingsDao;
import edu.univ.erp.data.dao.StudentDao;

/**
 * Central registry for service instances and DAOs.
 * Provides singleton access to all services.
 */
public class ServiceRegistry {
    // DAOs
    private static final AuthDao authDao = new AuthDao();
    private static final StudentDao studentDao = new StudentDao();
    private static final InstructorDao instructorDao = new InstructorDao();
    private static final CourseDao courseDao = new CourseDao();
    private static final SectionDao sectionDao = new SectionDao();
    private static final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private static final GradeDao gradeDao = new GradeDao();
    private static final SettingsDao settingsDao = new SettingsDao();
    
    // Services - MaintenanceService must be created first as other services depend on it
    private static final MaintenanceService maintenanceService = new MaintenanceService(settingsDao);
    private static final AuthService authService = new AuthService(authDao);
    private static final AdminService adminService = new AdminService(
        authDao, studentDao, instructorDao, courseDao, sectionDao, settingsDao,
        enrollmentDao, gradeDao);
    private static final StudentService studentService = new StudentService(
        enrollmentDao, sectionDao, courseDao, gradeDao, maintenanceService);
    private static final InstructorService instructorService = new InstructorService(
        sectionDao, enrollmentDao, studentDao, gradeDao, maintenanceService, authDao);
    private static final CatalogService catalogService = new CatalogService(courseDao, sectionDao);
    
    // Getters for services
    public static AuthService getAuthService() {
        return authService;
    }
    
    public static AdminService getAdminService() {
        return adminService;
    }
    
    public static StudentService getStudentService() {
        return studentService;
    }
    
    public static InstructorService getInstructorService() {
        return instructorService;
    }
    
    public static CatalogService getCatalogService() {
        return catalogService;
    }
    
    public static MaintenanceService getMaintenanceService() {
        return maintenanceService;
    }
}


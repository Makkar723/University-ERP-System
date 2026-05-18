package edu.univ.erp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mindrot.jbcrypt.BCrypt;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.data.dao.GradeDao;
import edu.univ.erp.data.dao.InstructorDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.data.dao.SettingsDao;
import edu.univ.erp.data.dao.StudentDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.domain.UserSummary;
import edu.univ.erp.util.Validators;

public class AdminService {
    private final AuthDao authDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final SettingsDao settingsDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;
    
    public AdminService(AuthDao authDao, StudentDao studentDao, InstructorDao instructorDao,
                       CourseDao courseDao, SectionDao sectionDao, SettingsDao settingsDao,
                       EnrollmentDao enrollmentDao, GradeDao gradeDao) {
        this.authDao = authDao;
        this.studentDao = studentDao;
        this.instructorDao = instructorDao;
        this.courseDao = courseDao;
        this.sectionDao = sectionDao;
        this.settingsDao = settingsDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeDao = gradeDao;
    }
    
    public void createStudentUser(String username, String rawPassword, String rollNo, String program, int year) {
        AccessController.requireAdmin();
        
        if (username == null || username.trim().isEmpty() ||
            rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        if (program == null || program.trim().isEmpty()) {
            throw new IllegalArgumentException("Program/branch is required.");
        }

        String normalizedProgram = program.trim().toUpperCase(Locale.ROOT);
        Validators.validateStudentBranch(normalizedProgram);
        
        if (authDao.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        
        UserAuth userAuth = new UserAuth();
        userAuth.setUsername(username.trim());
        userAuth.setRole("student");
        userAuth.setPasswordHash(hashedPassword);
        userAuth.setStatus("active");
        
        int userId;
        try {
            userId = authDao.createUser(userAuth);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create auth record for student: " + e.getMessage(), e);
        }
        
        try {
            Student student = new Student();
            student.setUserId(userId);
            student.setRollNo(rollNo != null && !rollNo.trim().isEmpty() ? rollNo.trim() : "STU-" + userId);
            student.setProgram(normalizedProgram);
            student.setYear(year > 0 && year <= 4 ? year : 1);
            studentDao.insertStudent(student);
        } catch (Exception e) {
            try {
                authDao.deleteUser(userId);
            } catch (Exception cleanupEx) {
                System.err.println("Failed to cleanup auth record: " + cleanupEx.getMessage());
            }
            throw new RuntimeException("Failed to create student profile: " + e.getMessage() + 
                                     ". Auth record has been cleaned up.", e);
        }
    }
    
    public void createInstructorUser(String username, String rawPassword, String department) {
        AccessController.requireAdmin();
        
        if (username == null || username.trim().isEmpty() ||
            rawPassword == null || rawPassword.isEmpty() ||
            department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Username, password, and department are required.");
        }

        String normalizedDepartment = department.trim().toUpperCase(Locale.ROOT);
        Validators.validateInstructorDept(normalizedDepartment);
        
        if (authDao.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        
        UserAuth userAuth = new UserAuth();
        userAuth.setUsername(username.trim());
        userAuth.setRole("instructor");
        userAuth.setPasswordHash(hashedPassword);
        userAuth.setStatus("active");
        
        int userId;
        try {
            userId = authDao.createUser(userAuth);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create auth record for instructor: " + e.getMessage(), e);
        }
        
        try {
            Instructor instructor = new Instructor();
            instructor.setUserId(userId);
            instructor.setDepartment(normalizedDepartment);
            instructorDao.insertInstructor(instructor);
        } catch (Exception e) {
            try {
                authDao.deleteUser(userId);
            } catch (Exception cleanupEx) {
                System.err.println("Failed to cleanup auth record: " + cleanupEx.getMessage());
            }
            throw new RuntimeException("Failed to create instructor profile: " + e.getMessage() + 
                                     ". Auth record has been cleaned up.", e);
        }
    }
    
    public int createCourse(String code, String title, int credits) {
        AccessController.requireAdmin();
        
        if (code == null || code.trim().isEmpty() ||
            title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code and title are required.");
        }
        
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive.");
        }

        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        Validators.validateCourseCode(normalizedCode);
        
        if (courseDao.findByCode(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Course code already exists: " + normalizedCode);
        }
        
        Course course = new Course();
        course.setCode(normalizedCode);
        course.setTitle(title.trim());
        course.setCredits(credits);
        
        return courseDao.createCourse(course);
    }
    
    public int createSection(int courseId, Integer instructorUserId, String dayTime, String room, 
                            int capacity, String semester, int year) {
        AccessController.requireAdmin();
        
        if (dayTime == null || dayTime.trim().isEmpty() ||
            room == null || room.trim().isEmpty() ||
            semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Day/time, room, and semester are required.");
        }
        
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        
        courseDao.findById(courseId);
        
        if (instructorUserId != null) {
            authDao.findById(instructorUserId);
        }
        
        Section section = new Section();
        section.setCourseId(courseId);
        section.setInstructorId(instructorUserId);
        section.setDayTime(dayTime.trim());
        section.setRoom(room.trim());
        section.setCapacity(capacity);
        section.setSemester(semester.trim());
        section.setYear(year);
        
        return sectionDao.createSection(section);
    }
    
    public void assignInstructorToSection(int sectionId, int instructorUserId) {
        AccessController.requireAdmin();
        
        sectionDao.findById(sectionId);
        
        authDao.findById(instructorUserId);
        
        sectionDao.updateInstructor(sectionId, instructorUserId);
    }
    
    public boolean isMaintenanceOn() {
        AccessController.requireAdmin();
        
        return settingsDao.getBoolean("maintenance", false);
    }
    
    public void setMaintenance(boolean on) {
        AccessController.requireAdmin();
        
        settingsDao.set("maintenance", on ? "true" : "false");
    }
    
    public void updateCourse(int courseId, String code, String title, int credits) {
        AccessController.requireAdmin();
        AccessController.requireNotInMaintenance(isMaintenanceOn());

        if (code == null || code.trim().isEmpty() ||
            title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code and title are required.");
        }

        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive.");
        }

        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        Validators.validateCourseCode(normalizedCode);
        
        Course existingCourse = courseDao.findById(courseId);
        
        if (!normalizedCode.equals(existingCourse.getCode())) {
            java.util.Optional<Course> duplicate = courseDao.findByCode(normalizedCode);
            if (duplicate.isPresent() && !duplicate.get().getCourseId().equals(courseId)) {
                throw new IllegalArgumentException("Course code already exists: " + normalizedCode);
            }
        }
        
        existingCourse.setCode(normalizedCode);
        existingCourse.setTitle(title.trim());
        existingCourse.setCredits(credits);
        
        courseDao.updateCourse(existingCourse);
    }
    
    public void deleteCourse(int courseId, boolean force) {
        AccessController.requireAdmin();
        AccessController.requireNotInMaintenance(isMaintenanceOn());
        
        Course course = courseDao.findById(courseId);
        
        int sectionCount = courseDao.countSections(courseId);
        if (sectionCount > 0 && !force) {
            throw new RuntimeException("Cannot delete course: " + course.getCode() + 
                ". " + sectionCount + " section(s) exist. Delete sections first or use force delete.");
        }
        
        if (force && sectionCount > 0) {
            List<Section> sections = sectionDao.listSectionsForCourse(courseId);
            for (Section section : sections) {
                List<Enrollment> enrollments = enrollmentDao.listBySection(section.getSectionId());
                for (Enrollment enrollment : enrollments) {
                    gradeDao.deleteByEnrollment(enrollment.getEnrollmentId());
                }
                enrollmentDao.deleteBySectionId(section.getSectionId());
            }
        }
        
        courseDao.deleteCourse(courseId);
    }
    
    public void deregisterStudentsFromCourse(int courseId) {
        AccessController.requireAdmin();
        AccessController.requireNotInMaintenance(isMaintenanceOn());
        
        courseDao.findById(courseId);
        
        enrollmentDao.deleteByCourseId(courseId);
    }
    
    public void updateSection(int sectionId, int courseId, Integer instructorId, String dayTime,
                              String room, int capacity, String semester, int year) {
        AccessController.requireAdmin();
        AccessController.requireNotInMaintenance(isMaintenanceOn());
        
        if (dayTime == null || dayTime.trim().isEmpty() ||
            room == null || room.trim().isEmpty() ||
            semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Day/time, room, and semester are required.");
        }
        
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative.");
        }
        
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100.");
        }
        
        Section section = sectionDao.findById(sectionId);
        
        courseDao.findById(courseId);
        
        if (instructorId != null) {
            authDao.findById(instructorId);
        }
        
        int enrolledCount = sectionDao.countEnrollments(sectionId);
        if (capacity < enrolledCount) {
            throw new IllegalArgumentException(
                "Cannot reduce capacity below currently enrolled students (" + enrolledCount + "). " +
                "Increase capacity or drop students first.");
        }
        
        try {
            edu.univ.erp.util.SectionScheduleUtil.parseSchedule(dayTime.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid schedule format: " + e.getMessage());
        }
        
        section.setCourseId(courseId);
        section.setInstructorId(instructorId);
        section.setDayTime(dayTime.trim());
        section.setRoom(room.trim());
        section.setCapacity(capacity);
        section.setSemester(semester.trim());
        section.setYear(year);
        
        sectionDao.updateSection(section);
    }
    
    public void deleteSection(int sectionId, boolean force) {
        AccessController.requireAdmin();
        AccessController.requireNotInMaintenance(isMaintenanceOn());
        
        // Ensure section exists
        Section section = sectionDao.findById(sectionId);
        
        // Check enrolled count
        int enrollmentCount = sectionDao.countEnrollments(sectionId);
        if (enrollmentCount > 0 && !force) {
            throw new RuntimeException("Section has enrolled students (" + enrollmentCount + 
                "); cannot delete. Drop students first or use force delete.");
        }
        
        if (force && enrollmentCount > 0) {
            // Cascade delete data
            List<Enrollment> enrollments = enrollmentDao.listBySection(sectionId);
            for (Enrollment enrollment : enrollments) {
                gradeDao.deleteByEnrollment(enrollment.getEnrollmentId());
            }
            enrollmentDao.deleteBySectionId(sectionId);
        }
        
        sectionDao.deleteSection(sectionId);
    }
    
    /** Deregister section students */
    public void deregisterStudentsFromSection(int sectionId) {
        AccessController.requireAdmin();
        AccessController.requireNotInMaintenance(isMaintenanceOn());
        
        // Ensure section exists
        sectionDao.findById(sectionId);
        
        enrollmentDao.deleteBySectionId(sectionId);
    }
    
    public List<Enrollment> getEnrollmentsForSection(int sectionId) {
        AccessController.requireAdmin();
        
        return enrollmentDao.listBySection(sectionId);
    }
    
    public List<Enrollment> getEnrollmentsForCourse(int courseId) {
        AccessController.requireAdmin();
        
        List<Enrollment> allEnrollments = new ArrayList<>();
        List<Section> sections = sectionDao.listSectionsByCourse(courseId);
        
        for (Section section : sections) {
            allEnrollments.addAll(enrollmentDao.listBySection(section.getSectionId()));
        }
        
        return allEnrollments;
    }
    
    /** List all courses */
    public List<Course> listCourses() {
        AccessController.requireAdmin();
        
        return courseDao.listCourses();
    }
    
    /** List all sections */
    public List<Section> listAllSections() {
        AccessController.requireAdmin();
        
        return sectionDao.listAllSections();
    }
    
    /** List user summaries */
    public List<UserSummary> listAllUsers() {
        AccessController.requireAdmin();
        
        List<UserSummary> summaries = new ArrayList<>();
        List<UserAuth> allUsers = authDao.findAll();
        
        for (UserAuth user : allUsers) {
            UserSummary summary = new UserSummary();
            summary.setUserId(user.getUserId());
            summary.setUsername(user.getUsername());
            summary.setRole(user.getRole());
            summary.setFullName(null); // We don't have a name field, use username as fallback
            
            String role = user.getRole();
            if ("student".equals(role)) {
                studentDao.findByUserId(user.getUserId()).ifPresent(student -> {
                    summary.setExtraInfo(String.format("Roll: %s, Program: %s, Year: %d",
                        student.getRollNo(), student.getProgram(), student.getYear()));
                });
            } else if ("instructor".equals(role)) {
                instructorDao.findByUserId(user.getUserId()).ifPresent(instructor -> {
                    summary.setExtraInfo("Dept: " + instructor.getDepartment());
                });
            } else if ("admin".equals(role)) {
                summary.setExtraInfo("Admin");
            }
            
            // Default extra info
            if (summary.getExtraInfo() == null) {
                summary.setExtraInfo("-");
            }
            
            summaries.add(summary);
        }
        
        return summaries;
    }
    
    /** Delete user and data */
    public void deleteUser(int userId) {
        AccessController.requireAdmin();
        
        // Fetch user role
        UserAuth user = authDao.findById(userId);
        String role = user.getRole();
        
        // Block deleting self
        UserAuth currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getUserId().equals(userId) && "admin".equals(role)) {
            throw new IllegalStateException("Cannot delete the currently logged-in admin user.");
        }
        
        if ("student".equals(role)) {
            // Delete dependent records
            gradeDao.deleteByStudentId(userId);
            enrollmentDao.deleteByStudentId(userId);
            
            // Delete student profile
            studentDao.deleteByUserId(userId);
            
            // Remove from auth
            authDao.deleteUserById(userId);
            
        } else if ("instructor".equals(role)) {
            // Check assigned sections
            int sectionCount = sectionDao.countByInstructor(userId);
            if (sectionCount > 0) {
                throw new IllegalStateException(
                    "Cannot delete instructor assigned to " + sectionCount + " section(s). " +
                    "Reassign or remove sections first.");
            }
            
            // Delete instructor profile
            instructorDao.deleteByUserId(userId);
            
            // Remove from auth
            authDao.deleteUserById(userId);
            
        } else if ("admin".equals(role)) {
            // Delete admin auth only
            authDao.deleteUserById(userId);
            
        } else {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}

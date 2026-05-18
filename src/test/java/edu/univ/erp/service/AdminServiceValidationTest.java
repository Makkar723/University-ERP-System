package edu.univ.erp.service;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminServiceValidationTest {

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(null, null, null, null, null, null, null, null);
        UserAuth admin = new UserAuth();
        admin.setUserId(1);
        admin.setRole("admin");
        SessionManager.setCurrentUser(admin);
    }

    @AfterEach
    void tearDown() {
        SessionManager.clear();
    }

    @Test
    void createCourseRejectsInvalidCode() {
        assertThrows(IllegalArgumentException.class,
            () -> adminService.createCourse("ABC123", "Test Course", 3));
    }

    @Test
    void createInstructorUserRejectsInvalidDepartment() {
        assertThrows(IllegalArgumentException.class,
            () -> adminService.createInstructorUser("inst1", "password", "MKT"));
    }

    @Test
    void createStudentUserRejectsInvalidProgram() {
        assertThrows(IllegalArgumentException.class,
            () -> adminService.createStudentUser("stud1", "password", "RN-1", "MECH", 1));
    }
}


package edu.univ.erp.access;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.MaintenanceService;

// role based access control
public final class AccessController {
    
    // require user logged in
    public static void requireLoggedIn() {
        UserAuth user = SessionManager.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("You must be logged in.");
        }
    }
    
    // require admin role
    public static void requireAdmin() {
        requireLoggedIn();
        UserAuth user = SessionManager.getCurrentUser();
        if (!"admin".equals(user.getRole())) {
            throw new AccessDeniedException("Admin access required.");
        }
    }
    
    // require student role
    public static void requireStudent() {
        requireLoggedIn();
        UserAuth user = SessionManager.getCurrentUser();
        if (!"student".equals(user.getRole())) {
            throw new AccessDeniedException("Student access required.");
        }
    }
    
    // require instructor role
    public static void requireInstructor() {
        requireLoggedIn();
        UserAuth user = SessionManager.getCurrentUser();
        if (!"instructor".equals(user.getRole())) {
            throw new AccessDeniedException("Instructor access required.");
        }
    }
    
    // check maintenance not on
    public static void requireNotInMaintenance(MaintenanceService maintenanceService) {
        if (maintenanceService.isMaintenanceOn()) {
            throw new AccessDeniedException("Cannot change anything- Maintenance mode is enabled");
        }
    }
    
    // legacy maintenance check
    @Deprecated
    public static void requireNotInMaintenance(boolean maintenanceOn) {
        if (maintenanceOn) {
            throw new AccessDeniedException("Cannot change anything- Maintenance mode is enabled");
        }
    }
    
    // check instructor owns section
    public static void requireInstructorOwnsSection(int sectionId, SectionDao sectionDao) {
        requireInstructor();
        int currentId = SessionManager.getCurrentUser().getUserId();
        boolean owns = sectionDao.instructorOwnsSection(currentId, sectionId);
        if (!owns) {
            throw new AccessDeniedException("Not your section.");
        }
    }
}



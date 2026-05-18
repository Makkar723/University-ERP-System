package edu.univ.erp.api.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.ServiceRegistry;

public class MaintenanceApi {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceApi.class);
    private final MaintenanceService maintenanceService;

    public MaintenanceApi() {
        this.maintenanceService = ServiceRegistry.getMaintenanceService();
    }

    public ApiResponse<Boolean> isMaintenanceOn() {
        try {
            boolean isOn = maintenanceService.isMaintenanceOn();
            return ApiResponse.ok(isOn);
        } catch (Exception ex) {
            logger.error("Unexpected error checking maintenance mode", ex);
            return ApiResponse.error("Error checking maintenance mode: " + ex.getMessage());
        }
    }

    public ApiResponse<Boolean> setMaintenance(boolean on) {
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
            logger.error("Unexpected error setting maintenance mode", ex);
            return ApiResponse.error("Error setting maintenance mode: " + ex.getMessage());
        }
    }

    public boolean isReadOnlyNow() {
        try {
            return maintenanceService.isMaintenanceOn();
        } catch (Exception ex) {
            logger.error("Error checking read-only mode", ex);
            return false; // Default to allowing operations if check fails
        }
    }
}



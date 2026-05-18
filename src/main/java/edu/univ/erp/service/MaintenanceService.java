package edu.univ.erp.service;

import edu.univ.erp.data.dao.SettingsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for maintenance mode operations.
 */
public class MaintenanceService {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final SettingsDao settingsDao;
    private volatile boolean maintenanceCache;
    private volatile long cacheTimestamp;
    private static final long CACHE_TTL_MS = 1000; // 1 second cache
    
    public MaintenanceService(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
        this.maintenanceCache = false;
        this.cacheTimestamp = 0;
    }
    
    /**
     * Check if maintenance mode is enabled.
     * Uses a short-lived cache to avoid constant DB reads.
     */
    public boolean isMaintenanceOn() {
        long now = System.currentTimeMillis();
        if (now - cacheTimestamp < CACHE_TTL_MS) {
            return maintenanceCache;
        }
        
        boolean value = settingsDao.getBoolean("maintenance", false);
        maintenanceCache = value;
        cacheTimestamp = now;
        return value;
    }
    
    /**
     * Set maintenance mode on or off.
     * @param on true to enable maintenance mode, false to disable
     * @param adminUserId the user ID of the admin making the change (for logging)
     */
    public void setMaintenance(boolean on, int adminUserId) {
        settingsDao.set("maintenance", on ? "true" : "false");
        
        // Update cache immediately
        maintenanceCache = on;
        cacheTimestamp = System.currentTimeMillis();
        
        // Log the change
        logger.info("Maintenance mode toggled to {} by userId={}", on, adminUserId);
    }
    
    /**
     * Invalidate cache (force next read to go to DB).
     */
    public void invalidateCache() {
        cacheTimestamp = 0;
    }
}



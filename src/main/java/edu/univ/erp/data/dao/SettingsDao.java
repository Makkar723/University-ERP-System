package edu.univ.erp.data.dao;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettingsDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public String get(String key) {
        String sql = "SELECT v FROM settings WHERE k = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, key);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("v");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting setting: " + key, e);
        }
        
        return null;
    }
    
    public void set(String key, String value) {
        String sql = "INSERT INTO settings (k, v) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE v = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error setting value for key: " + key, e);
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value);
    }
            
    public Map<String, String> getAllSettings() {
        String sql = "SELECT k, v FROM settings";
        Map<String, String> settings = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                settings.put(rs.getString("k"), rs.getString("v"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all settings", e);
        }
        
        return settings;
    }
}




package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class DataSourceFactory {
    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;
    
    static {
        initializeDataSources();
    }
    
    private static void initializeDataSources() {
        try {
            Properties props = loadProperties();
            
            // Configure auth database connection pool
            HikariConfig authConfig = new HikariConfig();
            authConfig.setJdbcUrl(props.getProperty("auth.db.url"));
            authConfig.setUsername(props.getProperty("auth.db.user"));
            authConfig.setPassword(props.getProperty("auth.db.password"));
            authConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            authConfig.setMaximumPoolSize(10);
            authConfig.setMinimumIdle(2);
            authConfig.setConnectionTimeout(30000);
            authConfig.setIdleTimeout(600000);
            authConfig.setMaxLifetime(1800000);
            authDataSource = new HikariDataSource(authConfig);
            
            // Configure ERP database connection pool
            HikariConfig erpConfig = new HikariConfig();
            erpConfig.setJdbcUrl(props.getProperty("erp.db.url"));
            erpConfig.setUsername(props.getProperty("erp.db.user"));
            erpConfig.setPassword(props.getProperty("erp.db.password"));
            erpConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            erpConfig.setMaximumPoolSize(10);
            erpConfig.setMinimumIdle(2);
            erpConfig.setConnectionTimeout(30000);
            erpConfig.setIdleTimeout(600000);
            erpConfig.setMaxLifetime(1800000);
            erpDataSource = new HikariDataSource(erpConfig);
            
        } catch (Exception e) {
            System.err.println("Error initializing database connections: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Properties loadProperties() throws Exception {
        Properties props = new Properties();
        try (InputStream is = DataSourceFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new Exception("application.properties not found");
            }
            props.load(is);
        }
        return props;
    }
    
    public static DataSource getAuthDataSource() {
        if (authDataSource == null) {
            throw new IllegalStateException("Auth database connection pool not initialized");
        }
        return authDataSource;
    }
    
    public static DataSource getERPDataSource() {
        if (erpDataSource == null) {
            throw new IllegalStateException("ERP database connection pool not initialized");
        }
        return erpDataSource;
    }
    
    public static boolean testConnections() {
        try {
            try (Connection authConn = getAuthDataSource().getConnection()) {
                try (var stmt = authConn.prepareStatement("SELECT 1")) {
                    stmt.executeQuery();
                }
            }
            
            try (Connection erpConn = getERPDataSource().getConnection()) {
                try (var stmt = erpConn.prepareStatement("SELECT 1")) {
                    stmt.executeQuery();
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    public static void closeAll() {
        if (authDataSource != null) {
            authDataSource.close();
        }
        if (erpDataSource != null) {
            erpDataSource.close();
        }
    }
}




package edu.univ.erp.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.UserAuth;

public class AuthDao {
    private static final DataSource dataSource = DataSourceFactory.getAuthDataSource();
    
    public Optional<UserAuth> findByUsername(String username) {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login " +
                     "FROM users_auth WHERE username = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by username: " + username, e);
        }
        
        return Optional.empty();
    }
    
    public UserAuth findById(int userId) {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login " +
                     "FROM users_auth WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by ID: " + userId, e);
        }
        
        throw new DataAccessException("User not found with ID: " + userId);
    }
    
    public int createUser(UserAuth user) {
        if (user.getUserId() != null) {
            // Insert with explicit user_id
            String sql = "INSERT INTO users_auth (user_id, username, role, password_hash, status) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, user.getUserId());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getRole());
                stmt.setString(4, user.getPasswordHash());
                stmt.setString(5, user.getStatus() != null ? user.getStatus() : "active");
                
                stmt.executeUpdate();
                return user.getUserId();
            } catch (SQLException e) {
                throw new DataAccessException("Error creating user with explicit ID", e);
            }
        } else {
            // Insert without user_id (auto-increment)
            String sql = "INSERT INTO users_auth (username, role, password_hash, status) " +
                        "VALUES (?, ?, ?, ?)";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getRole());
                stmt.setString(3, user.getPasswordHash());
                stmt.setString(4, user.getStatus() != null ? user.getStatus() : "active");
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
                
                throw new DataAccessException("Failed to retrieve generated user_id");
            } catch (SQLException e) {
                throw new DataAccessException("Error creating user", e);
            }
        }
    }
    
    public void updateLastLogin(int userId, LocalDateTime when) {
        String sql = "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (when != null) {
                stmt.setObject(1, when);
            } else {
                stmt.setObject(1, null);
            }
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating last login for user: " + userId, e);
        }
    }
    
    public void updatePasswordHash(int userId, String newHash) {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating password hash for user: " + userId, e);
        }
    }
    
    public List<UserAuth> findAll() {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login " +
                     "FROM users_auth ORDER BY username";
        List<UserAuth> users = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all users", e);
        }
        
        return users;
    }
    
    public void deleteUser(int userId) {
        deleteUserById(userId);
    }
    
    public void deleteUserById(int userId) {
        String sql = "DELETE FROM users_auth WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user: " + userId, e);
        }
    }
    
    private UserAuth mapRow(ResultSet rs) throws SQLException {
        UserAuth user = new UserAuth();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setStatus(rs.getString("status"));
        
        java.sql.Timestamp lastLoginTs = rs.getTimestamp("last_login");
        if (lastLoginTs != null) {
            user.setLastLogin(lastLoginTs.toLocalDateTime());
        }
        
        return user;
    }
}



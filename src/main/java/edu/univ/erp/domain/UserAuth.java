package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class UserAuth {
    private Integer userId;
    private String username;
    private String role;
    private String passwordHash;
    private String status;
    private LocalDateTime lastLogin;
    
    public UserAuth() {
    }
    
    public UserAuth(Integer userId, String username, String role, String passwordHash, String status, LocalDateTime lastLogin) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.status = status;
        this.lastLogin = lastLogin;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    @Override
    public String toString() {
        return "UserAuth{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", lastLogin=" + lastLogin +
                '}';
    }
}




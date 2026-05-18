package edu.univ.erp.domain;

public class UserSummary {
    private int userId;
    private String username;
    private String role;        
    private String fullName;    
    private String extraInfo;   
    
    public UserSummary() {
    }
    
    public UserSummary(int userId, String username, String role, String fullName, String extraInfo) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.extraInfo = extraInfo;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
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
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getExtraInfo() {
        return extraInfo;
    }
    
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }
    
    @Override
    public String toString() {
        return "UserSummary{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", extraInfo='" + extraInfo + '\'' +
                '}';
    }
}



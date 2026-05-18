package edu.univ.erp.domain;

public class Instructor {
    private Integer userId;
    private String department;
    
    public Instructor() {
    }
    
    public Instructor(Integer userId, String department) {
        this.userId = userId;
        this.department = department;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    @Override
    public String toString() {
        return "Instructor{" +
                "userId=" + userId +
                ", department='" + department + '\'' +
                '}';
    }
}




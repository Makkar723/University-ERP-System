package edu.univ.erp.domain;


public class Student {
    private Integer userId;
    private String rollNo;
    private String program;
    private Integer year;
    
    public Student() {
    }
    
    public Student(Integer userId, String rollNo, String program, Integer year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getRollNo() {
        return rollNo;
    }
    
    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        this.program = program;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "userId=" + userId +
                ", rollNo='" + rollNo + '\'' +
                ", program='" + program + '\'' +
                ", year=" + year +
                '}';
    }
}




package edu.univ.erp.domain;

import java.util.ArrayList;
import java.util.List;


public class StudentEnrollmentRow {
    private int enrollmentId;
    private int studentUserId;
    private String studentUsername;
    private String studentRollNo;
    private String studentProgram;
    private List<Grade> grades;
    
    public StudentEnrollmentRow() {
    }
    
    public StudentEnrollmentRow(int enrollmentId, int studentUserId, String studentUsername, 
                                String studentRollNo, String studentProgram) {
        this.enrollmentId = enrollmentId;
        this.studentUserId = studentUserId;
        this.studentUsername = studentUsername;
        this.studentRollNo = studentRollNo;
        this.studentProgram = studentProgram;
    }
    
    public int getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public int getStudentUserId() {
        return studentUserId;
    }
    
    public void setStudentUserId(int studentUserId) {
        this.studentUserId = studentUserId;
    }
    
    public String getStudentUsername() {
        return studentUsername;
    }
    
    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }
    
    public String getStudentRollNo() {
        return studentRollNo;
    }
    
    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }
    
    public String getStudentProgram() {
        return studentProgram;
    }
    
    public void setStudentProgram(String studentProgram) {
        this.studentProgram = studentProgram;
    }
    
    public List<Grade> getGrades() {
        if (grades == null) {
            grades = new ArrayList<>();
        }
        return grades;
    }
    
    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }
    
    @Override
    public String toString() {
        return "StudentEnrollmentRow{" +
                "enrollmentId=" + enrollmentId +
                ", studentUserId=" + studentUserId +
                ", studentUsername='" + studentUsername + '\'' +
                ", studentRollNo='" + studentRollNo + '\'' +
                ", studentProgram='" + studentProgram + '\'' +
                '}';
    }
}


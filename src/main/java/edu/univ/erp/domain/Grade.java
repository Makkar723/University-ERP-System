package edu.univ.erp.domain;

import java.math.BigDecimal;

/**
 * Domain model for grade data.
 */
public class Grade {
    private Integer gradeId;
    private Integer enrollmentId;
    private String component;
    private BigDecimal score;
    private String finalGrade;
    
    public Grade() {
    }
    
    public Grade(Integer gradeId, Integer enrollmentId, String component, BigDecimal score, String finalGrade) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }
    
    public Integer getGradeId() {
        return gradeId;
    }
    
    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
    
    public Integer getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(Integer enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public String getComponent() {
        return component;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public String getFinalGrade() {
        return finalGrade;
    }
    
    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }
    
    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", enrollmentId=" + enrollmentId +
                ", component='" + component + '\'' +
                ", score=" + score +
                ", finalGrade='" + finalGrade + '\'' +
                '}';
    }
}




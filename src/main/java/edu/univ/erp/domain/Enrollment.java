package edu.univ.erp.domain;

public class Enrollment {
    private Integer enrollmentId;
    private Integer studentId;
    private Integer sectionId;
    private String status;
    
    public Enrollment() {
    }
    
    public Enrollment(Integer enrollmentId, Integer studentId, Integer sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }
    
    public Integer getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(Integer enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public Integer getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", sectionId=" + sectionId +
                ", status='" + status + '\'' +
                '}';
    }
}




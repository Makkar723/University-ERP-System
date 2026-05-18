package edu.univ.erp.api.types;


public class EnrollmentRow {
    private Integer enrollmentId;
    private String courseCode;
    private Integer sectionId;
    private String status;
    private String gradeSummary;

    public EnrollmentRow() {
    }

    public EnrollmentRow(Integer enrollmentId, String courseCode, Integer sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.courseCode = courseCode;
        this.sectionId = sectionId;
        this.status = status;
    }

    public Integer getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Integer enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
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

    public String getGradeSummary() {
        return gradeSummary;
    }

    public void setGradeSummary(String gradeSummary) {
        this.gradeSummary = gradeSummary;
    }
}



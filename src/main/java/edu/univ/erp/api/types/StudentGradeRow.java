package edu.univ.erp.api.types;


public class StudentGradeRow {
    private String courseCode;
    private Integer sectionId;
    private String component;
    private Double score;
    private String finalGrade;

    public StudentGradeRow() {
    }

    public StudentGradeRow(String courseCode, Integer sectionId, String component) {
        this.courseCode = courseCode;
        this.sectionId = sectionId;
        this.component = component;
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

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }
}



package edu.univ.erp.api.types;


public class CourseRow {
    private Integer courseId;
    private String code;
    private String title;
    private Integer credits;
    private String instructorName;
    private Integer sectionCount;

    public CourseRow() {
    }

    public CourseRow(Integer courseId, String code, String title, Integer credits) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public Integer getSectionCount() {
        return sectionCount;
    }

    public void setSectionCount(Integer sectionCount) {
        this.sectionCount = sectionCount;
    }
}



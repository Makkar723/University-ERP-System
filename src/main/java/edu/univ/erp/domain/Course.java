package edu.univ.erp.domain;

public class Course {
    private Integer courseId;
    private String code;
    private String title;
    private Integer credits;
    
    public Course() {
    }
    
    public Course(Integer courseId, String code, String title, Integer credits) {
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
    
    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", credits=" + credits +
                '}';
    }
}




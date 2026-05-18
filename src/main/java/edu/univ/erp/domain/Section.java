package edu.univ.erp.domain;

import edu.univ.erp.util.SectionScheduleUtil;

import java.util.List;

public class Section {
    private Integer sectionId;
    private Integer courseId;
    private Integer instructorId;
    private String dayTime;
    private String room;
    private Integer capacity;
    private String semester;
    private Integer year;
    
    public Section() {
    }
    
    public Section(Integer sectionId, Integer courseId, Integer instructorId, String dayTime, 
                   String room, Integer capacity, String semester, Integer year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }
    
    public Integer getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }
    
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public Integer getInstructorId() {
        return instructorId;
    }
    
    public void setInstructorId(Integer instructorId) {
        this.instructorId = instructorId;
    }
    
    public String getDayTime() {
        return dayTime;
    }
    
    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public List<SectionMeeting> getMeetings() {
        return SectionScheduleUtil.parseSchedule(dayTime);
    }
    
    public void setMeetings(List<SectionMeeting> meetings) {
        this.dayTime = SectionScheduleUtil.formatSchedule(meetings);
    }
    
    @Override
    public String toString() {
        return "Section{" +
                "sectionId=" + sectionId +
                ", courseId=" + courseId +
                ", instructorId=" + instructorId +
                ", dayTime='" + dayTime + '\'' +
                ", room='" + room + '\'' +
                ", capacity=" + capacity +
                ", semester='" + semester + '\'' +
                ", year=" + year +
                '}';
    }
}



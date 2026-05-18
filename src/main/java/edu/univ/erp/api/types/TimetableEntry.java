package edu.univ.erp.api.types;


public class TimetableEntry {
    private String day;
    private String startTime;
    private String endTime;
    private String courseCode;
    private String title;
    private String room;

    public TimetableEntry() {
    }

    public TimetableEntry(String day, String startTime, String endTime, String courseCode, String title, String room) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseCode = courseCode;
        this.title = title;
        this.room = room;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}



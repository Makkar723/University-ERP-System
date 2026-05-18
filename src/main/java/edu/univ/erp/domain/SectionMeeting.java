package edu.univ.erp.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
            
public class SectionMeeting {
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    
    public SectionMeeting() {
    }
    
    public SectionMeeting(DayOfWeek day, LocalTime start, LocalTime end) {
        this.day = day;
        this.start = start;
        this.end = end;
    }
    
    public DayOfWeek getDay() {
        return day;
    }
    
    public void setDay(DayOfWeek day) {
        this.day = day;
    }
    
    public LocalTime getStart() {
        return start;
    }
    
    public void setStart(LocalTime start) {
        this.start = start;
    }
    
    public LocalTime getEnd() {
        return end;
    }
    
    public void setEnd(LocalTime end) {
        this.end = end;
    }
    
    @Override
    public String toString() {
        return day + " " + start + "-" + end;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionMeeting that = (SectionMeeting) o;
        return day == that.day && 
               start.equals(that.start) && 
               end.equals(that.end);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(day, start, end);
    }
}



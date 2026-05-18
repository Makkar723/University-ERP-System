package edu.univ.erp.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.univ.erp.domain.SectionMeeting;

/**
 * Utility class for parsing and formatting section schedules.
 * Format: "MON 09:00-10:30;WED 14:00-15:00"
 */
public final class SectionScheduleUtil {
    
    /**
     * Parse a schedule string into a list of SectionMeeting objects.
     * @param scheduleString Format: "MON 09:00-10:30;WED 14:00-15:00" or null/empty
     * @return List of SectionMeeting objects
     * @throws IllegalArgumentException if the format is invalid
     */
    public static List<SectionMeeting> parseSchedule(String scheduleString) {
        List<SectionMeeting> meetings = new ArrayList<>();
        
        if (scheduleString == null || scheduleString.trim().isEmpty()) {
            return meetings;
        }
        
        String[] parts = scheduleString.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) {
                continue;
            }
            
            // Parse format: "MON 09:00-10:30"
            String[] dayTimeParts = part.split("\\s+", 2);
            if (dayTimeParts.length != 2) {
                throw new IllegalArgumentException("Invalid schedule format: " + part + 
                    ". Expected format: DAY HH:MM-HH:MM");
            }
            
            // Parse day - map 3-letter abbreviations to full DayOfWeek enum names
            String dayStr = dayTimeParts[0].trim().toUpperCase();
            DayOfWeek day;
            try {
                // Map abbreviations to full enum names
                String fullDayName;
                switch (dayStr) {
                    case "MON": fullDayName = "MONDAY"; break;
                    case "TUE": fullDayName = "TUESDAY"; break;
                    case "WED": fullDayName = "WEDNESDAY"; break;
                    case "THU": fullDayName = "THURSDAY"; break;
                    case "FRI": fullDayName = "FRIDAY"; break;
                    case "SAT": fullDayName = "SATURDAY"; break;
                    case "SUN": fullDayName = "SUNDAY"; break;
                    default: fullDayName = dayStr; // Try as-is (might be full name)
                }
                day = DayOfWeek.valueOf(fullDayName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid day: " + dayStr + 
                    ". Must be one of: MON, TUE, WED, THU, FRI, SAT, SUN");
            }
            
            // Parse time range
            String timeRange = dayTimeParts[1].trim();
            String[] timeParts = timeRange.split("-");
            if (timeParts.length != 2) {
                throw new IllegalArgumentException("Invalid time range: " + timeRange + 
                    ". Expected format: HH:MM-HH:MM");
            }
            
            LocalTime start = parseTime(timeParts[0].trim(), "start");
            LocalTime end = parseTime(timeParts[1].trim(), "end");
            
            // Validate end > start
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("End time must be after start time: " + 
                    start + " - " + end);
            }
            
            meetings.add(new SectionMeeting(day, start, end));
        }
        
        return meetings;
    }
    
    /**
     * Parse a time string (HH:MM) and validate 30-minute granularity.
     */
    private static LocalTime parseTime(String timeStr, String label) {
        String[] parts = timeStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid " + label + " time format: " + timeStr + 
                ". Expected HH:MM");
        }
        
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            if (hour < 0 || hour > 23) {
                throw new IllegalArgumentException("Invalid hour: " + hour + ". Must be 0-23");
            }
            
            if (minute != 0 && minute != 30) {
                throw new IllegalArgumentException("Invalid " + label + " minutes: " + minute + 
                    ". Must be 00 or 30");
            }
            
            return LocalTime.of(hour, minute);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + label + " time format: " + timeStr + 
                ". Expected HH:MM");
        }
    }
    
    /**
     * Format a list of SectionMeeting objects into a schedule string.
     * @param meetings List of meetings (will be sorted by day then start time)
     * @return Formatted string: "MON 09:00-10:30;WED 14:00-15:00"
     */
    public static String formatSchedule(List<SectionMeeting> meetings) {
        if (meetings == null || meetings.isEmpty()) {
            return "";
        }
        
        // Sort by day (MON=1, TUE=2, etc.) then start time
        List<SectionMeeting> sorted = new ArrayList<>(meetings);
        sorted.sort(Comparator
            .comparing((SectionMeeting m) -> m.getDay().getValue())
            .thenComparing(SectionMeeting::getStart));
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                sb.append(";");
            }
            
            SectionMeeting meeting = sorted.get(i);
            String dayStr = meeting.getDay().name().substring(0, 3); // MON, TUE, etc.
            String startStr = formatTime(meeting.getStart());
            String endStr = formatTime(meeting.getEnd());
            
            sb.append(dayStr).append(" ")
              .append(startStr).append("-").append(endStr);
        }
        
        return sb.toString();
    }
    
    /**
     * Format LocalTime to HH:MM string.
     */
    private static String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}


package edu.univ.erp.data.dao;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.Section;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SectionDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public Section findById(int id) {
        String sql = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year " +
                     "FROM sections WHERE section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding section by ID: " + id, e);
        }
        
        throw new DataAccessException("Section not found with ID: " + id);
    }
    
    public int createSection(Section section) {
        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, section.getCourseId());
            if (section.getInstructorId() != null) {
                stmt.setInt(2, section.getInstructorId());
            } else {
                stmt.setObject(2, null);
            }
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setString(6, section.getSemester());
            stmt.setInt(7, section.getYear());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            throw new DataAccessException("Failed to retrieve generated section_id");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating section", e);
        }
    }
    
    public List<Section> listSectionsByCourse(int courseId) {
        String sql = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year " +
                     "FROM sections WHERE course_id = ? ORDER BY section_id";
        List<Section> sections = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing sections for course: " + courseId, e);
        }
        
        return sections;
    }
    
    public int countEnrolled(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'enrolled'";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting enrolled students for section: " + sectionId, e);
        }
        
        return 0;
    }
    
    public int getCapacity(int sectionId) {
        String sql = "SELECT capacity FROM sections WHERE section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacity");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting capacity for section: " + sectionId, e);
        }
        
        throw new DataAccessException("Section not found: " + sectionId);
    }
    
    public int countByInstructor(int instructorUserId) {
        String sql = "SELECT COUNT(*) FROM sections WHERE instructor_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instructorUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting sections for instructor: " + instructorUserId, e);
        }
        
        return 0;
    }
    
    public void updateInstructor(int sectionId, Integer instructorId) {
        String sql = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (instructorId != null) {
                stmt.setInt(1, instructorId);
            } else {
                stmt.setObject(1, null);
            }
            stmt.setInt(2, sectionId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Section not found: " + sectionId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating instructor for section: " + sectionId, e);
        }
    }
    
    public void updateSection(Section section) {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, day_time = ?, " +
                     "room = ?, capacity = ?, semester = ?, year = ? WHERE section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, section.getCourseId());
            if (section.getInstructorId() != null) {
                stmt.setInt(2, section.getInstructorId());
            } else {
                stmt.setObject(2, null);
            }
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setString(6, section.getSemester());
            stmt.setInt(7, section.getYear());
            stmt.setInt(8, section.getSectionId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Section not found: " + section.getSectionId());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating section: " + section.getSectionId(), e);
        }
    }
    
    public List<Section> listAllSections() {
        String sql = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year " +
                     "FROM sections ORDER BY section_id";
        List<Section> sections = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                sections.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all sections", e);
        }
        
        return sections;
    }
    
    public List<Section> listSectionsByInstructor(int instructorUserId) {
        String sql = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year " +
                     "FROM sections WHERE instructor_id = ? ORDER BY year DESC, semester, course_id";
        List<Section> sections = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instructorUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing sections for instructor: " + instructorUserId, e);
        }
        
        return sections;
    }
    
    public boolean instructorOwnsSection(int instructorUserId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM sections WHERE section_id = ? AND instructor_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            stmt.setInt(2, instructorUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking section ownership", e);
        }
        
        return false;
    }
    
    public void deleteSection(int sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Section not found with ID: " + sectionId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting section: " + sectionId, e);
        }
    }
    
    public int countEnrollments(int sectionId) {
        return countEnrolled(sectionId);
    }
    
    public List<Section> listSections() {
        return listAllSections();
    }
    
    public List<Section> listSectionsForCourse(int courseId) {
        return listSectionsByCourse(courseId);
    }
    
    private Section mapRow(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseId(rs.getInt("course_id"));
        
        int instructorId = rs.getInt("instructor_id");
        if (!rs.wasNull()) {
            section.setInstructorId(instructorId);
        }
        
        section.setDayTime(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getString("semester"));
        section.setYear(rs.getInt("year"));
        return section;
    }
}



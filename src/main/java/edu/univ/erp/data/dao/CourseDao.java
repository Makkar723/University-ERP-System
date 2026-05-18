package edu.univ.erp.data.dao;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.Course;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public Course findById(int id) {
        String sql = "SELECT course_id, code, title, credits FROM courses WHERE course_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding course by ID: " + id, e);
        }
        
        throw new DataAccessException("Course not found with ID: " + id);
    }
    
    public int createCourse(Course course) {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            throw new DataAccessException("Failed to retrieve generated course_id");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating course", e);
        }
    }
    
    public List<Course> listCourses() {
        String sql = "SELECT course_id, code, title, credits FROM courses ORDER BY code";
        List<Course> courses = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing courses", e);
        }
        
        return courses;
    }
    
    public void updateCourse(Course course) {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE course_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getCourseId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Course not found with ID: " + course.getCourseId());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating course: " + course.getCourseId(), e);
        }
    }
    
    public java.util.Optional<Course> findByCode(String code) {
        String sql = "SELECT course_id, code, title, credits FROM courses WHERE code = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding course by code: " + code, e);
        }
        
        return java.util.Optional.empty();
    }
    
    public void deleteCourse(int courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Course not found with ID: " + courseId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting course: " + courseId, e);
        }
    }
        
    public int countSections(int courseId) {
        String sql = "SELECT COUNT(*) FROM sections WHERE course_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting sections for course: " + courseId, e);
        }
        
        return 0;
    }
    
    private Course mapRow(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCode(rs.getString("code"));
        course.setTitle(rs.getString("title"));
        course.setCredits(rs.getInt("credits"));
        return course;
    }
}



package edu.univ.erp.data.dao;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.StudentEnrollmentRow;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public int createEnrollment(Connection conn, Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getSectionId());
            stmt.setString(3, enrollment.getStatus() != null ? enrollment.getStatus() : "enrolled");
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            throw new DataAccessException("Failed to retrieve generated enrollment_id");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating enrollment", e);
        }
    }
    
    public void createEnrollment(Enrollment enrollment) {
        try (Connection conn = dataSource.getConnection()) {
            createEnrollment(conn, enrollment);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting connection for enrollment", e);
        }
    }
    
    public List<Enrollment> listByStudent(int studentId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status " +
                     "FROM enrollments WHERE student_id = ? ORDER BY enrollment_id";
        List<Enrollment> enrollments = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing enrollments for student: " + studentId, e);
        }
        
        return enrollments;
    }
    
    public boolean exists(int studentId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking enrollment existence", e);
        }
        
        return false;
    }
    
    public void deleteEnrollment(int studentId, int sectionId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting enrollment", e);
        }
    }
    
    public void deleteByStudentId(int studentId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting enrollments for student: " + studentId, e);
        }
    }
    
    public int countBySection(int sectionId) {
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
            throw new DataAccessException("Error counting enrollments for section: " + sectionId, e);
        }
        
        return 0;
    }
    
    public List<Enrollment> listBySection(int sectionId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status " +
                     "FROM enrollments WHERE section_id = ? ORDER BY enrollment_id";
        List<Enrollment> enrollments = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing enrollments for section: " + sectionId, e);
        }
        
        return enrollments;
    }
    
    public void deleteBySectionId(int sectionId) {
        String sql = "DELETE FROM enrollments WHERE section_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting enrollments for section: " + sectionId, e);
        }
    }
    
    public void deleteByCourseId(int courseId) {
        String sql = "DELETE FROM enrollments WHERE section_id IN " +
                     "(SELECT section_id FROM sections WHERE course_id = ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting enrollments for course: " + courseId, e);
        }
    }
    
    public List<StudentEnrollmentRow> listStudentRowsBySection(int sectionId) {
        String sql = "SELECT e.enrollment_id, e.student_id, s.roll_no, s.program " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.student_id = s.user_id " +
                     "WHERE e.section_id = ? AND e.status = 'enrolled' " +
                     "ORDER BY s.roll_no";
        List<StudentEnrollmentRow> rows = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StudentEnrollmentRow row = new StudentEnrollmentRow();
                    row.setEnrollmentId(rs.getInt("enrollment_id"));
                    row.setStudentUserId(rs.getInt("student_id"));
                    row.setStudentRollNo(rs.getString("roll_no"));
                    row.setStudentProgram(rs.getString("program"));
                    // Username will be set separately via AuthDao
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing student rows for section: " + sectionId, e);
        }
        
        return rows;
    }
    
    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setSectionId(rs.getInt("section_id"));
        enrollment.setStatus(rs.getString("status"));
        return enrollment;
    }
}



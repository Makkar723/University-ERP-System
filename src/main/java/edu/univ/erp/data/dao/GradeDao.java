package edu.univ.erp.data.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.Grade;

public class GradeDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public void upsertGrade(Grade grade) {
        // Check if grade exists
        String checkSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, grade.getEnrollmentId());
            checkStmt.setString(2, grade.getComponent());
            
            boolean exists = false;
            Integer existingGradeId = null;
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    exists = true;
                    existingGradeId = rs.getInt("grade_id");
                }
            }
            
            if (exists && existingGradeId != null) {
                // Update existing grade
                String updateSql = "UPDATE grades SET score = ?, final_grade = ? WHERE grade_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    if (grade.getScore() != null) {
                        updateStmt.setBigDecimal(1, grade.getScore());
                    } else {
                        updateStmt.setObject(1, null);
                    }
                    updateStmt.setString(2, grade.getFinalGrade());
                    updateStmt.setInt(3, existingGradeId);
                    updateStmt.executeUpdate();
                }
            } else {
                // Insert new grade
                String insertSql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, grade.getEnrollmentId());
                    insertStmt.setString(2, grade.getComponent());
                    if (grade.getScore() != null) {
                        insertStmt.setBigDecimal(3, grade.getScore());
                    } else {
                        insertStmt.setObject(3, null);
                    }
                    insertStmt.setString(4, grade.getFinalGrade());
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error upserting grade", e);
        }
    }
    
    public void deleteByStudentId(int studentId) {
        // Delete grades where enrollment_id is in enrollments for this student
        String sql = "DELETE FROM grades WHERE enrollment_id IN " +
                     "(SELECT enrollment_id FROM enrollments WHERE student_id = ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting grades for student: " + studentId, e);
        }
    }
    
    public void deleteByEnrollment(int enrollmentId) {
        String sql = "DELETE FROM grades WHERE enrollment_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting grades for enrollment: " + enrollmentId, e);
        }
    }
    
    public List<Grade> listByEnrollment(int enrollmentId) {
        String sql = "SELECT grade_id, enrollment_id, component, score, final_grade " +
                     "FROM grades WHERE enrollment_id = ? ORDER BY grade_id";
        List<Grade> grades = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, enrollmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing grades for enrollment: " + enrollmentId, e);
        }
        
        return grades;
    }
    
    public List<Grade> listBySection(int sectionId) {
        String sql = "SELECT g.grade_id, g.enrollment_id, g.component, g.score, g.final_grade " +
                     "FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                     "WHERE e.section_id = ? " +
                     "ORDER BY e.enrollment_id, g.grade_id";
        List<Grade> grades = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing grades for section: " + sectionId, e);
        }
        
        return grades;
    }
    
    public void upsertGrade(int enrollmentId, String component, Double score, String finalGrade) {
        Grade grade = new Grade();
        grade.setEnrollmentId(enrollmentId);
        grade.setComponent(component);
        if (score != null) {
            grade.setScore(BigDecimal.valueOf(score));
        }
        grade.setFinalGrade(finalGrade);
        upsertGrade(grade);
    }
    
    public List<String> listComponentsForSection(int sectionId) {
        String sql = "SELECT DISTINCT g.component " +
                     "FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                     "WHERE e.section_id = ? " +
                     "ORDER BY g.component";
        List<String> components = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String component = rs.getString("component");
                    // Filter out reserved "final" component
                    if (component != null && !"final".equalsIgnoreCase(component)) {
                        components.add(component);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing components for section: " + sectionId, e);
        }
        
        return components;
    }
    
    public void deleteComponentForSection(int sectionId, String component) {
        String sql = "DELETE g FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                     "WHERE e.section_id = ? AND g.component = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            stmt.setString(2, component);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting component for section: " + sectionId, e);
        }
    }
    
    public void renameComponentForSection(int sectionId, String oldComponent, String newComponent) {
        String sql = "UPDATE grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                     "SET g.component = ? " +
                     "WHERE e.section_id = ? AND g.component = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newComponent);
            stmt.setInt(2, sectionId);
            stmt.setString(3, oldComponent);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error renaming component for section: " + sectionId, e);
        }
    }
    
    public void createComponentForSection(int sectionId, String component) {
        String sql = "INSERT INTO grades (enrollment_id, component, score, final_grade) " +
                     "SELECT e.enrollment_id, ?, NULL, NULL " +
                     "FROM enrollments e " +
                     "WHERE e.section_id = ? AND e.status = 'enrolled' " +
                     "AND NOT EXISTS (" +
                     "  SELECT 1 FROM grades g2 " +
                     "  WHERE g2.enrollment_id = e.enrollment_id AND g2.component = ?" +
                     ")";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, component);
            stmt.setInt(2, sectionId);
            stmt.setString(3, component);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating component for section: " + sectionId, e);
        }
    }
    
    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setEnrollmentId(rs.getInt("enrollment_id"));
        grade.setComponent(rs.getString("component"));
        
        BigDecimal score = rs.getBigDecimal("score");
        if (score != null) {
            grade.setScore(score);
        }
        
        grade.setFinalGrade(rs.getString("final_grade"));
        return grade;
    }
}



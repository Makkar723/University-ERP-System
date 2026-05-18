package edu.univ.erp.data.dao;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.Student;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public Optional<Student> findByUserId(int userId) {
        String sql = "SELECT user_id, roll_no, program, year FROM students WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding student by user_id: " + userId, e);
        }
        
        return Optional.empty();
    }
    
    public void insertStudent(Student student) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, student.getUserId());
            stmt.setString(2, student.getRollNo());
            stmt.setString(3, student.getProgram());
            stmt.setInt(4, student.getYear());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting student", e);
        }
    }
    
    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM students WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting student by user_id: " + userId, e);
        }
    }
    
    public List<Student> listAll() {
        String sql = "SELECT user_id, roll_no, program, year FROM students ORDER BY user_id";
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all students", e);
        }
        
        return students;
    }
    
    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setUserId(rs.getInt("user_id"));
        student.setRollNo(rs.getString("roll_no"));
        student.setProgram(rs.getString("program"));
        student.setYear(rs.getInt("year"));
        return student;
    }
}



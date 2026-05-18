package edu.univ.erp.data.dao;

import edu.univ.erp.data.DataAccessException;
import edu.univ.erp.data.DataSourceFactory;
import edu.univ.erp.domain.Instructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstructorDao {
    private static final DataSource dataSource = DataSourceFactory.getERPDataSource();
    
    public Optional<Instructor> findByUserId(int userId) {
        String sql = "SELECT user_id, department FROM instructors WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding instructor by user_id: " + userId, e);
        }
        
        return Optional.empty();
    }
    
    public void insertInstructor(Instructor instructor) {
        String sql = "INSERT INTO instructors (user_id, department) VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instructor.getUserId());
            stmt.setString(2, instructor.getDepartment());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting instructor", e);
        }
    }
    
    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM instructors WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting instructor by user_id: " + userId, e);
        }
    }
    
    public List<Instructor> listAll() {
        String sql = "SELECT user_id, department FROM instructors ORDER BY user_id";
        List<Instructor> instructors = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                instructors.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all instructors", e);
        }
        
        return instructors;
    }
    
    private Instructor mapRow(ResultSet rs) throws SQLException {
        Instructor instructor = new Instructor();
        instructor.setUserId(rs.getInt("user_id"));
        instructor.setDepartment(rs.getString("department"));
        return instructor;
    }
}



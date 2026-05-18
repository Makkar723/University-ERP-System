package edu.univ.erp.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.domain.UserAuth;

/** Simple authentication service */
public class AuthService {
    private final AuthDao authDao;
    
    public AuthService(AuthDao authDao) {
        this.authDao = authDao;
    }
    
    /** Login user with password */
    public UserAuth login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        
        Optional<UserAuth> userOpt = authDao.findByUsername(username.trim());
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Incorrect username or password.");
        }
        
        UserAuth user = userOpt.get();
        
        // Ensure user active
        if (!"active".equals(user.getStatus())) {
            throw new IllegalArgumentException("Account is not active.");
        }
        
        // Check BCrypt password
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect username or password.");
        }
        
        // Update last login
        LocalDateTime now = LocalDateTime.now();
        authDao.updateLastLogin(user.getUserId(), now);
        user.setLastLogin(now);
        
        // Save session user
        SessionManager.setCurrentUser(user);
        
        return user;
    }
    
    /** Change user password */
    public void changePassword(int userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty() || 
            newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Old and new passwords are required.");
        }
        
        UserAuth user = authDao.findById(userId);
        
        // Verify old password
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }
        
        // Hash and update
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        authDao.updatePasswordHash(userId, hashedPassword);
    }
}


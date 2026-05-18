package edu.univ.erp.auth;

import edu.univ.erp.domain.UserAuth;

public class SessionManager {
    private static UserAuth currentUser;
    
    // set logged user
    public static void setCurrentUser(UserAuth user) {
        currentUser = user;
    }
    
    // get current user
    public static UserAuth getCurrentUser() {
        return currentUser;
    }
    
    // clear session logout
    public static void clear() {
        currentUser = null;
    }
    
    // check if logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}



package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

    /** Simple BCrypt hash helper */
public class HashPassword {
    public static void main(String[] args) {
        // Ensure argument provided
        if (args.length == 0 || args[0].isEmpty()) {
            System.err.println("Usage: java edu.univ.erp.util.HashPassword <password>");
            System.exit(1);
        }
        
        String plainPassword = args[0];
        
        // Generate BCrypt hash
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        
        // Print hash to stdout
        System.out.println(hash);
    }
}


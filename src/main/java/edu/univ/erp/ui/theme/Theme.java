package edu.univ.erp.ui.theme;

import java.awt.Color;
import javax.swing.JButton;

/**
 * UI theme constants for consistent accent color usage across the application.
 */
public final class Theme {
    // Primary accent color
    public static final Color ACCENT = Color.decode("#3FADA8");
    
    // Optional secondary shades
    public static final Color ACCENT_DARK = new Color(40, 120, 115);
    public static final Color ACCENT_LIGHT = new Color(200, 240, 238);
    
    // Buttons
    public static final Color BUTTON_BG = ACCENT;
    public static final Color BUTTON_FG = Color.WHITE;
    
    // Headings
    public static final Color HEADING = ACCENT_DARK;
    
    // Success messages
    public static final Color SUCCESS_TEXT = ACCENT_DARK;
    
    /**
     * Apply theme styling to a button.
     */
    public static void styleButton(JButton button) {
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }
    
    private Theme() {}
}


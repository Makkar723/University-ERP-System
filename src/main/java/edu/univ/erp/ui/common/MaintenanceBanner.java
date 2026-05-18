package edu.univ.erp.ui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import static javax.swing.SwingUtilities.invokeLater;

import edu.univ.erp.service.MaintenanceService;

public class MaintenanceBanner extends JPanel {
    private final MaintenanceService maintenanceService;
    private final JLabel warningLabel;
    
    public MaintenanceBanner(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
        
        setLayout(new BorderLayout());
        setBackground(new Color(0xB00020)); // Red background
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        setOpaque(true); // Ensure background is painted
        
        warningLabel = new JLabel("WARNING: ADMIN HAS SET THE SYSTEM TO MAINTENANCE MODE. YOU CANNOT CHANGE ANYTHING.");
        warningLabel.setForeground(Color.WHITE);
        warningLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        warningLabel.setOpaque(false);
        
        add(warningLabel, BorderLayout.CENTER);
        
        // Set initial size and visibility
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        setMinimumSize(new Dimension(0, 60));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // Set initial visibility
        updateVisibility();
    }
    
    public void updateVisibility() {
        boolean maintenanceOn = maintenanceService.isMaintenanceOn();
        
        if (maintenanceOn) {
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            setMinimumSize(new Dimension(0, 60));
            setVisible(true);
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
            setMinimumSize(new Dimension(0, 0));
            setVisible(false);
        }
        
        invokeLater(() -> {
            revalidate();
            repaint();
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        });
    }
}


package edu.univ.erp.ui;

/**
 * Helper class for JComboBox items that store both display text and ID.
 */
public class ComboBoxItem {
    private final String displayText;
    private final Integer id;
    
    public ComboBoxItem(String displayText, Integer id) {
        this.displayText = displayText;
        this.id = id;
    }
    
    public String getDisplayText() {
        return displayText;
    }
    
    public Integer getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return displayText;
    }
}



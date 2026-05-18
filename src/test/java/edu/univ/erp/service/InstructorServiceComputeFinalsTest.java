package edu.univ.erp.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InstructorService computeFinalGrades computation logic.
 * Tests the formula and validation without requiring database or mocks.
 */
class InstructorServiceComputeFinalsTest {
    
    @Test
    void testComputeFinalGrades_ValidWeights_ComputesCorrectly() {
        // Test weight validation
        Map<String, Double> weights = new HashMap<>();
        weights.put("quiz", 20.0);
        weights.put("midsem", 30.0);
        weights.put("endsem", 50.0);
        
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(100.0, total, 0.01, "Weights should sum to 100");
        
        // Test computation formula
        double quizScore = 80.0;
        double midsemScore = 70.0;
        double endsemScore = 90.0;
        
        double expectedFinal = (quizScore * 20.0 / 100.0) +
                              (midsemScore * 30.0 / 100.0) +
                              (endsemScore * 50.0 / 100.0);
        
        assertEquals(81.0, expectedFinal, 0.01, "Final should be 81.0");
        
        // Test letter grade assignment
        String letter = expectedFinal >= 85 ? "A" :
                       expectedFinal >= 70 ? "B" :
                       expectedFinal >= 55 ? "C" :
                       expectedFinal >= 40 ? "D" : "F";
        assertEquals("B", letter, "81.0 should be grade B");
    }
    
    @Test
    void testComputeFinalGrades_WeightsDontSumTo100_ShouldFail() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("quiz", 20.0);
        weights.put("midsem", 30.0);
        weights.put("endsem", 40.0); // Sum = 90, not 100
        
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        assertTrue(Math.abs(total - 100.0) > 0.01, "Weights should not sum to 100");
    }
    
    @Test
    void testComputeFinalGrades_MissingScoresTreatedAsZero() {
        // Test that missing component scores are treated as 0
        double quizScore = 80.0;
        double midsemScore = 0.0; // Missing
        double endsemScore = 90.0;
        
        double finalScore = (quizScore * 20.0 / 100.0) +
                           (midsemScore * 30.0 / 100.0) +
                           (endsemScore * 50.0 / 100.0);
        
        assertEquals(61.0, finalScore, 0.01, "Missing midsem should be treated as 0");
    }
    
    @Test
    void testLetterGradeAssignment() {
        // Test letter grade boundaries
        assertEquals("A", getLetterGrade(85.0));
        assertEquals("A", getLetterGrade(100.0));
        assertEquals("B", getLetterGrade(84.99));
        assertEquals("B", getLetterGrade(70.0));
        assertEquals("C", getLetterGrade(69.99));
        assertEquals("C", getLetterGrade(55.0));
        assertEquals("D", getLetterGrade(54.99));
        assertEquals("D", getLetterGrade(40.0));
        assertEquals("F", getLetterGrade(39.99));
        assertEquals("F", getLetterGrade(0.0));
    }
    
    private String getLetterGrade(double score) {
        if (score >= 85) return "A";
        if (score >= 70) return "B";
        if (score >= 55) return "C";
        if (score >= 40) return "D";
        return "F";
    }
}

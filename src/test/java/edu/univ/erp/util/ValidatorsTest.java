package edu.univ.erp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorsTest {

    @Test
    void validateCourseCode_acceptsValidCodes() {
        assertDoesNotThrow(() -> Validators.validateCourseCode("CSE101"));
        assertDoesNotThrow(() -> Validators.validateCourseCode("ece205"));
    }

    @Test
    void validateCourseCode_rejectsInvalidCodes() {
        assertThrows(IllegalArgumentException.class, () -> Validators.validateCourseCode("ABC123"));
        assertThrows(IllegalArgumentException.class, () -> Validators.validateCourseCode("CSE12"));
        assertThrows(IllegalArgumentException.class, () -> Validators.validateCourseCode("CSE1A1"));
    }

    @Test
    void validateInstructorDept_acceptsAllowedDepartments() {
        assertDoesNotThrow(() -> Validators.validateInstructorDept("ECE"));
        assertDoesNotThrow(() -> Validators.validateInstructorDept("com"));
    }

    @Test
    void validateInstructorDept_rejectsOthers() {
        assertThrows(IllegalArgumentException.class, () -> Validators.validateInstructorDept("MKT"));
    }

    @Test
    void validateStudentBranch_acceptsAllowedBranches() {
        assertDoesNotThrow(() -> Validators.validateStudentBranch("CSAI"));
        assertDoesNotThrow(() -> Validators.validateStudentBranch("csss"));
    }

    @Test
    void validateStudentBranch_rejectsOthers() {
        assertThrows(IllegalArgumentException.class, () -> Validators.validateStudentBranch("MECH"));
    }
}


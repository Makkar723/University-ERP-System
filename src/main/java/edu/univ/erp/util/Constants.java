package edu.univ.erp.util;

import java.util.Set;

/**
 * Shared constants for validation rules across the application.
 */
public final class Constants {
    private Constants() {}

    public static final Set<String> COURSE_PREFIXES = Set.of(
        "CSE", "MTH", "ECE", "DES", "BIO", "SSH", "SOC", "COM", "ENT", "ECO"
    );

    public static final Set<String> INSTRUCTOR_DEPTS = COURSE_PREFIXES;

    public static final Set<String> STUDENT_BRANCHES = Set.of(
        "CSE", "CSAM", "CSD", "CSB", "ECE", "EVE", "CSAI", "CSSS"
    );
}


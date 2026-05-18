package edu.univ.erp.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Centralized validation helpers for domain specific rules.
 */
public final class Validators {
    private static final Pattern COURSE_CODE = Pattern.compile(
        "^(CSE|MTH|ECE|DES|BIO|SSH|SOC|COM|ENT|ECO)\\d{3}$",
        Pattern.CASE_INSENSITIVE
    );

    private Validators() {}

    public static void validateCourseCode(String code) {
        if (code == null || !COURSE_CODE.matcher(code.trim()).matches()) {
            throw new IllegalArgumentException(
                "Invalid course code. Use format CSE101 and prefix one of: " + Constants.COURSE_PREFIXES
            );
        }
    }

    public static void validateInstructorDept(String dept) {
        if (dept == null || !Constants.INSTRUCTOR_DEPTS.contains(dept.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid instructor department. Allowed: " + Constants.INSTRUCTOR_DEPTS);
        }
    }

    public static void validateStudentBranch(String branch) {
        if (branch == null || !Constants.STUDENT_BRANCHES.contains(branch.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid student branch. Allowed: " + Constants.STUDENT_BRANCHES);
        }
    }
}


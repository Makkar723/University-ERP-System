-- Optional constraint: enforce course code format
-- Requires MySQL 8.0+ for CHECK constraint enforcement
ALTER TABLE courses
ADD CONSTRAINT chk_course_code_format
CHECK (code REGEXP '^(CSE|MTH|ECE|DES|BIO|SSH|SOC|COM|ENT|ECO)[0-9]{3}$');


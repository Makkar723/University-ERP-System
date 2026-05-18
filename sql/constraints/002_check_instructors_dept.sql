-- Optional constraint: enforce instructor departments
-- Requires MySQL 8.0+ for CHECK constraint enforcement
ALTER TABLE instructors
ADD CONSTRAINT chk_instructor_dept
CHECK (department IN ('CSE','MTH','ECE','DES','BIO','SSH','SOC','COM','ENT','ECO'));


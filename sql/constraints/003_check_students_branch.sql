-- Optional constraint: enforce student program/branch values
-- Requires MySQL 8.0+ for CHECK constraint enforcement
ALTER TABLE students
ADD CONSTRAINT chk_student_branch
CHECK (program IN ('CSE','CSAM','CSD','CSB','ECE','EVE','CSAI','CSSS'));


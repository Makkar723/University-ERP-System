-- ============================================
-- Initialize All Databases - Wrapper Script
-- ============================================
-- This script sources all SQL files in the correct order.
-- 
-- WARNING: This script does NOT include seed_data.sql because
-- seed_data.sql contains password hash placeholders that must be
-- replaced with real BCrypt hashes before execution.
-- 
-- Usage:
--   1. Run this script to create databases and schemas
--   2. Generate password hashes using HashPassword.java
--   3. Replace placeholders in seed_data.sql
--   4. Run seed_data.sql separately
-- 
-- Or run manually in this order:
--   mysql -u root -p < sql/create_databases.sql
--   mysql -u root -p auth_db < sql/auth_schema.sql
--   mysql -u root -p erp_db < sql/erp_schema.sql
--   (generate hashes and update seed_data.sql)
--   mysql -u root -p < sql/seed_data.sql
-- ============================================

-- Note: SOURCE command requires absolute paths or paths relative to
-- the MySQL client's current directory. For portability, this script
-- is provided as a reference. You may need to adjust paths or run
-- the individual scripts manually.

-- Create databases
SOURCE create_databases.sql;

-- Create auth schema
USE auth_db;
SOURCE auth_schema.sql;

-- Create ERP schema
USE erp_db;
SOURCE erp_schema.sql;

-- DO NOT source seed_data.sql here - it requires password hash replacement first!
-- After generating hashes and updating seed_data.sql, run it separately:
-- USE auth_db;
-- SOURCE seed_data.sql;

SELECT 'Database initialization complete (schemas only).' AS Status;
SELECT 'Next: Generate password hashes and update seed_data.sql, then run it separately.' AS NextStep;


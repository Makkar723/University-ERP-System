# Running SQL Scripts - Setup Instructions

This document provides step-by-step instructions for setting up the `auth_db` and `erp_db` databases, creating schemas, and inserting seed data.

## Prerequisites

- MySQL Server installed and running
- MySQL root access (or appropriate user with CREATE DATABASE privileges)
- Maven installed (for generating password hashes)
- Java 17+ installed

## Quick Reference for PowerShell Users

If you're using PowerShell on Windows, use `Get-Content` instead of `<` redirection:

```powershell
# Create databases
Get-Content sql/create_databases.sql | mysql -u root -p

# Create schemas
Get-Content sql/auth_schema.sql | mysql -u root -p auth_db
Get-Content sql/erp_schema.sql | mysql -u root -p erp_db

# Insert seed data (after replacing password hashes)
Get-Content sql/seed_data.sql | mysql -u root -p
```

**If MySQL is not in your PATH**, use the full path to mysql.exe:

```powershell
# Example for MySQL 8.0 (adjust version/path as needed)
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

# Create databases
Get-Content sql/create_databases.sql | & $mysqlPath -u root -p

# Create schemas
Get-Content sql/auth_schema.sql | & $mysqlPath -u root -p auth_db
Get-Content sql/erp_schema.sql | & $mysqlPath -u root -p erp_db

# Insert seed data
Get-Content sql/seed_data.sql | & $mysqlPath -u root -p
```

**To find your MySQL installation:**
```powershell
# Check common locations
Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter "mysql.exe" -ErrorAction SilentlyContinue | Select-Object FullName
```

**Using the helper script (recommended):**
A PowerShell helper script `run-sql.ps1` is provided that automatically detects MySQL.

**Important:** Run these commands from the `univ-erp` directory:

```powershell
# Navigate to univ-erp directory first
cd univ-erp

# Create databases
.\run-sql.ps1 -SqlFile sql/create_databases.sql

# Create schemas
.\run-sql.ps1 -SqlFile sql/auth_schema.sql -Database auth_db
.\run-sql.ps1 -SqlFile sql/erp_schema.sql -Database erp_db

# Insert seed data (after replacing password hashes)
.\run-sql.ps1 -SqlFile sql/seed_data.sql
```

**Alternative:** You can also use just the filename if you're in the `univ-erp` directory:
```powershell
.\run-sql.ps1 -SqlFile create_databases.sql
```

## Step-by-Step Setup

**⚠️ Important:** All commands below should be run from the `univ-erp` project directory unless otherwise specified.

### Step 1: Create Databases

Create both `auth_db` and `erp_db` databases:

**On Linux/Mac (bash):**
```bash
mysql -u root -p < sql/create_databases.sql
```

**On Windows (PowerShell):**
```powershell
Get-Content sql/create_databases.sql | mysql -u root -p
```

**Or using cmd.exe:**
```cmd
mysql -u root -p < sql\create_databases.sql
```

Or using MySQL Workbench:
1. Open MySQL Workbench
2. File → Open SQL Script → Select `sql/create_databases.sql`
3. Execute the script (Ctrl+Shift+Enter or click Execute)

**Expected output:** Both databases should be created with utf8mb4 charset.

---

### Step 2: Create Auth Schema

Create the authentication tables in `auth_db`:

**On Linux/Mac (bash):**
```bash
mysql -u root -p auth_db < sql/auth_schema.sql
```

**On Windows (PowerShell):**
```powershell
Get-Content sql/auth_schema.sql | mysql -u root -p auth_db
```

**Or using cmd.exe:**
```cmd
mysql -u root -p auth_db < sql\auth_schema.sql
```

Or in MySQL Workbench:
1. Open `sql/auth_schema.sql`
2. Ensure `auth_db` is selected in the schema dropdown
3. Execute the script

**Expected output:** Tables `users_auth` and `auth_failed_logins` created.

---

### Step 3: Create ERP Schema

Create the ERP business tables in `erp_db`:

**On Linux/Mac (bash):**
```bash
mysql -u root -p erp_db < sql/erp_schema.sql
```

**On Windows (PowerShell):**
```powershell
Get-Content sql/erp_schema.sql | mysql -u root -p erp_db
```

**Or using cmd.exe:**
```cmd
mysql -u root -p erp_db < sql\erp_schema.sql
```

Or in MySQL Workbench:
1. Open `sql/erp_schema.sql`
2. Ensure `erp_db` is selected in the schema dropdown
3. Execute the script

**Expected output:** Tables `students`, `instructors`, `courses`, `sections`, `enrollments`, `grades`, and `settings` created.

---

### Step 4: Generate BCrypt Password Hashes

**Quick Method:** Use the provided PowerShell script:
```powershell
.\generate-all-hashes.ps1
```

This will generate all four password hashes at once. Then copy each hash and replace the placeholders in `sql/seed_data.sql`.

**Manual Method:** Generate hashes one at a time:

**IMPORTANT:** Before inserting seed data, you must generate real BCrypt password hashes and replace the placeholders in `seed_data.sql`.

#### Using Maven Exec Plugin

**Note:** If you just installed Maven globally, you may need to restart your terminal/PowerShell for it to be available in PATH.

Navigate to the project root (`univ-erp`) and run:

```powershell
# Generate hash for admin1 (example password: "admin1pass")
mvn -q compile exec:java -Dexec.mainClass="edu.univ.erp.util.HashPassword" -Dexec.args="admin1pass"

# Generate hash for inst1 (example password: "inst1pass")
mvn -q compile exec:java -Dexec.mainClass="edu.univ.erp.util.HashPassword" -Dexec.args="inst1pass"

# Generate hash for stu1 (example password: "stu1pass")
mvn -q compile exec:java -Dexec.mainClass="edu.univ.erp.util.HashPassword" -Dexec.args="stu1pass"

# Generate hash for stu2 (example password: "stu2pass")
mvn -q compile exec:java -Dexec.mainClass="edu.univ.erp.util.HashPassword" -Dexec.args="stu2pass"
```

**Or use the helper script:**
```powershell
.\generate-all-hashes.ps1
```

The script will automatically use global Maven if available, or fall back to the wrapper.

Each command will output a BCrypt hash (starts with `$2a$12$...`). Copy each hash.

#### Example Output

```
$2a$12$KIXxY9Z8qJ3vN5mH2pL8qOeXyZ1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P
```

---

### Step 5: Update seed_data.sql

Open `sql/seed_data.sql` and replace each `<BCRYPT-HASH-HERE>` placeholder with the corresponding hash generated in Step 4.

**Example replacement:**

**Before:**
```sql
(1001, 'admin1', 'admin', '<BCRYPT-HASH-HERE>', 'active'),
```

**After:**
```sql
(1001, 'admin1', 'admin', '$2a$12$KIXxY9Z8qJ3vN5mH2pL8qOeXyZ1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P', 'active'),
```

**Important:** Replace ALL four placeholders (one for each user: admin1, inst1, stu1, stu2).

---

### Step 6: Insert Seed Data

After replacing all password hash placeholders, run the seed data script:

**On Linux/Mac (bash):**
```bash
mysql -u root -p < sql/seed_data.sql
```

**On Windows (PowerShell):**
```powershell
Get-Content sql/seed_data.sql | mysql -u root -p
```

**Or using cmd.exe:**
```cmd
mysql -u root -p < sql\seed_data.sql
```

Or in MySQL Workbench:
1. Open `sql/seed_data.sql`
2. Execute the script (it will switch between `auth_db` and `erp_db` automatically)

**Expected output:** 
- 4 users inserted into `auth_db.users_auth`
- 2 students, 1 instructor inserted into `erp_db`
- 1 course, 1 section, 1 enrollment, 1 setting inserted

---

## Using init_all.sql (Optional)

The `sql/init_all.sql` file is a wrapper script that attempts to source all schema creation scripts. However:

⚠️ **WARNING:** `init_all.sql` does NOT include `seed_data.sql` because seed data requires password hash replacement first.

**If you want to use init_all.sql:**

1. Ensure you're in the `sql/` directory when running it, or adjust SOURCE paths
2. **On Linux/Mac:** `mysql -u root -p < sql/init_all.sql`
3. **On Windows (PowerShell):** `Get-Content sql/init_all.sql | mysql -u root -p`
4. **On Windows (cmd):** `mysql -u root -p < sql\init_all.sql`
5. This will create databases and schemas only
6. Then follow Steps 4-6 above to generate hashes and insert seed data

**Recommended approach:** Run scripts individually (Steps 1-3, then 4-6) for better control and error handling.

---

## Verification

After completing all steps, verify the data:

```bash
# Check auth_db users
mysql -u root -p -e "SELECT user_id, username, role, status FROM auth_db.users_auth;"

# Check erp_db students
mysql -u root -p -e "SELECT * FROM erp_db.students;"

# Check erp_db courses and sections
mysql -u root -p -e "SELECT * FROM erp_db.courses; SELECT * FROM erp_db.sections;"

# Check enrollments
mysql -u root -p -e "SELECT * FROM erp_db.enrollments;"

# Check settings
mysql -u root -p -e "SELECT * FROM erp_db.settings;"
```

---

## Seed Data Reference

The seed data includes:

- **Users (auth_db.users_auth):**
  - `admin1` (user_id: 1001, role: admin)
  - `inst1` (user_id: 1002, role: instructor)
  - `stu1` (user_id: 1003, role: student)
  - `stu2` (user_id: 1004, role: student)

- **Students (erp_db.students):**
  - `stu1` (roll_no: STU001, program: Computer Science, year: 2)
  - `stu2` (roll_no: STU002, program: Computer Science, year: 2)

- **Instructor (erp_db.instructors):**
  - `inst1` (department: Computer Science)

- **Course (erp_db.courses):**
  - CS101 - Intro to Programming (4 credits)

- **Section (erp_db.sections):**
  - CS101 section (instructor: inst1, capacity: 30, Fall 2024)

- **Enrollment (erp_db.enrollments):**
  - stu2 enrolled in CS101 section

- **Settings (erp_db.settings):**
  - maintenance = false

**Note:** User IDs are explicitly set to 1001-1004 for clarity and consistent references across tables.

---

## Cleaning Up Seed Data

If you inserted seed data with placeholder password hashes or want to start fresh:

### Option 1: Delete Only Seed Data (Recommended)

Run the cleanup script to remove only the seed data:

```powershell
# Using helper script
.\run-sql.ps1 -SqlFile sql/cleanup_seed_data.sql

# Or directly
Get-Content sql/cleanup_seed_data.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

This will delete:
- Users: admin1, inst1, stu1, stu2 (user_ids 1001-1004)
- All related student, instructor, course, section, enrollment, and grade records
- The maintenance setting

### Option 2: Drop All Tables (Nuclear Option)

If you want to completely start over and recreate all tables:

```powershell
.\run-sql.ps1 -SqlFile sql/drop_all_tables.sql
```

Then re-run the schema creation scripts:
```powershell
.\run-sql.ps1 -SqlFile sql/auth_schema.sql -Database auth_db
.\run-sql.ps1 -SqlFile sql/erp_schema.sql -Database erp_db
```

### Option 3: Drop Entire Databases

To completely remove both databases and start from scratch:

```sql
DROP DATABASE IF EXISTS auth_db;
DROP DATABASE IF EXISTS erp_db;
```

Then re-run `create_databases.sql` and all schema scripts.

---

## Troubleshooting

### Error: "mysql is not recognized"
- MySQL is not in your system PATH
- **Option 1:** Add MySQL to PATH:
  1. Find MySQL installation (usually `C:\Program Files\MySQL\MySQL Server X.X\bin`)
  2. Add that path to your system PATH environment variable
  3. Restart PowerShell/terminal
- **Option 2:** Use full path to mysql.exe (see Quick Reference section above)
- **Option 3:** Use MySQL Workbench GUI instead (File → Open SQL Script)

### Error: "Access denied"
- Ensure you're using the correct MySQL user with sufficient privileges
- Try: `mysql -u root -p` and enter password when prompted
- If using full path: `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p`

### Error: "Table already exists"
- Drop existing databases if starting fresh:
  ```sql
  DROP DATABASE IF EXISTS auth_db;
  DROP DATABASE IF EXISTS erp_db;
  ```
- Then re-run the creation scripts

### HashPassword utility not found
- Ensure Maven dependencies are downloaded: `mvn clean compile`
- Check that jBCrypt is in pom.xml (it should be)

### Foreign key errors
- The schemas use logical foreign keys (not enforced by MySQL)
- If you have `foreign_key_checks` enabled, you may need to disable it temporarily:
  ```sql
  SET FOREIGN_KEY_CHECKS = 0;
  -- run your script
  SET FOREIGN_KEY_CHECKS = 1;
  ```

---

## Security Notes

- **Never commit real password hashes to version control**
- The seed_data.sql file uses placeholders specifically to prevent accidental exposure
- Always generate fresh hashes for production environments
- Consider using environment variables or secure configuration management for production passwords


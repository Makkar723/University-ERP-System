## How to Run – Univ ERP (Java + Swing)

### Requirements

- **Java**: 17 (project uses `maven.compiler.source/target = 17`)
- **Maven**: 3.6+ (tested with 3.9.x)
- **MySQL**: 8.x server running locally

### Database Setup

From the `univ-erp` directory, create the databases and run the schemas/seed data:

```bash
# Create databases (auth_db, erp_db)
mysql -u root -p < sql/create_databases.sql

# Create schemas
mysql -u root -p auth_db < sql/auth_schema.sql
mysql -u root -p erp_db  < sql/erp_schema.sql

# Insert seed data (users, students, instructor, course, section, enrollments, settings)
mysql -u root -p < sql/seed_data.sql
```

Scripts used:
- `sql/create_databases.sql` – creates `auth_db` and `erp_db`
- `sql/auth_schema.sql` – auth schema (e.g., `users_auth`, `auth_failed_logins`)
- `sql/erp_schema.sql` – ERP schema (students, instructors, courses, sections, enrollments, grades, settings)
- `sql/seed_data.sql` – inserts sample users and ERP data

On Windows PowerShell you can alternatively use the helper script:

```powershell
cd univ-erp
.\run-sql.ps1 -SqlFile sql/create_databases.sql
.\run-sql.ps1 -SqlFile sql/auth_schema.sql -Database auth_db
.\run-sql.ps1 -SqlFile sql/erp_schema.sql -Database erp_db
.\run-sql.ps1 -SqlFile sql/seed_data.sql
```

### Application Configuration

Database connection settings are in `src/main/resources/application.properties`:

```properties
auth.db.url=jdbc:mysql://localhost:3306/auth_db
auth.db.user=root
auth.db.password=YOUR_PASSWORD

erp.db.url=jdbc:mysql://localhost:3306/erp_db
erp.db.user=root
erp.db.password=YOUR_PASSWORD
```

If your DB host, user, or password differ, update these values before running the app.

### Build & Run

From the `univ-erp` directory:

```bash
mvn -DskipTests package
java -jar target/univ-erp-1.0.0-SNAPSHOT.jar
```

Alternatively, you can run via Maven directly (recommended during development):

```bash
mvn compile exec:java -Dexec.mainClass="edu.univ.erp.Main"
```

Or open the project in your IDE (IntelliJ IDEA, VS Code, etc.) and run the main class:

- `edu.univ.erp.Main`

### Default Accounts

After running `sql/seed_data.sql`, the following users are available:

- **admin1** — role: **admin** — password: `admin1pass`
- **inst1** — role: **instructor** — password: `inst1pass`
- **stu1** — role: **student** — password: `stu1pass`
- **stu2** — role: **student** — password: `stu2pass`

Passwords are stored in the database as BCrypt hashes generated with the `edu.univ.erp.util.HashPassword` utility; the plain-text values above are what were used when generating those hashes.

### Notes / Troubleshooting

- **Login fails**: Verify MySQL is running, the URLs/users/passwords in `application.properties` are correct, and all scripts in `sql/` ran without errors.
- **Tables or data missing**: Re-run `create_databases.sql`, `auth_schema.sql`, `erp_schema.sql`, then `seed_data.sql` in that order.
- **MySQL not found**: Either add MySQL to your PATH or use the full path to `mysql.exe`, or use the provided `run-sql.ps1` helper script (see `RUN_SQL.md` for details).



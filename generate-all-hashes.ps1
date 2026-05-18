# PowerShell script to generate all password hashes at once
# Run this from the univ-erp directory

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Generating BCrypt Password Hashes" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Try to find Maven - prefer global installation, fall back to wrapper
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    $mvnPath = "mvn"
    Write-Host "Using global Maven: $($mvnCmd.Source)" -ForegroundColor Green
} elseif (Test-Path ".\maven-temp\bin\mvn.cmd") {
    $mvnPath = ".\maven-temp\bin\mvn.cmd"
    Write-Host "Using Maven wrapper (global Maven not in PATH)" -ForegroundColor Yellow
} else {
    Write-Host "ERROR: Maven not found. Please:" -ForegroundColor Red
    Write-Host "  1. Add Maven to your PATH and restart terminal, or" -ForegroundColor Yellow
    Write-Host "  2. Ensure maven-temp directory exists" -ForegroundColor Yellow
    exit 1
}

$passwords = @{
    "admin1" = "admin1pass"
    "inst1" = "inst1pass"
    "stu1" = "stu1pass"
    "stu2" = "stu2pass"
}

Write-Host "Generating hashes for default passwords..." -ForegroundColor Yellow
Write-Host "(You can change the passwords in this script if needed)" -ForegroundColor Gray
Write-Host ""

foreach ($user in $passwords.Keys) {
    $password = $passwords[$user]
    Write-Host "$user :" -ForegroundColor Yellow -NoNewline
    Write-Host " (password: $password)" -ForegroundColor Gray
    
    $hash = & $mvnPath -q compile exec:java -Dexec.mainClass="edu.univ.erp.util.HashPassword" -Dexec.args=$password 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Hash: $hash" -ForegroundColor Green
    } else {
        Write-Host "  ERROR generating hash for $user" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Copy each hash above" -ForegroundColor Yellow
Write-Host "2. Open sql/seed_data.sql" -ForegroundColor Yellow
Write-Host "3. Replace <BCRYPT-HASH-HERE> placeholders with the hashes" -ForegroundColor Yellow
Write-Host "4. Save and run: .\run-sql.ps1 -SqlFile sql/seed_data.sql" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan



# Maven Build Helper Script for University ERP Project
# This script helps build the project and provides helpful error messages

Write-Host "University ERP - Build Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check for Maven - prefer global installation, fall back to wrapper
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue

if ($mvnCmd) {
    Write-Host "Maven found: $($mvnCmd.Source)" -ForegroundColor Green
    $mvnCommand = "mvn"
} elseif (Test-Path ".\maven-temp\bin\mvn.cmd") {
    Write-Host "Using Maven wrapper (global Maven not in PATH)" -ForegroundColor Yellow
    $mvnCommand = ".\maven-temp\bin\mvn.cmd"
} else {
    Write-Host "ERROR: Maven is not installed or not in your PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "To install Maven:" -ForegroundColor Yellow
    Write-Host "1. Download from: https://maven.apache.org/download.cgi" -ForegroundColor White
    Write-Host "2. Extract to a directory (e.g., C:\Program Files\Apache\maven)" -ForegroundColor White
    Write-Host "3. Add the 'bin' directory to your system PATH" -ForegroundColor White
    Write-Host "4. Restart your terminal and try again" -ForegroundColor White
    Write-Host ""
    Write-Host "Or install via Chocolatey (if available):" -ForegroundColor Yellow
    Write-Host "  choco install maven" -ForegroundColor White
    Write-Host ""
    exit 1
}
Write-Host ""

# Check Java version
$javaVersion = java -version 2>&1 | Select-Object -First 1
Write-Host "Java: $javaVersion" -ForegroundColor Green
Write-Host ""

# Build the project
Write-Host "Building project..." -ForegroundColor Cyan
Write-Host ""

& $mvnCommand clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Build successful!" -ForegroundColor Green
    Write-Host "JAR file location: target\univ-erp-1.0.0-SNAPSHOT.jar" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "Build failed. Check the error messages above." -ForegroundColor Red
    exit $LASTEXITCODE
}

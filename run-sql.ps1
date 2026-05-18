# PowerShell helper script to run SQL files with MySQL
# Automatically detects MySQL installation if not in PATH

param(
    [Parameter(Mandatory=$true)]
    [string]$SqlFile,
    
    [string]$Database = "",
    
    [string]$User = "root",
    
    [string]$MysqlPath = ""
)

# Find MySQL if not provided and not in PATH
if ([string]::IsNullOrEmpty($MysqlPath)) {
    $mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
    if ($mysqlCmd) {
        $MysqlPath = $mysqlCmd.Source
        Write-Host "Using MySQL from PATH: $MysqlPath" -ForegroundColor Green
    } else {
        # Try common installation locations
        $commonPaths = @(
            "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
            "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
            "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
            "C:\xampp\mysql\bin\mysql.exe"
        )
        
        foreach ($path in $commonPaths) {
            if (Test-Path $path) {
                $MysqlPath = $path
                Write-Host "Found MySQL at: $MysqlPath" -ForegroundColor Green
                break
            }
        }
        
        if ([string]::IsNullOrEmpty($MysqlPath)) {
            Write-Host "ERROR: MySQL not found. Please:" -ForegroundColor Red
            Write-Host "  1. Add MySQL to your PATH, or" -ForegroundColor Yellow
            Write-Host "  2. Specify -MysqlPath parameter, or" -ForegroundColor Yellow
            Write-Host "  3. Use MySQL Workbench GUI" -ForegroundColor Yellow
            exit 1
        }
    }
}

# Resolve SQL file path (handle relative paths)
$resolvedSqlFile = $SqlFile
if (-not [System.IO.Path]::IsPathRooted($SqlFile)) {
    # If relative path, try current directory first, then sql/ subdirectory
    if (Test-Path $SqlFile) {
        $resolvedSqlFile = (Resolve-Path $SqlFile).Path
    } elseif (Test-Path "sql\$SqlFile") {
        $resolvedSqlFile = (Resolve-Path "sql\$SqlFile").Path
    } elseif (Test-Path "univ-erp\sql\$SqlFile") {
        $resolvedSqlFile = (Resolve-Path "univ-erp\sql\$SqlFile").Path
    }
}

# Check if SQL file exists
if (-not (Test-Path $resolvedSqlFile)) {
    Write-Host "ERROR: SQL file not found: $SqlFile" -ForegroundColor Red
    Write-Host "Searched in: $(Get-Location)" -ForegroundColor Yellow
    Write-Host "Make sure you're in the univ-erp directory or provide the full path." -ForegroundColor Yellow
    exit 1
}

# Build MySQL command
$mysqlArgs = @("-u", $User, "-p")
if (-not [string]::IsNullOrEmpty($Database)) {
    $mysqlArgs += $Database
}

Write-Host "Executing: $resolvedSqlFile" -ForegroundColor Cyan
if (-not [string]::IsNullOrEmpty($Database)) {
    Write-Host "Database: $Database" -ForegroundColor Cyan
}
Write-Host ""

# Execute SQL file
Get-Content $resolvedSqlFile | & $MysqlPath $mysqlArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nSQL script executed successfully!" -ForegroundColor Green
} else {
    Write-Host "`nSQL script execution failed. Check errors above." -ForegroundColor Red
    exit $LASTEXITCODE
}


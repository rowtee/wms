# ============================================================
# WMS Auto-Setup & Launcher Script for Windows PowerShell
# Repository: https://github.com/rowtee/wms.git (Public)
# ============================================================
$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Workshop Management System (WMS) - Automated Setup" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Verify Docker is running
try {
    $null = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[-] Docker is not running. Please start Docker Desktop and try again." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[-] Docker CLI was not found. Please install Docker Desktop: https://www.docker.com/products/docker-desktop/" -ForegroundColor Red
    exit 1
}

# 2. Check if inside repository or clone from public GitHub
$RepoUrl = "https://github.com/rowtee/wms.git"
$TargetDir = "wms"

if (Test-Path -Path "pom.xml" -PathType Leaf) {
    Write-Host "[+] Running inside WMS directory." -ForegroundColor Green
} elseif (Test-Path -Path $TargetDir -PathType Container) {
    Write-Host "[+] Directory '$TargetDir' found. Updating from remote..." -ForegroundColor Green
    Set-Location $TargetDir
    if (Get-Command git -ErrorAction SilentlyContinue) {
        git pull origin main
    }
} else {
    Write-Host "[+] Cloning public repository from $RepoUrl..." -ForegroundColor Green
    if (Get-Command git -ErrorAction SilentlyContinue) {
        git clone $RepoUrl $TargetDir
        Set-Location $TargetDir
    } else {
        Write-Host "[!] Git command not found on host. Attempting Docker Compose remote build..." -ForegroundColor Yellow
    }
}

# 3. Detect available web application port (default 8080, fallback to 8081, 8082, etc.)
function Test-PortInUse([int]$Port) {
    $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    return ($null -ne $connections)
}

$ChosenPort = 8080
if (Test-PortInUse $ChosenPort) {
    Write-Host "[!] Port 8080 is already in use by a local process (e.g. local Tomcat/IDE)." -ForegroundColor Yellow
    $candidate = 8081
    while (Test-PortInUse $candidate) {
        $candidate++
    }
    $ChosenPort = $candidate
    Write-Host "[+] Automatically switching application port to $ChosenPort." -ForegroundColor Green
}
$env:APP_PORT = "$ChosenPort"

# 4. Spin up Docker containers
Write-Host "[+] Starting WMS and PostgreSQL containers via Docker Compose (Port: $ChosenPort)..." -ForegroundColor Green
docker compose up --build -d

# 5. Display status and credentials
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  WMS is up and running!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  URL: http://localhost:$ChosenPort/" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Staff Demo Accounts:" -ForegroundColor Yellow
Write-Host "    - Front Desk:  najwa  / 123456"
Write-Host "    - Manager:     aina   / 123456"
Write-Host "    - Mechanics:   adam   / 123456 (Senior)"
Write-Host "                   ajim   / 123456"
Write-Host "                   suresh / 123456 (Electrical)"
Write-Host "                   chong  / 123456 (Junior)"
Write-Host "============================================================" -ForegroundColor Green

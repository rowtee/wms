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

# 3. Spin up Docker containers
Write-Host "[+] Starting WMS and PostgreSQL containers via Docker Compose..." -ForegroundColor Green
docker compose up --build -d

# 4. Display status and credentials
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  WMS is up and running!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  URL: http://localhost:8080/" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Staff Demo Accounts:" -ForegroundColor Yellow
Write-Host "    - Front Desk:  najwa  / 123456"
Write-Host "    - Manager:     aina   / 123456"
Write-Host "    - Mechanics:   adam   / 123456 (Senior)"
Write-Host "                   ajim   / 123456"
Write-Host "                   suresh / 123456 (Electrical)"
Write-Host "                   chong  / 123456 (Junior)"
Write-Host "============================================================" -ForegroundColor Green

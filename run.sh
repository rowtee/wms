#!/usr/bin/env bash
# ============================================================
# WMS Auto-Setup & Launcher Script for Linux / macOS / WSL
# Repository: https://github.com/rowtee/wms.git (Public)
# ============================================================
set -e

echo "============================================================"
echo "  Workshop Management System (WMS) - Automated Setup"
echo "============================================================"

# 1. Verify Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "[-] Docker is not running or not installed."
    echo "    Please install/start Docker Desktop or Docker Engine and retry."
    exit 1
fi

REPO_URL="https://github.com/rowtee/wms.git"
TARGET_DIR="wms"

# 2. Check if inside repository or clone from public GitHub
if [ -f "pom.xml" ]; then
    echo "[+] Running inside existing repository directory."
elif [ -d "$TARGET_DIR" ]; then
    echo "[+] Found '$TARGET_DIR'. Pulling latest changes from remote..."
    cd "$TARGET_DIR"
    git pull origin main || true
else
    echo "[+] Cloning public repository from $REPO_URL..."
    git clone "$REPO_URL" "$TARGET_DIR"
    cd "$TARGET_DIR"
fi

# 3. Spin up Docker containers
echo "[+] Starting WMS and PostgreSQL containers via Docker Compose..."
docker compose up --build -d

# 4. Display status and credentials
echo ""
echo "============================================================"
echo "  WMS is up and running!"
echo "============================================================"
echo "  URL: http://localhost:8080/"
echo ""
echo "  Staff Demo Accounts:"
echo "    - Front Desk:  najwa  / 123456"
echo "    - Manager:     aina   / 123456"
echo "    - Mechanics:   adam   / 123456 (Senior)"
echo "                   ajim   / 123456"
echo "                   suresh / 123456 (Electrical)"
echo "                   chong  / 123456 (Junior)"
echo "============================================================"

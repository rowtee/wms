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

# 3. Detect free port starting from 8080
CHOSEN_PORT=8080
is_port_in_use() {
    (lsof -i :"$1" || nc -z 127.0.0.1 "$1" || ss -tlpn | grep -q ":$1 ") >/dev/null 2>&1
}

if is_port_in_use $CHOSEN_PORT; then
    echo "[!] Port 8080 is already in use by a local process."
    CANDIDATE=8081
    while is_port_in_use $CANDIDATE; do
        CANDIDATE=$((CANDIDATE + 1))
    done
    CHOSEN_PORT=$CANDIDATE
    echo "[+] Automatically switching application port to $CHOSEN_PORT."
fi
export APP_PORT="$CHOSEN_PORT"

# 4. Spin up Docker containers
echo "[+] Starting WMS and PostgreSQL containers via Docker Compose (Port: $CHOSEN_PORT)..."
docker compose up --build -d

# 5. Display status and credentials
echo ""
echo "============================================================"
echo "  WMS is up and running!"
echo "============================================================"
echo "  URL: http://localhost:$CHOSEN_PORT/"
echo ""
echo "  Staff Demo Accounts:"
echo "    - Front Desk:  najwa  / 123456"
echo "    - Manager:     aina   / 123456"
echo "    - Mechanics:   adam   / 123456 (Senior)"
echo "                   ajim   / 123456"
echo "                   suresh / 123456 (Electrical)"
echo "                   chong  / 123456 (Junior)"
echo "============================================================"

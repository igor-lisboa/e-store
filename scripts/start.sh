#!/bin/sh
# Starts the whole store with Docker Compose: database, migrations, build (without tests), and the web server.
# Usage: sh scripts/start.sh [docker compose up options, for example -d]
# To run the tests use scripts/test.sh instead.
set -eu

cd "$(dirname "$0")/.."
if [ ! -f .env ]; then
    cp .env.example .env
fi

# The port can come from the shell, then .env, then the built-in default.
store_port="${MVN_PORT:-$(sed -n 's/^MVN_PORT=//p' .env | tail -n 1)}"
store_port="${store_port:-8181}"
store_url="http://localhost:${store_port}"

echo "Starting e-store. When it is ready, open: ${store_url}"
echo "(The first start builds the application and can take several minutes.)"
echo ""
echo "Startup stages:"
echo "  1. Database initialization (20-60s)"
echo "  2. Schema migration (Flyway)"
echo "  3. Maven build & package (2-10 min on first run, 30-60s thereafter)"
echo "  4. Webapp server startup"
echo ""

# Print the address again when the server really answers, so it is not lost in the build output.
announce_when_ready() {
    command -v curl >/dev/null 2>&1 || return 0
    attempts=0
    # Check both webapp startup and app readiness. /products is public and returns 200 (not a redirect).
    while [ "$attempts" -lt 360 ]; do
        # Try /products (no auth needed); if it responds 200, app is ready.
        if curl -fs -o /dev/null "${store_url}/products"; then
            echo ""
            echo "=============================================================="
            echo " e-store is ready: ${store_url}"
            echo " Sign in as admin@example.test or customer@example.test"
            echo " (password: development-password). Demo data only."
            echo "=============================================================="
            return 0
        fi
        attempts=$((attempts + 1))
        # Print progress every 30 seconds (6 attempts × 5s)
        if [ $((attempts % 6)) -eq 0 ]; then
            echo "Still starting... (waiting $(($attempts * 5))s)"
        fi
        sleep 5
    done
    echo ""
    echo "⚠ App did not respond after $(($attempts * 5))s. Check logs:"
    echo "  docker compose logs -f app"
    return 1
}

announce_when_ready &
watcher=$!
trap 'kill "$watcher" 2>/dev/null || true' EXIT INT TERM

docker compose up "$@"

# With -d, "up" returns at once; keep the script alive until the address has been announced.
case " $* " in
    *" -d "*|*" --detach "*) wait "$watcher" ;;
esac

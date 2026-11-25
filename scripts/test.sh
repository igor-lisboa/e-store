#!/bin/sh
# Runs the repository's checks. It never starts the store for normal use; scripts/start.sh does that.
#
# Usage: sh scripts/test.sh [suite ...]
#
# Suites (default: quick)
#   quick      unit + contracts + openapi             (needs Java 8, Maven, Python; no database)
#   unit       default Maven tests and packaging, with JaCoCo coverage
#   contracts  repository contract tests (Python)
#   openapi    OpenAPI schema validation (needs: python -m pip install -r scripts/requirements.txt)
#   mysql      Maven profile with real MySQL migration, checkout, and SQL tests
#   legacy     Maven profile with the older database and Cucumber scenarios
#   failure    packaged app against an unreachable database: errors must be generic
#   browser    real-browser journeys in both demo-checkout modes (needs Playwright, see README)
#   all        everything above
#
# Database suites (mysql, legacy, browser) need a DISPOSABLE MySQL schema named e_store_integration.
# Either export TEST_MYSQL_URL, TEST_MYSQL_USER and TEST_MYSQL_PASSWORD yourself, or leave them unset and
# this script starts a throwaway MySQL container (needs Docker; port TEST_DB_PORT, default 3317) and removes it.
set -u

cd "$(dirname "$0")/.."

# ---- tools -------------------------------------------------------------------------------------------------
PYTHON=""
for candidate in python3 python; do
    # Some systems ship a placeholder that exists but cannot run (for example the Windows Store alias).
    if command -v "$candidate" >/dev/null 2>&1 && "$candidate" -c "import sys" >/dev/null 2>&1; then
        PYTHON="$candidate"
        break
    fi
done
if ! command -v mvn >/dev/null 2>&1; then
    local_maven="$(ls -d .local-tools/apache-maven-*/bin 2>/dev/null | head -n 1)"
    [ -n "$local_maven" ] && PATH="$PWD/$local_maven:$PATH"
    local_jdk="$(ls -d .local-tools/jdk*/ 2>/dev/null | head -n 1)"
    if [ -z "${JAVA_HOME:-}" ] && [ -n "$local_jdk" ]; then
        JAVA_HOME="$PWD/$local_jdk"
        PATH="$JAVA_HOME/bin:$PATH"
        export JAVA_HOME
    fi
fi

# Optional Python packages bootstrapped into .local-tools (see README).
[ -d .local-tools/python ] && PYTHONPATH="$PWD/.local-tools/python${PYTHONPATH:+:$PYTHONPATH}" && export PYTHONPATH

# ---- bookkeeping -------------------------------------------------------------------------------------------
RESULTS=""
FAILED=0
DB_CONTAINER=""

record() { RESULTS="${RESULTS}$1|$2
"; [ "$2" = "PASS" ] || FAILED=1; }

step() {
    name="$1"
    shift
    echo ""
    echo "=== $name ==="
    if "$@"; then record "$name" PASS; else record "$name" FAIL; fi
}

skip() { echo ""; echo "=== $1 === SKIPPED: $2"; record "$1" "SKIPPED ($2)"; FAILED=1; }

cleanup() {
    if [ -n "$DB_CONTAINER" ]; then
        docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
    fi
}
trap cleanup EXIT INT TERM

# ---- disposable database -----------------------------------------------------------------------------------
ensure_test_database() {
    if [ -n "${TEST_MYSQL_URL:-}" ] && [ -n "${TEST_MYSQL_USER:-}" ] && [ -n "${TEST_MYSQL_PASSWORD:-}" ]; then
        return 0
    fi
    command -v docker >/dev/null 2>&1 || { echo "No TEST_MYSQL_* variables and no Docker to start a test database."; return 1; }
    port="${TEST_DB_PORT:-3317}"
    password="$(head -c 18 /dev/urandom | od -An -tx1 | tr -d ' \n')"
    DB_CONTAINER="e-store-test-db"
    docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
    echo "Starting a throwaway MySQL container on 127.0.0.1:${port} ..."
    docker run -d --name "$DB_CONTAINER" -e MYSQL_ROOT_PASSWORD="$password" -e MYSQL_DATABASE=e_store_integration \
        -e MYSQL_USER=store_tests -e MYSQL_PASSWORD="$password" -p "127.0.0.1:${port}:3306" \
        mysql:8.4.13 --skip-log-bin >/dev/null || { DB_CONTAINER=""; return 1; }
    attempts=0
    until docker exec -e MYSQL_PWD="$password" "$DB_CONTAINER" mysqladmin ping -h 127.0.0.1 -uroot --silent >/dev/null 2>&1; do
        attempts=$((attempts + 1))
        if [ "$attempts" -gt 60 ]; then echo "The test database did not become ready."; return 1; fi
        sleep 3
    done
    TEST_MYSQL_URL="jdbc:mysql://127.0.0.1:${port}/e_store_integration?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    TEST_MYSQL_USER=store_tests
    TEST_MYSQL_PASSWORD="$password"
    export TEST_MYSQL_URL TEST_MYSQL_USER TEST_MYSQL_PASSWORD
}

# ---- suites ------------------------------------------------------------------------------------------------
package_app() { mvn -B -DskipTests package; }

run_browser() {
    package_app || return 1
    case "$TEST_MYSQL_URL" in *\?*) url="$TEST_MYSQL_URL" ;; *) url="${TEST_MYSQL_URL}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" ;; esac
    status=0
    for demo in true false; do
        echo "--- browser journeys with ALLOW_DEMO_CHECKOUT=$demo"
        STORE_DATABASE_E2E=1 MYSQL_CONNECTION_STRING="$url" MYSQL_USER="$TEST_MYSQL_USER" \
            MYSQL_PASSWORD="$TEST_MYSQL_PASSWORD" ALLOW_DEMO_CHECKOUT="$demo" \
            "$PYTHON" scripts/run_browser_smoke.py || status=1
    done
    return $status
}

run_failure_check() { package_app && "$PYTHON" scripts/run_infrastructure_failure_check.py; }

# Installs the validator's packages into the ignored .local-tools/python folder when they are missing.
run_openapi() {
    if ! "$PYTHON" -c "import openapi_spec_validator" >/dev/null 2>&1; then
        echo "Installing OpenAPI validator packages into .local-tools/python ..."
        "$PYTHON" -m pip install -q --target .local-tools/python -r scripts/requirements.txt || return 1
        PYTHONPATH="$PWD/.local-tools/python${PYTHONPATH:+:$PYTHONPATH}"
        export PYTHONPATH
    fi
    "$PYTHON" scripts/validate_openapi.py
}

needs_python() { [ -n "$PYTHON" ] || { skip "$1" "Python was not found"; return 1; }; }

run_suite() {
    case "$1" in
        unit) step "unit (mvn verify)" mvn -B clean verify ;;
        contracts) needs_python contracts && step "contracts" "$PYTHON" scripts/test_repository_contracts.py ;;
        openapi) needs_python openapi && step "openapi" run_openapi ;;
        mysql)
            if ensure_test_database; then step "mysql (real MySQL)" mvn -B -Pmysql-integration clean verify
            else skip mysql "no test database"; fi ;;
        legacy)
            if ensure_test_database; then step "legacy (database + Cucumber)" mvn -B -Plegacy-integration test
            else skip legacy "no test database"; fi ;;
        failure) needs_python failure && step "failure (unreachable database)" run_failure_check ;;
        browser)
            if ! needs_python browser; then return; fi
            if ensure_test_database; then step "browser (both demo modes)" run_browser
            else skip browser "no test database"; fi ;;
        *) echo "Unknown suite: $1 (use quick, unit, contracts, openapi, mysql, legacy, failure, browser, all)"; FAILED=1 ;;
    esac
}

[ "$#" -eq 0 ] && set -- quick
for suite in "$@"; do
    case "$suite" in
        quick) for part in unit contracts openapi; do run_suite "$part"; done ;;
        all) for part in unit contracts openapi mysql legacy failure browser; do run_suite "$part"; done ;;
        *) run_suite "$suite" ;;
    esac
done

echo ""
echo "================ Summary ================"
printf '%s' "$RESULTS" | while IFS='|' read -r name outcome; do
    [ -n "$name" ] && printf '  %-34s %s\n' "$name" "$outcome"
done
[ "$FAILED" -eq 0 ] && echo "All requested checks passed." || echo "Some checks failed or were skipped."
exit "$FAILED"

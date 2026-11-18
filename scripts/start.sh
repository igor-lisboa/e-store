#!/bin/sh
set -eu

cd "$(dirname "$0")/.."
if [ ! -f .env ]; then
    cp .env.example .env
fi
exec docker compose up "$@"

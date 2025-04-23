#!/usr/bin/env bash
set -euo pipefail

# ./health_check.sh <ipServidor> [puerto]
IP="${1:-localhost}"
PUERTO="${2:-5555}"

cd "$(dirname "$0")/.."

echo "[health_check] Compilando proyecto..."
mvn compile

echo "[health_check] Iniciando HealthChecker contra $IP:$PUERTO..."
mvn exec:java \
  -Dexec.mainClass="tolerancia.HealthChecker" \
  -Dexec.args="$IP $PUERTO"

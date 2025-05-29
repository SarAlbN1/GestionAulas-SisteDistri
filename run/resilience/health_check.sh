#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el HealthChecker que supervisa al Servidor Central
# Uso: ./health_check.sh <ipServidor> [puerto]

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "Uso: $0 <ipServidor> [puerto]"
  exit 1
fi

IP="$1"
PUERTO="${2:-5555}"

cd "$(dirname "$0")/.."

echo "[health_check] Ejecutando HealthChecker contra $IP:$PUERTO..."

mvn clean compile

mvn exec:java \
  -Dexec.mainClass="tolerancia.HealthChecker" \
  -Dexec.args="$IP $PUERTO" \
  -Dexec.cleanupDaemonThreads=false

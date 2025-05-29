#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Réplica de respaldo
# Uso: ./run_backup.sh <ipServidorPrincipal> [puertoPrincipal]

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "Uso: $0 <ipServidorPrincipal> [puertoPrincipal]"
  exit 1
fi

IP="$1"
PUERTO="${2:-5555}"

cd "$(dirname "$0")/.."

echo "[run_backup] Iniciando Servidor Réplica contra $IP:$PUERTO..."

mvn clean compile

mvn exec:java \
  -Dexec.mainClass="tolerancia.ServidorReplica" \
  -Dexec.args="$IP $PUERTO" \
  -Dexec.cleanupDaemonThreads=false

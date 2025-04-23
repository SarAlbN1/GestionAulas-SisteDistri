#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Réplica de respaldo
# Uso: ./run_backup.sh <ipServidorPrincipal> [puertoPrincipal]
IP="${1:-localhost}"
PUERTO="${2:-5555}"

cd "$(dirname "$0")/.."

echo "[run_backup] Compilando proyecto..."
mvn compile

echo "[run_backup] Iniciando Servidor Réplica contra $IP:$PUERTO..."
mvn exec:java \
  -Dexec.mainClass="tolerancia.ServidorReplica" \
  -Dexec.args="$IP $PUERTO"

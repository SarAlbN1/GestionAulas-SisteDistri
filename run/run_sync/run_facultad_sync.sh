#!/usr/bin/env bash
set -euo pipefail

# Arranca una Facultad en modo síncrono (REP/REQ)
# Uso: ./run_facultad_sync.sh <ipServidor>
if [ $# -ne 1 ]; then
  echo "Uso: $0 <ipServidor>"
  exit 1
fi
IP_SERVIDOR=$1

cd "$(dirname "$0")/.."

echo "[run_facultad_sync] Compilando proyecto..."
mvn compile

echo "[run_facultad_sync] Iniciando Facultad (REQ→REP) contra $IP_SERVIDOR:5555..."
mvn exec:java \
  -Dexec.mainClass="facultades.FacultadSync" \
  -Dexec.args="$IP_SERVIDOR"

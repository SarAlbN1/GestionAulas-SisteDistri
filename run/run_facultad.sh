#!/usr/bin/env bash
set -euo pipefail

# Ejecuta una Facultad
# Uso: ./run_facultad.sh <nombreFacultad> <ipServidor>

if [[ $# -ne 2 ]]; then
  echo "Uso: $0 <nombreFacultad> <ipServidor>"
  exit 1
fi

cd "$(dirname "$0")/.."

FACULTAD="$1"
IP_SERVIDOR="$2"

echo "[run_facultad] Iniciando Facultad '$FACULTAD' conectándose a $IP_SERVIDOR..."
mvn exec:java \
  -Dexec.mainClass="facultades.Facultad" \
  -Dexec.args="\"$FACULTAD\" \"$IP_SERVIDOR\"" \
  -Dexec.cleanupDaemonThreads=false

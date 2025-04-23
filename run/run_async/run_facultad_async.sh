#!/usr/bin/env bash
set -euo pipefail

# Arranca una Facultad en modo asíncrono (DEALER)
# Uso: ./run_facultad_async.sh <ipServidor>
if [ $# -ne 1 ]; then
  echo "Uso: $0 <ipServidor>"
  exit 1
fi
IP_SERVIDOR=$1

cd "$(dirname "$0")/.."

echo "[run_facultad_async] Compilando proyecto..."
mvn compile

echo "[run_facultad_async] Iniciando Facultad (DEALER) contra $IP_SERVIDOR:5555..."
mvn exec:java \
  -Dexec.mainClass="facultades.FacultadAsync" \
  -Dexec.args="$IP_SERVIDOR"

#!/usr/bin/env bash
set -euo pipefail

# Ejecuta una Facultad en modo asíncrono (DEALER)
# Uso: ./run_facultad.sh <ipServidor>
if [ $# -ne 1 ]; then
  echo "Uso: $0 <ipServidor>"
  exit 1
fi
IP_SERVIDOR=$1

cd "$(dirname "$0")/.."

echo "[run_facultad] Compilando proyecto..."
mvn compile

echo "[run_facultad] Iniciando Facultad (DEALER) contra $IP_SERVIDOR:5555..."
mvn exec:java \
  -Dexec.mainClass="facultades.Facultad" \
  -Dexec.args="$IP_SERVIDOR"

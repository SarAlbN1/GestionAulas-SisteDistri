#!/usr/bin/env bash
set -euo pipefail

# ./run_facultad.sh <sync|async> <ipServidor>
if [ $# -ne 2 ]; then
  echo "Uso: $0 <sync|async> <ipServidor>"
  exit 1
fi
MODE=$1; IP=$2

cd "$(dirname "$0")/.."

echo "[run_facultad] Compilando proyecto..."
mvn compile

echo "[run_facultad][$MODE] Iniciando Facultad en modo $MODE contra $IP:5555..."
mvn exec:java \
  -Dexec.mainClass="facultades.Facultad" \
  -Dexec.args="$MODE $IP"

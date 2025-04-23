#!/usr/bin/env bash
set -euo pipefail

# ./run_servidor.sh <sync|async>
if [ $# -ne 1 ]; then
  echo "Uso: $0 <sync|async>"
  exit 1
fi
MODE=$1

cd "$(dirname "$0")/.."

echo "[run_servidor] Compilando proyecto..."
mvn clean compile

echo "[run_servidor][$MODE] Iniciando Servidor en modo $MODE..."
mvn exec:java \
  -Dexec.mainClass="servidor.Servidor" \
  -Dexec.args="$MODE"

#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Central (ROUTER asíncrono)
# Uso: ./run_servidor.sh

cd "$(dirname "$0")/.."
PUERTO=5555

echo "[run_servidor] Iniciando servidor central en puerto $PUERTO..."

mvn clean compile

mvn exec:java \
  -Dexec.mainClass="servidor.Servidor" \
  -Dexec.cleanupDaemonThreads=false

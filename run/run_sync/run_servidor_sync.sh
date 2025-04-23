#!/usr/bin/env bash
set -euo pipefail

# Arranca el Servidor Central en modo síncrono (REQ/REP)
# Uso: ./run_servidor_sync.sh

cd "$(dirname "$0")/.."

echo "[run_servidor_sync] Compilando proyecto..."
mvn clean compile

echo "[run_servidor_sync] Iniciando Servidor Central (REP) en el puerto 5555..."
mvn exec:java \
  -Dexec.mainClass="servidor.ServidorSync" \
  -Dexec.cleanupDaemonThreads=false

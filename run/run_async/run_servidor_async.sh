#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Central (ROUTER asíncrono)
# Uso: ./run_servidor.sh

cd "$(dirname "$0")/.."

echo "[run_servidor] Limpiando y compilando proyecto..."
mvn clean compile

echo "[run_servidor] Iniciando Servidor Central (ROUTER asíncrono) en el puerto 5555..."
mvn exec:java \
  -Dexec.mainClass="servidor.Servidor" \
  -Dexec.cleanupDaemonThreads=false

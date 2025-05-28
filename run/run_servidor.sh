#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Central (ROUTER asíncrono)
# Uso: ./run_servidor.sh

cd "$(dirname "$0")/.."
PUERTO=5555
IP_LOCAL="DEFINIR_IP_LOCAL_MANUALMENTE"

echo "[run_servidor] IP local: $IP_LOCAL"
echo "[run_servidor] Escuchando en puerto $PUERTO..."

mvn clean compile

mvn exec:java \
  -Dexec.mainClass="servidor.Servidor" \
  -Dexec.cleanupDaemonThreads=false

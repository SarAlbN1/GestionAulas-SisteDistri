#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Central (ROUTER asíncrono)
# Uso: ./run_servidor.sh

cd "$(dirname "$0")/.."
PUERTO=5555
IP_LOCAL="10.43.102.242" # Cambia esto a la IP local de tu máquina

echo "[run_servidor] IP local: $IP_LOCAL"
echo "[run_servidor] Escuchando en puerto $PUERTO..."

mvn clean compile

mvn exec:java \
  -Dexec.mainClass="servidor.Servidor" \
  -Dexec.cleanupDaemonThreads=false

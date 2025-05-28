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
PUERTO=5555
IP_LOCAL="DEFINIR_IP_LOCAL_MANUALMENTE"

echo "[run_facultad] IP local: $IP_LOCAL"
echo "[run_facultad] Conectando a servidor en $IP_SERVIDOR:$PUERTO..."
ping -c 1 "$IP_SERVIDOR" > /dev/null || {
  echo "❌ No se puede contactar al servidor en $IP_SERVIDOR"
  exit 1
}

mvn exec:java \
  -Dexec.mainClass="facultades.Facultad" \
  -Dexec.args="\"$FACULTAD\" \"$IP_SERVIDOR\"" \
  -Dexec.cleanupDaemonThreads=false

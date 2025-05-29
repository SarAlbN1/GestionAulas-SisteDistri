#!/usr/bin/env bash
set -euo pipefail

# Ejecuta una Facultad
# Uso: ./run_facultad.sh <nombreFacultad> <ipServidorPrincipal> <ipServidorReplica>

if [[ $# -ne 3 ]]; then
  echo "Uso: $0 <nombreFacultad> <ipServidorPrincipal> <ipServidorReplica>"
  exit 1
fi

cd "$(dirname "$0")/.."

FACULTAD="$1"
IP_SERVIDOR_PRINCIPAL="$2"
IP_SERVIDOR_REPLICA="$3"

echo "[run_facultad] Ejecutando Facultad '$FACULTAD' con servidor principal $IP_SERVIDOR_PRINCIPAL y réplica $IP_SERVIDOR_REPLICA..."

mvn exec:java \
  -Dexec.mainClass="facultades.Facultad" \
  -Dexec.args="\"$FACULTAD\" \"$IP_SERVIDOR_PRINCIPAL\" \"$IP_SERVIDOR_REPLICA\"" \
  -Dexec.cleanupDaemonThreads=false

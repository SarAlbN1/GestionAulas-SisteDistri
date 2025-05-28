#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el Servidor Réplica de respaldo
# Uso: ./run_backup.sh <ipServidorPrincipal> [puertoPrincipal]
IP="${1:-localhost}"
PUERTO="${2:-5555}"
IP_LOCAL="DEFINIR_IP_LOCAL_MANUALMENTE"

cd "$(dirname "$0")/.."

echo "[run_backup] IP local: $IP_LOCAL"
echo "[run_backup] Verificando conectividad a $IP:$PUERTO..."
ping -c 1 "$IP" > /dev/null || {
  echo "❌ No se puede contactar al servidor principal en $IP"
  exit 1
}

mvn compile

echo "[run_backup] Iniciando Servidor Réplica contra $IP:$PUERTO..."
mvn exec:java \
  -Dexec.mainClass="tolerancia.ServidorReplica" \
  -Dexec.args="$IP $PUERTO"

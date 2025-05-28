#!/usr/bin/env bash
set -euo pipefail

# Ejecuta el HealthChecker que supervisa al Servidor Central
# Uso: ./health_check.sh <ipServidor> [puerto]
IP="${1:-localhost}"
PUERTO="${2:-5555}"
IP_LOCAL="DEFINIR_IP_LOCAL_MANUALMENTE"

cd "$(dirname "$0")/.."

echo "[health_check] IP local: $IP_LOCAL"
echo "[health_check] Verificando conectividad a $IP:$PUERTO..."
ping -c 1 "$IP" > /dev/null || {
  echo "❌ No se puede contactar al servidor en $IP"
  exit 1
}

mvn compile

echo "[health_check] Iniciando HealthChecker contra $IP:$PUERTO..."
mvn exec:java \
  -Dexec.mainClass="tolerancia.HealthChecker" \
  -Dexec.args="$IP $PUERTO"

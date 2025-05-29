#!/usr/bin/env bash
set -euo pipefail

# Ejecuta un Programa Académico
# Uso: ./run_programa.sh <nombrePrograma> <nombreFacultad> <semestre> <salones> <laboratorios> <ipFacultad>

if [[ $# -ne 6 ]]; then
  echo "Uso: $0 <nombrePrograma> <nombreFacultad> <semestre> <salones> <laboratorios> <ipFacultad>"
  exit 1
fi

cd "$(dirname "$0")/.."

PROGRAMA="$1"
FACULTAD="$2"
SEMESTRE="$3"
SALONES="$4"
LABS="$5"
IP_FACULTAD="$6"
PUERTO=6000
IP_LOCAL="DEFINIR_IP_LOCAL_MANUALMENTE"

echo "[run_programa] IP local: $IP_LOCAL"
echo "[run_programa] Conectando a facultad $FACULTAD en $IP_FACULTAD:$PUERTO..."
ping -n 1 "$IP_FACULTAD" > /dev/null || {
  echo "❌ No se puede contactar a la facultad en $IP_FACULTAD"
  exit 1
}

mvn exec:java \
  -Dexec.mainClass="programas.ProgramaAcademico" \
  -Dexec.args="\"$PROGRAMA\" \"$FACULTAD\" $SEMESTRE $SALONES $LABS \"$IP_FACULTAD\"" \
  -Dexec.cleanupDaemonThreads=false

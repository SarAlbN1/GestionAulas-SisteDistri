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

echo "[run_programa] Ejecutando Programa '$PROGRAMA' con facultad '$FACULTAD' en $IP_FACULTAD..."

mvn exec:java \
  -Dexec.mainClass="programas.ProgramaAcademico" \
  -Dexec.args="\"$PROGRAMA\" \"$FACULTAD\" $SEMESTRE $SALONES $LABS \"$IP_FACULTAD\"" \
  -Dexec.cleanupDaemonThreads=false

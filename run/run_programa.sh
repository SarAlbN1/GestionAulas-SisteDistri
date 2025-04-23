#!/usr/bin/env bash
set -euo pipefail

# Ejecuta un Programa Académico (DEALER → Facultad)
# Uso: ./run_programa.sh <nombrePrograma> <codigoFacultad> <semestre> <salones> <laboratorios> <ipFacultad>
if [ $# -ne 6 ]; then
  echo "Uso: $0 <nombrePrograma> <codigoFacultad> <semestre> <salones> <laboratorios> <ipFacultad>"
  exit 1
fi
ARGS="$*"

cd "$(dirname "$0")/.."

echo "[run_programa] Compilando proyecto..."
mvn compile

echo "[run_programa] Ejecutando ProgramaAcademico con args: $ARGS"
mvn exec:java \
  -Dexec.mainClass="programas.ProgramaAcademico" \
  -Dexec.args="$ARGS"

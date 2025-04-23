#!/usr/bin/env bash
set -euo pipefail

# Ejecuta un Programa Académico en modo asíncrono
# Uso: ./run_programa_async.sh <nombre> <semestre> <salones> <laboratorios> <ipFacultad>
if [ $# -ne 5 ]; then
  echo "Uso: $0 <nombre> <semestre> <salones> <laboratorios> <ipFacultad>"
  exit 1
fi

cd "$(dirname "$0")/.."

echo "[run_programa_async] Compilando proyecto..."
mvn compile

echo "[run_programa_async] Ejecutando ProgramaAsync con args: $*"
mvn exec:java \
  -Dexec.mainClass="programas.ProgramaAsync" \
  -Dexec.args="$*"

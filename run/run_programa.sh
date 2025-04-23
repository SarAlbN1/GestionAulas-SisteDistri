#!/usr/bin/env bash
set -euo pipefail

# ./run_programa.sh <sync|async> <nombre> <codFac> <sem> <sal> <lab> <ipFacultad>
if [ $# -ne 7 ]; then
  echo "Uso: $0 <sync|async> <nombre> <codFac> <sem> <sal> <lab> <ipFacultad>"
  exit 1
fi
MODE=$1; shift

ARGS="$*"

cd "$(dirname "$0")/.."

echo "[run_programa] Compilando proyecto..."
mvn compile

echo "[run_programa][$MODE] Ejecutando Programa con args: $ARGS"
mvn exec:java \
  -Dexec.mainClass="programas.ProgramaAcademico" \
  -Dexec.args="$MODE $ARGS"

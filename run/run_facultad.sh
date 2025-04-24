#!/bin/bash
# Ejecuta el proceso de Facultad
# $1 = nombreFacultad
# $2 = IP_Servidor

if [ "$#" -ne 2 ]; then
  echo "Uso: $0 <nombreFacultad> <IP_Servidor>"
  exit 1
fi

FACULTAD_DIR="src/main/java/facultades"
MODELO_DIR="src/main/java/modelo"

if [ ! -d "$FACULTAD_DIR" ] || [ ! -d "$MODELO_DIR" ]; then
  echo "Error: No se encuentran los directorios src/main/java/facultades o src/main/java/modelo"
  exit 1
fi

mkdir -p bin

echo "Compilando clases..."
javac -cp "lib/*" -d bin "$FACULTAD_DIR"/*.java "$MODELO_DIR"/*.java

echo "Ejecutando facultad..."
java -cp "bin:lib/*" facultades.Facultad "$1" "tcp://$2:5555"

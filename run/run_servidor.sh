#!/bin/bash
# Ejecuta el servidor central

SERVIDOR_DIR="src/main/java/servidor"
MODELO_DIR="src/main/java/modelo"

if [ ! -d "$SERVIDOR_DIR" ] || [ ! -d "$MODELO_DIR" ]; then
  echo "Error: No se encuentran los directorios src/main/java/servidor o src/main/java/modelo"
  exit 1
fi

mkdir -p bin

echo "Compilando servidor..."
javac -cp "lib/*" -d bin "$SERVIDOR_DIR"/*.java "$MODELO_DIR"/*.java

echo "Ejecutando servidor..."
java -cp "bin:lib/*" servidor.Servidor tcp://0.0.0.0:5555

#!/bin/bash
# Ejecuta el servidor central
chmod +x run/*.sh

mkdir -p bin
javac -cp "lib/*" -d  bin servidor/*.java modelo/*.java

java -cp bin servidor.Servidor

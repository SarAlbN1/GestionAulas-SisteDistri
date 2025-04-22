#!/bin/bash
# Ejecuta el proceso de Facultad
# $1 = nombreFacultad
# $2 = IP_Servidor
chmod +x run/*.sh

mkdir -p bin
javac -cp "lib/*" -d  bin facultades/*.java modelo/*.java

java -cp bin facultades.Facultad "$1" "$2"

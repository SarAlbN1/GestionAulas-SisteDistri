#!/bin/bash
# Ejecuta un programa académico con los argumentos:
# $1 = nombrePrograma
# $2 = semestre
# $3 = salones
# $4 = laboratorios
# $5 = IP_Facultad
chmod +x run/*.sh

mkdir -p bin
javac -cp "lib/*" -d bin src/main/java/programas/Programa1.java src/main/java/modelo/*.java

java -cp bin programas.Programa1 "$1" "$2" "$3" "$4" "$5"

#!/bin/bash
# Ejecuta el servidor réplica de respaldo
chmod +x run/*.sh

mkdir -p bin
javac -cp "lib/*" -d  bin tolerancia/ServidorReplica.java modelo/*.java servidor/*.java

java -cp bin tolerancia.ServidorReplica

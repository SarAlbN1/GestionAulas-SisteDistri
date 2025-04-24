#!/bin/bash
# Ejecuta el servidor réplica de respaldo
chmod +x run/*.sh

mkdir -p bin
javac -cp "lib/*" -d bin src/main/java/tolerancia/ServidorReplica.java src/main/java/modelo/*.java src/main/java/servidor/*.java

java -cp bin tolerancia.ServidorReplica

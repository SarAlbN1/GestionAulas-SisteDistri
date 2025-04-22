#!/bin/bash
# Ejecuta el health checker que supervisa al servidor principal
chmod +x run/*.sh

mkdir -p bin
javac -d bin tolerancia/HealthChecker.java

java -cp bin tolerancia.HealthChecker

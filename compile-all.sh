#!/bin/bash
mkdir -p bin
javac -cp "lib/*" -d bin src/modelo/*.java \
                         src/programas/*.java \
                         src/facultades/*.java \
                         src/servidor/*.java \
                         src/tolerancia/*.java

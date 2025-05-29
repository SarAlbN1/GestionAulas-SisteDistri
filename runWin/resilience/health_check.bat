@echo off
setlocal

set IP=%1
set PUERTO=%2

cd /d "%~dp0\..\.."

echo [run_backup] IP local: DEFINIR_IP_LOCAL_MANUALMENTE
echo [run_backup] Verificando conectividad a %IP%:%PUERTO%...

ping -n 1 %IP% >nul
if errorlevel 1 (
  echo ❌ No se puede contactar al servidor principal en %IP%
  exit /b 1
)

call mvn compile

echo [run_backup] Iniciando Servidor Réplica contra %IP%:%PUERTO%...
call mvn exec:java -Dexec.mainClass="tolerancia.ServidorReplica" -Dexec.args="%IP% %PUERTO%"

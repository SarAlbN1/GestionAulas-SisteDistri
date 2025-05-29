@echo off
setlocal

set "IP=%1"
set "PUERTO=%2"

:: Cambiar al directorio raíz del proyecto (donde está el pom.xml)
cd /d "%~dp0\..\.." || (
    echo ❌ Error: No se pudo cambiar al directorio del proyecto.
    exit /b 1
)

echo [run_backup] 📍 IP local: DEFINIR_IP_LOCAL_MANUALMENTE
echo [run_backup] 🌐 Verificando conectividad a %IP%:%PUERTO%...

:: Probar conectividad (solo una respuesta necesaria)
ping -n 1 %IP% >nul
if errorlevel 1 (
    echo ❌ No se puede contactar al servidor principal en %IP%
    exit /b 1
)

:: Compilar el proyecto
echo [run_backup] 🔨 Compilando el proyecto...
call mvn compile
if errorlevel 1 (
    echo ❌ Error al compilar el proyecto Maven.
    exit /b 1
)

:: Ejecutar Servidor Réplica
echo [run_backup] 🚀 Iniciando Servidor Réplica contra %IP%:%PUERTO%...
call mvn exec:java -Dexec.mainClass="tolerancia.ServidorReplica" -Dexec.args="%IP% %PUERTO%"
if errorlevel 1 (
    echo ❌ Error al iniciar el Servidor Réplica.
    exit /b 1
)

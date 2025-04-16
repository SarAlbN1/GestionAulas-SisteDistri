# Sistema Distribuido de Gestión de Aulas

Este sistema permite gestionar la asignación de aulas universitarias (salones y laboratorios) mediante una arquitectura distribuida basada en microservicios, usando Java y ZeroMQ.

## 📦 Componentes del sistema

- `ProgramaMain`: proceso que representa un programa académico
- `FacultadMain`: recibe solicitudes de sus programas y las envía al servidor
- `ServidorMain`: gestiona las solicitudes concurrentemente
- `GestorSolicitudes` y `AsignadorAulas`: lógica interna del servidor
- `BackupMain` y `HealthChecker`: respaldo y monitoreo ante fallos

## 🖥️ Requisitos

- Java 17 o superior
- ZeroMQ instalado y configurado
- Linux o terminal bash (recomendado para red local)
- Conexión entre máquinas en la misma red local

## ⚙️ Configuración

Asegúrese de definir las IPs y puertos en `config/config.properties`.  
Ejemplo de estructura esperada:

```
# IP y puerto del servidor central
server.ip=192.168.1.100
server.port=5555

# IP y puerto del servidor backup
backup.ip=192.168.1.101
backup.port=5556

# Puerto donde escucha la facultad
faculty.port=5560
```

## 🚀 Ejecución

1. Compile todos los componentes:
   ```bash
   javac -cp libs/zmq.jar -d out/ src/**/*.java
   ```

2. Inicie los procesos en las máquinas respectivas:

   - **Programa Académico** (en PC1 o PC2):
     ```bash
     java -cp out ProgramaMain "Ingeniería de Sistemas" 1 8 3
     ```

   - **Facultad** (en PC2):
     ```bash
     java -cp out FacultadMain "Facultad de Ingeniería" 1
     ```

   - **Servidor Central** (en PC3):
     ```bash
     java -cp out ServidorMain
     ```

   - **Backup + Monitor** (en PC1):
     ```bash
     java -cp out HealthChecker
     java -cp out BackupMain
     ```

## 🗂️ Archivos importantes

- `asignaciones.log`: Registro de asignaciones aceptadas
- `fallos.log`: Registro de solicitudes rechazadas
- `config/config.properties`: Configuración de red

## 🧪 Pruebas

- Asegúrese de probar con al menos 2 programas académicos conectados a la misma facultad
- Verifique que se generen logs y que las asignaciones se registren correctamente
- Simule la caída del servidor para comprobar que `BackupMain` se activa

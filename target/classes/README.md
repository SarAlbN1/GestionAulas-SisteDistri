# 🎓 Gestión de Aulas Distribuidas - Pontificia Universidad Javeriana

Este proyecto implementa un sistema distribuido para la **asignación de aulas** (salones y laboratorios) entre programas académicos de distintas facultades universitarias. Hace parte del curso **Introducción a los Sistemas Distribuidos**, y fue desarrollado por **Sara Albarracín**.

---

## 📂 Estructura del Proyecto

```
GestionAulas-SisteDistri/
├── src/
│   ├── modelo/             # Clases de dominio (Solicitud, Aula, Constantes)
│   ├── programas/          # Programas académicos (clientes solicitantes)
│   ├── facultades/         # Facultades (intermediarios con validación)
│   ├── servidor/           # Servidor central de asignación
│   └── tolerancia/         # Backup y verificador de salud (extensión)
├── data/
│   ├── logs/               # Log general del sistema
│   └── solicitudes/
│       ├── asignaciones/   # Asignaciones exitosas por semestre
│       └── rechazos/       # Rechazos de solicitudes por semestre
├── run/                    # Scripts de ejecución
├── lib/                    # Dependencias externas (.jar)
├── pom.xml                 # Configuración de Maven
└── README.md               # Documentación del sistema
```

---

## 📦 Dependencias

Este proyecto utiliza:

- **Gson 2.10.1** – Serialización JSON (solicitudes y respuestas).
- **JeroMQ 0.5.2** – Comunicación entre procesos usando ZeroMQ puro en Java.

Ambas están definidas como dependencias en `pom.xml`.

---

## 🛠️ Compilación

Para compilar el proyecto con Maven, ubícate en la raíz del proyecto y ejecuta:

```bash
mvn clean compile
```

---

## 🚀 Ejecución por Módulo

Cada proceso se lanza desde un script contenido en la carpeta `chmod +x run/*.sh`:


# 🟢 Iniciar el servidor central (modo asíncrono - ROUTER)
```bash

./run/run_servidor.sh
 ```


# 🎓 Iniciar una facultad
```bash
./run/run_facultad.sh <NombreFacultad> <IpServidor>
 
#Ejemplos: 
./run/run_facultad.sh "Ingeniería" "192.168.32.1"
./run/run_facultad.sh "Ingeniería" "localhost"
 ```

# 🧑‍🎓 Iniciar un programa académico
```bash
./run/run_programa.sh <nombrePrograma> <nombreFacultad> <semestre> <salones> <laboratorios> <ipFacultad>

# Ejemplo:
./run/run_programa.sh "Ingeniería de Sistemas" "Ingeniería" 4 2 1 "localhost"
 ```

# 🔁 Iniciar el servidor de respaldo (backup)
```bash
./run/run_backup.sh
 ```

# ❤️ Iniciar el verificador de salud
```bash
./run/health_check.sh
```

> 💡 Asegúrate de que los puertos estén abiertos entre máquinas y se mantenga la coherencia en los nombres de facultades y programas.

---

## 📁 Carpetas de Datos

| Carpeta                         | Contenido generado automáticamente                    |
|---------------------------------|--------------------------------------------------------|
| `data/solicitudes/asignaciones/`| Asignaciones exitosas, organizadas por semestre       |
| `data/solicitudes/rechazos/`    | Solicitudes rechazadas por falta de recursos          |
| `data/logs/log_general.txt`     | Log central con trazabilidad y auditoría del sistema  |

Cada línea del log incluye fecha, ID de solicitud, estado y contenido en JSON.

---

## 💻 Configuración para VS Code

Crea o edita el archivo `.vscode/settings.json` con lo siguiente:

```json
{
  "java.project.sourcePaths": ["src"],
  "java.project.referencedLibraries": [
    "lib/**/*.jar"
  ]
}
```

Esto asegura que el editor reconozca correctamente la estructura y dependencias.

---

## 🌐 Arquitectura del Sistema

- **Patrón**: Comunicación distribuida DEALER ↔ ROUTER con ZeroMQ.
- **Facultades**: Se inscriben en el servidor y validan programas.
- **Programas académicos**: Generan solicitudes que atraviesan la facultad.
- **Servidor**: Centraliza la asignación y registra la trazabilidad.
- **Tolerancia a fallos (extensión)**: Incluye un servidor réplica y verificador de estado.

---

## 👩‍💻 Desarrollado por

**Sara Albarracín**  
Pontificia Universidad Javeriana  
Curso: *Introducción a los Sistemas Distribuidos*

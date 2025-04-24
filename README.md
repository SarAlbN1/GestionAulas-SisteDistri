# 🎓 Gestión de Aulas Distribuidas - Pontificia Universidad Javeriana

Este proyecto implementa un sistema distribuido para la asignación de aulas (salones y laboratorios) entre programas académicos de distintas facultades universitarias. Es parte del curso **Introducción a los Sistemas Distribuidos**, y está desarrollado por **Sara Albarracín**.

---

## 📂 Estructura del Proyecto

```
GestionAulas-SisteDistri/
├── src/                    # Código fuente organizado por paquetes
│   ├── modelo/             # Clases de dominio: Solicitud, Aula, Constantes
│   ├── programas/          # Programas académicos (generan solicitudes)
│   ├── facultades/         # Facultades (intermediarios)
│   ├── servidor/           # Lógica de asignación y concurrencia
│   └── tolerancia/         # HealthChecker y Servidor réplica
├── target/                 # Archivos .class generados por Maven
├── run/                    # Scripts de ejecución por módulo
├── data/                   # Logs, asignaciones y solicitudes
├── pom.xml                 # Archivo de configuración de Maven
└── README.md               # Documentación del proyecto
```

---

## 📦 Dependencias

Este proyecto utiliza:

- **Gson 2.10.1** – Para la serialización y deserialización de objetos en JSON.
- **JeroMQ 0.5.2** – Para la comunicación entre procesos usando ZeroMQ.

Ambas dependencias están definidas en el `pom.xml`.

---

## 🛠️ Compilación con Maven

Para compilar el proyecto, asegúrate de estar en la raíz y ejecuta:

```bash
mvn clean compile
```

---

## 🚀 Ejecución por Módulo

Cada proceso del sistema se lanza con un script dentro de `run/`:

```bash
# Iniciar el servidor principal
./run/run_servidor.sh

# Iniciar una facultad
./run/run_facultad.sh <ipServidor>

# Iniciar un programa académico
./run/run_programa.sh <nombre> <semestre> <salones> <laboratorios> <ipFacultad>

# Iniciar el servidor réplica (backup)
./run/run_backup.sh

# Iniciar el verificador de salud (tolerancia a fallos)
./run/health_check.sh
```

---

## 📁 Carpetas de Datos

| Carpeta             | Contenido generado                                       |
|---------------------|----------------------------------------------------------|
| `data/asignaciones/`| Asignaciones exitosas realizadas por el servidor         |
| `data/logs/`        | Logs de errores, rechazos o alertas por falta de recursos |
| `data/solicitudes/` | Historial de solicitudes realizadas por los programas    |

---

## 💻 Configuración en VS Code

Asegúrate de tener este archivo `.vscode/settings.json`:

```json
{
  "java.project.sourcePaths": ["src/main/java"],
  "java.project.referencedLibraries": [
    "lib/**/*.jar"
  ]
}
```

Esto permite que el editor reconozca correctamente los paquetes y dependencias.

---

## 👩‍💻 Desarrollado por

**Sara Albarracín**  
Pontificia Universidad Javeriana  
Curso: *Introducción a los Sistemas Distribuidos*

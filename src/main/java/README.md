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
│   ├── servidor/           # Servidor central (ROUTER)
│   └── tolerancia/         # HealthChecker (monitoreo y backup automático)
├── data/
│   ├── logs/               # Log general del sistema
│   └── solicitudes/
│       ├── asignaciones/   # Asignaciones exitosas por semestre
│       └── rechazos/       # Rechazos por falta de recursos
├── run/                    # Scripts ejecutables multiplataforma
├── lib/                    # Librerías externas (.jar)
├── pom.xml                 # Configuración del proyecto Maven
└── README.md               # Documentación del sistema
```

---

## 📦 Dependencias

Este proyecto usa las siguientes dependencias gestionadas por Maven:

- **Gson 2.10.1** – Serialización JSON.
- **JeroMQ 0.5.2** – Implementación pura de ZeroMQ en Java.

Ambas están definidas en `pom.xml` y se descargan automáticamente.

---

## 🛠️ Compilación

La compilación es **idéntica en Linux, macOS y Windows (usando Git Bash)**.

```bash
mvn clean compile
```

Asegúrate de tener:
- Java 17 o superior
- Maven instalado y en tu PATH
- Git Bash si estás en Windows

---

## 🚀 Ejecución por Módulo

Cada módulo se lanza con los scripts dentro de `run/`, los cuales funcionan en **todas las plataformas** con soporte Bash.

---

### 🟢 1. Iniciar el Servidor Central

```bash
./run/run_servidor.sh
```

Esto inicia el servidor principal (modo ROUTER) en el puerto `5555`.

---

### ❤️ 2. Iniciar el HealthChecker

```bash
./run/health_check.sh <ipServidorPrincipal> [puerto]

# Ejemplo:
./run/health_check.sh 192.168.1.4 5555
```

Este componente:
- Monitorea el servidor principal.
- Si detecta fallo, **lanza automáticamente el servidor en modo backup**.
- Notifica a las facultades para que redirijan sus conexiones.

> ❗ No necesitas ejecutar `run_backup.sh` manualmente. El HealthChecker lo hace.

---

### 🎓 3. Iniciar 2 Facultades Distintas

Cada facultad necesita:
- Su nombre (ej. Ingeniería, Medicina)
- IP del servidor principal
- IP del servidor backup (donde está el HealthChecker)






```bash
# Facultad de Ingeniería
./run/run_facultad.sh "Ingeniería" "192.168.1.4" "192.168.1.5"

# Facultad de Medicina
./run/run_facultad.sh "Medicina" "192.168.1.4" "192.168.1.5"
```

Cada una escucha solicitudes en el puerto `6000`.

---

### 🧑‍🎓 4. Iniciar 2 Programas Académicos para Facultades Diferentes

Cada programa requiere:
- Nombre del programa
- Nombre de la facultad destino
- Semestre, número de salones y laboratorios
- IP donde se ejecuta la facultad correspondiente

```bash
# Programa 1 → Ingeniería
./run/run_programa.sh "Ingeniería de Sistemas" "Ingeniería" 4 2 1 "192.168.1.10"

# Programa 2 → Medicina
./run/run_programa.sh "Medicina General" "Medicina" 2 1 1 "192.168.1.11"
```

---

## 📁 Datos y Trazabilidad

| Carpeta                          | Contenido generado automáticamente                    |
|----------------------------------|--------------------------------------------------------|
| `data/solicitudes/asignaciones/` | JSON con asignaciones exitosas por semestre           |
| `data/solicitudes/rechazos/`     | JSON de rechazos por falta de recursos                |
| `data/logs/log_general.txt`      | Registro general con fecha, estado y contenido JSON   |

---

## 🌐 Arquitectura del Sistema

```plaintext
[ProgramaAcadémico] →→ [Facultad] →→ [Servidor Principal]
                                ↘→ [Servidor Backup] (si falla el principal)
                      ↖⎯⎯⎯⎯⎯⎯⎯ [HealthChecker] ⎯⎯⎯→ notifica REDIRIGIR / VOLVER
```

- Comunicación **asincrónica** con ZeroMQ: DEALER ↔ ROUTER.
- Cada Facultad escucha en puerto 6000.
- Servidor Central escucha en 5555.
- HealthChecker notifica por PUB en 7000.
- El Servidor Backup se inicia automáticamente, reutilizando la clase `Servidor`.

---

## 🧠 Recomendaciones Finales

- Verifica que todas las IPs sean accesibles en red local.
- Evita repetir nombres de facultades o programas no registrados.
- Usa `Ctrl+C` para detener cada proceso manualmente si no usas supervisor de procesos.
- Los scripts son ejecutables: `chmod +x run/*.sh` (solo una vez en Unix).

---

## 👩‍💻 Desarrollado por

**Sara Albarracín**  
Pontificia Universidad Javeriana  
Curso: *Introducción a los Sistemas Distribuidos*

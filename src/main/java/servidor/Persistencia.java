package servidor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Maneja la persistencia organizada de solicitudes procesadas.
 * Guarda asignaciones y rechazos bajo carpeta data/solicitudes/
 * y registra todo en un log general bajo data/logs/.
 */
public class Persistencia {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void guardar(String tipo, String contenido) {
        Gson gson = new Gson();
        Map<String, Object> datos;

        try {
            datos = gson.fromJson(contenido, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (Exception e) {
            System.err.println("[Persistencia] ❌ No se pudo interpretar el JSON: " + contenido);
            return;
        }

        String id = (String) datos.getOrDefault("id", "sin-id");
        int semestre = ((Double) datos.getOrDefault("semestre", 0)).intValue();

        if (semestre == 0 || id.equals("sin-id")) {
            System.err.println("[Persistencia] ❌ Falta ID o semestre. Registro descartado.");
            return;
        }

        // Rutas organizadas
        String baseSolicitudes = "data/solicitudes/" + tipo;
        String archivoSolicitudes = baseSolicitudes + "/semestre_" + semestre + ".txt";
        String archivoLogGeneral = "data/logs/log_general.txt";

        try {
            Files.createDirectories(Paths.get(baseSolicitudes));
            Files.createDirectories(Paths.get("data/logs"));
        } catch (IOException e) {
            System.err.println("[Persistencia] ❌ Error creando directorios: " + e.getMessage());
            return;
        }

        // Verificar duplicado por ID en archivo de solicitudes
        try {
            Path path = Paths.get(archivoSolicitudes);
            if (Files.exists(path)) {
                List<String> lineas = Files.readAllLines(path);
                if (lineas.stream().anyMatch(line -> line.contains("\"id\":\"" + id + "\""))) {
                    System.out.println("[Persistencia] ⚠️ ID ya registrado en " + archivoSolicitudes);
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("[Persistencia] ❌ Error leyendo archivo de solicitudes: " + e.getMessage());
        }

        String timestamp = LocalDateTime.now().format(FORMATO_FECHA);
        String registroJson = new Gson().toJson(gson.fromJson(contenido, Object.class)); // pretty print JSON

        String registro = "-------------------------------\n" +
                "📅 Fecha: " + timestamp + "\n" +
                "📄 ID: " + id + "\n" +
                registroJson + "\n";

        // Escribir en archivo de solicitudes por semestre
        try (FileWriter writer = new FileWriter(archivoSolicitudes, true)) {
            writer.write(registro + "\n");
            System.out.println("[Persistencia] ✅ Registro de ID " + id + " guardado en " + archivoSolicitudes);
        } catch (IOException e) {
            System.err.println("[Persistencia] ❌ Error escribiendo en archivo de solicitudes: " + e.getMessage());
        }

        // Escribir en el log general con tipo destacado
        try (FileWriter logWriter = new FileWriter(archivoLogGeneral, true)) {
            logWriter.write("[" + tipo.toUpperCase() + "]\n" + registro + "\n");
        } catch (IOException e) {
            System.err.println("[Persistencia] ❌ Error escribiendo en log general: " + e.getMessage());
        }

    }
}

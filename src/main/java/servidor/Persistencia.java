package servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Maneja la escritura de archivos de solicitudes y asignaciones.
 */
public class Persistencia {

    public void guardar(String tipo, String contenido) {
        String ruta = "data/" + (tipo.equals("error") ? "logs" : "asignaciones") + "/" + tipo + ".txt";

        try (FileWriter writer = new FileWriter(ruta, true)) {
            writer.write(LocalDateTime.now() + " - " + contenido + "\n");
            System.out.println("[Persistencia] Registro guardado en " + ruta);
        } catch (IOException e) {
            System.err.println("[Persistencia] Error al guardar: " + e.getMessage());
        }
    }
}

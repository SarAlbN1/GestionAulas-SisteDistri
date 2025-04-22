package servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Maneja la escritura de archivos de solicitudes y asignaciones.
 */
public class Persistencia {

    /**
     * Guarda el contenido de una solicitud o error en un archivo de texto.
     *
     * @param tipo      indica la categoría del registro: "error" o cualquier otro para asignaciones
     * @param contenido el texto (JSON de la solicitud) que se desea persistir
     */
    public void guardar(String tipo, String contenido) {
        // Determina la ruta del archivo según el tipo:
        // - Si tipo=="error", se escribirá en data/logs/error.txt
        // - En caso contrario, en data/asignaciones/<tipo>.txt
        String ruta = "data/" +
                       (tipo.equals("error") ? "logs" : "asignaciones") +
                       "/" + tipo + ".txt";

        // try-with-resources abre el FileWriter en modo append (true)
        // para añadir al final del archivo sin sobrescribir contenido previo
        try (FileWriter writer = new FileWriter(ruta, true)) {
            // Escribe la marca de tiempo seguida del contenido y nueva línea
            writer.write(LocalDateTime.now() + " - " + contenido + "\n");
            System.out.println("[Persistencia] Registro guardado en " + ruta);
        } 
        // Captura cualquier error de E/S (por ejemplo, ruta inexistente o permisos)
        catch (IOException e) {
            // Muestra mensaje de error en stderr sin interrumpir flujo del servidor
            System.err.println("[Persistencia] Error al guardar: " + e.getMessage());
        }
    }
}

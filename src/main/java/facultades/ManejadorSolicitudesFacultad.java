package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;

/**
 * Manejador de solicitudes que una facultad recibe desde un programa académico.
 * Reenvía al servidor y retransmite la respuesta al programa.
 */
public class ManejadorSolicitudesFacultad implements Runnable {

    private final Solicitud solicitud;
    private final ZMQ.Socket socketEnvio;
    private final ZMQ.Socket socketRecepcion;

    public ManejadorSolicitudesFacultad(Solicitud solicitud, ZMQ.Socket socketEnvio, ZMQ.Socket socketRecepcion) {
        this.solicitud = solicitud;
        this.socketEnvio = socketEnvio;
        this.socketRecepcion = socketRecepcion;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        try {
            String solicitudJson = gson.toJson(solicitud);

            // Enviar solicitud al servidor
            socketEnvio.send("", ZMQ.SNDMORE);
            socketEnvio.send(solicitudJson);

            System.out.println("📤 [Facultad] Enviando solicitud ID " + solicitud.getId()
                    + " del programa '" + solicitud.getPrograma() + "' al servidor...");

            // Recibir respuesta del servidor
            String respuestaServidor = socketEnvio.recvStr();

            if (respuestaServidor == null || respuestaServidor.isEmpty()) {
                System.err.println("⚠️ [Facultad] Respuesta vacía desde el servidor para ID " + solicitud.getId());
                socketRecepcion.send("ERROR: Respuesta vacía desde el servidor.");
                return;
            }

            System.out.println("📥 [Facultad] Respuesta del servidor para ID " + solicitud.getId() + ": " + respuestaServidor);

            // Enviar de vuelta al programa académico
            socketRecepcion.send(respuestaServidor);

            System.out.println("✅ [Facultad] Solicitud ID " + solicitud.getId() + " procesada y respondida a '" + solicitud.getPrograma() + "'.");

        } catch (Exception e) {
            System.err.println("❌ Error en ManejadorSolicitudesFacultad (ID: " + solicitud.getId() + "): " + e.getMessage());
            try {
                socketRecepcion.send("ERROR al procesar solicitud del programa ID: " + solicitud.getId());
            } catch (Exception ignored) {
                System.err.println("❌ Error adicional al intentar enviar error al programa.");
            }
        }
    }
}

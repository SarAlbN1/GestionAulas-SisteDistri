package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * Manejador de solicitudes que una facultad recibe desde un programa académico.
 * Reenvía al servidor (DEALER) y transmite la respuesta al programa (ROUTER).
 */
public class ManejadorSolicitudesFacultad implements Runnable {

    private final Solicitud solicitud;
    private final ZMQ.Socket socketEnvio;       // DEALER (al servidor)
    private final ZMQ.Socket socketRecepcion;   // ROUTER (respuesta al programa)
    private final ZMsg envelope;                // Envelope completo del programa

    public ManejadorSolicitudesFacultad(Solicitud solicitud, ZMQ.Socket socketEnvio, ZMQ.Socket socketRecepcion, ZMsg envelope) {
        this.solicitud = solicitud;
        this.socketEnvio = socketEnvio;
        this.socketRecepcion = socketRecepcion;
        this.envelope = envelope;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        try {
            String solicitudJson = gson.toJson(solicitud);

            // Enviar solicitud al servidor
            socketEnvio.sendMore("");
            socketEnvio.send(solicitudJson);

            System.out.printf("📤 [Facultad] Enviando solicitud ID %s del programa '%s' al servidor...\n",
                    solicitud.getId(), solicitud.getPrograma());

            // Esperar respuesta del servidor
            String frameVacio = socketEnvio.recvStr(); // descartar frame vacío
            String respuestaServidor = socketEnvio.recvStr();

            if (respuestaServidor == null || respuestaServidor.trim().isEmpty()) {
                System.err.println("⚠️ [Facultad] Respuesta vacía del servidor para ID " + solicitud.getId());
                responderAPrograma("ERROR: Respuesta vacía del servidor.");
                return;
            }

            System.out.printf("📥 [Facultad] Respuesta del servidor para ID %s: %s\n",
                    solicitud.getId(), respuestaServidor);

            // Responder al programa académico
            ZMsg respuesta = envelope.duplicate();
            respuesta.addString(respuestaServidor);
            respuesta.send(socketRecepcion);

            System.out.println("✅ [Facultad] Solicitud procesada y respondida correctamente.");

        } catch (Exception e) {
            System.err.printf("❌ Error al procesar solicitud ID %s: %s\n", solicitud.getId(), e.getMessage());
            responderAPrograma("ERROR: No se pudo procesar la solicitud.");
        }
    }

    private void responderAPrograma(String mensaje) {
        try {
            ZMsg respuestaError = envelope.duplicate();
            respuestaError.addString(mensaje);
            respuestaError.send(socketRecepcion);
        } catch (Exception e) {
            System.err.println("❌ Error adicional al intentar enviar mensaje de error al programa.");
        }
    }
}

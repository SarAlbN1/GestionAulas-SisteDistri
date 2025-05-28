package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZFrame;

/**
 * Manejador de solicitudes que una facultad recibe desde un programa académico.
 * Reenvía al servidor y retransmite la respuesta al programa.
 */
public class ManejadorSolicitudesFacultad implements Runnable {

    private final Solicitud solicitud;
    private final ZMQ.Socket socketEnvio;
    private final ZMsg mensajeOriginal;

    public ManejadorSolicitudesFacultad(Solicitud solicitud, ZMQ.Socket socketEnvio, ZMsg mensajeOriginal) {
        this.solicitud = solicitud;
        this.socketEnvio = socketEnvio;
        this.mensajeOriginal = mensajeOriginal;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        try {
            String solicitudJson = gson.toJson(solicitud);

            // Enviar solicitud al servidor usando DEALER (2 frames: vacío + contenido)
            socketEnvio.sendMore("");
            socketEnvio.send(solicitudJson);

            System.out.printf("📤 [Facultad] Enviando solicitud ID %s del programa '%s' al servidor...\n",
                    solicitud.getId(), solicitud.getPrograma());

            // Esperar respuesta del servidor
            String frameVacio = socketEnvio.recvStr();
            String respuestaServidor = socketEnvio.recvStr();

            if (respuestaServidor == null || respuestaServidor.trim().isEmpty()) {
                System.err.println("⚠️ [Facultad] Respuesta vacía del servidor para ID " + solicitud.getId());
                responderAPrograma("ERROR: Respuesta vacía del servidor.");
                return;
            }

            System.out.printf("📥 [Facultad] Respuesta del servidor para ID %s: %s\n",
                    solicitud.getId(), respuestaServidor);

            // Responder al programa académico con los mismos frames de identidad
            ZMsg respuesta = new ZMsg();
            for (ZFrame frame : mensajeOriginal) {
                respuesta.add(frame.duplicate());
            }
            respuesta.addString(respuestaServidor);
            respuesta.send(socketEnvio);
            System.out.println("✅ [Facultad] Solicitud procesada y respondida correctamente.");

        } catch (Exception e) {
            System.err.printf("❌ Error al procesar solicitud ID %s: %s\n", solicitud.getId(), e.getMessage());
            responderAPrograma("ERROR: No se pudo procesar la solicitud.");
        }
    }

    private void responderAPrograma(String mensaje) {
        try {
            ZMsg respuestaError = new ZMsg();
            for (ZFrame frame : mensajeOriginal) {
                respuestaError.add(frame.duplicate());
            }
            respuestaError.addString(mensaje);
            respuestaError.send(socketEnvio);
        } catch (Exception e) {
            System.err.println("❌ Error adicional al intentar enviar mensaje de error al programa.");
        }
    }
}

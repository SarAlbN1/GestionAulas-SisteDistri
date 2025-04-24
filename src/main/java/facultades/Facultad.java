package facultades;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

public class Facultad {

    private static final int PUERTO_SERVIDOR = 5555;
    private static final int REINTENTO_MS = 3000;

    public static void iniciar(String ipServidor, String nombreFacultad, ZMQ.Socket recepcion, ZMQ.Context context) {
        Gson gson = new Gson();

        // Socket DEALER exclusivo por facultad (permite conexión paralela)
        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        envio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));

        boolean conectado = false;
        while (!conectado) {
            try {
                envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

                Map<String, String> inscripcion = new HashMap<>();
                inscripcion.put("tipo", "inscripcion");
                inscripcion.put("facultad", nombreFacultad);

                envio.send("", ZMQ.SNDMORE);  // Frame vacío
                envio.send(gson.toJson(inscripcion));  // Datos

                String ack = envio.recvStr();

                if ("Inscripción exitosa".equals(ack) || "Facultad ya activa".equals(ack)) {
                    System.out.println("[Facultad - " + nombreFacultad + "] ✅ Conexión establecida con el servidor. Estado: " + ack);
                    conectado = true;
                } else {
                    System.out.println("[Facultad - " + nombreFacultad + "] ⚠️ Respuesta inesperada del servidor: " + ack);
                    Thread.sleep(REINTENTO_MS);
                }
            } catch (Exception e) {
                System.out.println("[Facultad - " + nombreFacultad + "] ❌ Error de conexión. Reintentando...");
                envio.close();
                envio = context.socket(ZMQ.DEALER);
                envio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
                try {
                    Thread.sleep(REINTENTO_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("[Facultad - " + nombreFacultad + "] 🎓 Facultad activa. Atendiendo solicitudes en puerto compartido...");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String solicitud = recepcion.recvStr();
                System.out.println("[Facultad - " + nombreFacultad + "] 📥 Solicitud recibida: " + solicitud);

                envio.send("", ZMQ.SNDMORE);
                envio.send(solicitud);

                String respuesta = envio.recvStr();
                recepcion.send(respuesta);
                System.out.println("[Facultad - " + nombreFacultad + "] 📤 Respuesta enviada al programa académico.");
            } catch (Exception e) {
                System.out.println("[Facultad - " + nombreFacultad + "] ❌ Error al procesar solicitud: " + e.getMessage());
            }
        }

        envio.close();
    }
}
package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Facultad {

    private static final int PUERTO_SERVIDOR = 5555;
    private static final int PUERTO_RECEPCION = 6000;

    private static final Map<String, List<String>> FACULTADES_PROGRAMAS = Map.of(
            "Ingeniería", List.of("Ingeniería Civil", "Ingeniería Electrónica", "Ingeniería de Sistemas", "Ingeniería Mecánica", "Ingeniería Industrial"),
            "Medicina", List.of("Medicina General", "Enfermería", "Odontología", "Farmacia", "Terapia Física")
    );

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Facultad <nombreFacultad> <ipServidor>");
            return;
        }

        String nombreFacultadInput = args[0];
        String nombreFacultad = nombreFacultadInput.replace("Facultad de ", "").trim();
        String ipServidor = args[1];

        Gson gson = new Gson();
        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket recepcion = context.socket(SocketType.ROUTER);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        ZMQ.Socket envio = context.socket(SocketType.DEALER);
        envio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
        envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

        // Registro asincrónico en el servidor
        Map<String, String> inscripcion = Map.of(
                "tipo", "inscripcion",
                "facultad", nombreFacultadInput
        );
        envio.sendMore("");
        envio.send(gson.toJson(inscripcion));
        System.out.println("📤 Enviando solicitud de inscripción para facultad: " + nombreFacultad);

        // Cargar programas válidos
        List<String> programasValidos = FACULTADES_PROGRAMAS.getOrDefault(nombreFacultad, Collections.emptyList());
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println("🟢 Facultad '" + nombreFacultad + "' escuchando solicitudes...");

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg mensaje = ZMsg.recvMsg(recepcion);
            if (mensaje == null || mensaje.size() < 2) continue;

            String identificadorPrograma = mensaje.popString();
            String solicitudStr = new String(mensaje.pop().getData(), ZMQ.CHARSET); // ✔️ Esto garantiza que los bytes del mensaje se lean correctamente como UTF-8
Solicitud solicitud = gson.fromJson(solicitudStr, Solicitud.class);    // ✔️ Aquí sí usas la clase `Solicitud` como siempre

            try {
                if (solicitudStr.trim().startsWith("{")) {
                    solicitud = gson.fromJson(solicitudStr, Solicitud.class);
                } else {
                    System.err.println("⚠️ Mensaje recibido no es un JSON válido: " + solicitudStr);
                    continue;
                }
            } catch (Exception e) {
                System.err.println("❌ Error al parsear solicitud: " + e.getMessage());
                continue;
            }

            if (solicitud == null || solicitud.getPrograma() == null) continue;

            String programa = solicitud.getPrograma();
            System.out.printf("📥 Solicitud recibida de '%s', ID: %s\n", programa, solicitud.getId());

            if (programasValidos.contains(programa)) {
                pool.submit(new ManejadorSolicitudesFacultad(solicitud, envio, recepcion, identificadorPrograma));
            } else {
                String mensajeError = "ERROR: El programa '" + programa + "' no pertenece a la facultad '" + nombreFacultad + "'.";
                ZMsg respuesta = new ZMsg();
                respuesta.addString(identificadorPrograma);
                respuesta.addString(mensajeError);
                respuesta.send(recepcion);
                System.out.printf("❌ Rechazada solicitud de %s: programa no válido para esta facultad.\n", programa);
            }
        }

        envio.close();
        recepcion.close();
        pool.shutdown();
    }
}

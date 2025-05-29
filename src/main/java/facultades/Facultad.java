package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.*;

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

    private static ZMQ.Socket connectWithRetry(ZMQ.Context context, String ipServidor) {
        while (true) {
            try {
                ZMQ.Socket envio = context.socket(SocketType.DEALER);
                envio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
                envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);
                System.out.println("🔗 Conectado al servidor en " + ipServidor + ":" + PUERTO_SERVIDOR);
                return envio;
            } catch (Exception e) {
                System.out.println("❌ Error conectando al servidor. Reintentando en 5 segundos...");
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Facultad <nombreFacultad> <ipServidor>");
            return;
        }

        String nombreFacultad = args[0];
        String ipServidor = args[1];

        Gson gson = new Gson();
        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket recepcion = context.socket(SocketType.ROUTER);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);
        System.out.println("📡 Facultad escuchando en puerto " + PUERTO_RECEPCION);

        ZMQ.Socket envio = connectWithRetry(context, ipServidor);

        Map<String, String> inscripcion = Map.of("tipo", "inscripcion", "facultad", nombreFacultad);
        envio.sendMore("");
        envio.send(gson.toJson(inscripcion));
        System.out.println("📤 Enviando inscripción: " + gson.toJson(inscripcion));

        List<String> programasValidos = FACULTADES_PROGRAMAS.getOrDefault(nombreFacultad, Collections.emptyList());
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println("🟢 Facultad '" + nombreFacultad + "' esperando solicitudes...");

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg mensaje = ZMsg.recvMsg(recepcion);
            if (mensaje == null || mensaje.size() < 2) continue;

            ZMsg envelope = mensaje.duplicate();
            String solicitudStr = new String(mensaje.getLast().getData(), ZMQ.CHARSET);
            System.out.println("📥 Solicitud recibida: " + solicitudStr);

            try {
                Solicitud solicitud = gson.fromJson(solicitudStr, Solicitud.class);
                String programa = solicitud.getPrograma();
                String facultadDestino = solicitud.getFacultad();

                if (!facultadDestino.equalsIgnoreCase(nombreFacultad)) {
                    envelope.addString("❌ Facultad destino incorrecta.");
                    envelope.send(recepcion);
                    continue;
                }

                if (!programasValidos.contains(programa)) {
                    envelope.addString("❌ Programa no pertenece a la facultad.");
                    envelope.send(recepcion);
                    continue;
                }

                pool.submit(new ManejadorSolicitudesFacultad(solicitud, envio, recepcion, envelope));

            } catch (Exception e) {
                envelope.addString("❌ Error procesando solicitud.");
                envelope.send(recepcion);
            }
        }

        envio.close();
        recepcion.close();
        pool.shutdown();
    }
}

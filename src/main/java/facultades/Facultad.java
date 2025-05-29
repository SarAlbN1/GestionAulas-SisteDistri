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
    private static final int PUERTO_HEALTH_NOTIF = 7000;

    private static ZMQ.Socket socketEnvio;
    private static String ipServidorActivo;

    private static final Map<String, List<String>> FACULTADES_PROGRAMAS = Map.of(
            "Ingeniería", List.of("Ingeniería Civil", "Ingeniería Electrónica", "Ingeniería de Sistemas", "Ingeniería Mecánica", "Ingeniería Industrial"),
            "Medicina", List.of("Medicina General", "Enfermería", "Odontología", "Farmacia", "Terapia Física")
    );

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java Facultad <nombreFacultad> <ipServidorPrincipal> <ipServidorBackup>");
            return;
        }

        String nombreFacultad = args[0];
        String ipPrincipal = args[1];
        String ipBackup = args[2];

        Gson gson = new Gson();
        ZMQ.Context context = ZMQ.context(1);

        // ROUTER para recibir solicitudes de programas académicos
        ZMQ.Socket recepcion = context.socket(SocketType.ROUTER);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);
        System.out.println("📡 Facultad escuchando en puerto " + PUERTO_RECEPCION);

        // DEALER para enviar al servidor
        socketEnvio = context.socket(SocketType.DEALER);
        socketEnvio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
        socketEnvio.connect("tcp://" + ipPrincipal + ":" + PUERTO_SERVIDOR);
        ipServidorActivo = ipPrincipal;
        System.out.println("🔗 Conectado al servidor PRINCIPAL en " + ipPrincipal + ":" + PUERTO_SERVIDOR);

        // Inscripción inicial
        Map<String, String> inscripcion = Map.of("tipo", "inscripcion", "facultad", nombreFacultad);
        socketEnvio.sendMore("");
        socketEnvio.send(gson.toJson(inscripcion));
        System.out.println("📤 Enviando inscripción: " + gson.toJson(inscripcion));

        // Hilo para escuchar notificaciones de redirección del HealthChecker
        new Thread(() -> {
            ZMQ.Socket notifSocket = context.socket(SocketType.SUB);
            notifSocket.connect("tcp://" + ipBackup + ":" + PUERTO_HEALTH_NOTIF);
            notifSocket.subscribe("REDIRIGIR".getBytes(ZMQ.CHARSET));
            notifSocket.subscribe("VOLVER".getBytes(ZMQ.CHARSET));

            while (!Thread.currentThread().isInterrupted()) {
                String mensaje = notifSocket.recvStr();
                if (mensaje != null && mensaje.startsWith("REDIRIGIR")) {
                    redirigirAServidor(context, ipBackup);
                } else if (mensaje != null && mensaje.startsWith("VOLVER")) {
                    redirigirAServidor(context, ipPrincipal);
                }
            }
            notifSocket.close();
        }).start();

        // Procesamiento de solicitudes
        List<String> programasValidos = FACULTADES_PROGRAMAS.getOrDefault(nombreFacultad, Collections.emptyList());
        ExecutorService pool = Executors.newCachedThreadPool();
        System.out.println("🟢 Facultad '" + nombreFacultad + "' esperando solicitudes...");

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg mensaje = ZMsg.recvMsg(recepcion);
            if (mensaje == null || mensaje.size() < 2) continue;

            ZMsg envelope = mensaje.duplicate();
            String solicitudStr = new String(mensaje.getLast().getData(), ZMQ.CHARSET);
            System.out.println("📥 Solicitud recibida: " + solicitudStr);

            Solicitud solicitud;
            try {
                solicitud = gson.fromJson(solicitudStr, Solicitud.class);
            } catch (Exception e) {
                envelope.addString("❌ Solicitud malformada.");
                envelope.send(recepcion);
                continue;
            }

            if (!solicitud.getFacultad().equalsIgnoreCase(nombreFacultad)) {
                envelope.addString("❌ Facultad destino incorrecta.");
                envelope.send(recepcion);
                continue;
            }

            if (!programasValidos.contains(solicitud.getPrograma())) {
                envelope.addString("❌ Programa no válido.");
                envelope.send(recepcion);
                continue;
            }

            pool.submit(new ManejadorSolicitudesFacultad(solicitud, socketEnvio, recepcion, envelope));
        }

        socketEnvio.close();
        recepcion.close();
        pool.shutdown();
    }

    private static synchronized void redirigirAServidor(ZMQ.Context context, String nuevaIP) {
        if (ipServidorActivo.equals(nuevaIP)) return;

        System.out.printf("🔄 Redirigiendo conexión DEALER del servidor %s al nuevo %s...\n", ipServidorActivo, nuevaIP);
        socketEnvio.close();
        socketEnvio = context.socket(SocketType.DEALER);
        socketEnvio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
        socketEnvio.connect("tcp://" + nuevaIP + ":" + PUERTO_SERVIDOR);
        ipServidorActivo = nuevaIP;
    }
}

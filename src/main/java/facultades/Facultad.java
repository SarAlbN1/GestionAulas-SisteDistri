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

        String nombreFacultad = args[0];
        String ipServidor = args[1];

        Gson gson = new Gson();
        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket recepcion = context.socket(SocketType.ROUTER);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);
        System.out.println("📡 Facultad escuchando en puerto " + PUERTO_RECEPCION);

        new Thread(new tolerancia.HealthChecker()).start();

        ZMQ.Socket envio = context.socket(SocketType.DEALER);
        envio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
        envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);
        System.out.println("🔗 Conectado al servidor en " + ipServidor + ":" + PUERTO_SERVIDOR);

        // Inscripción
        Map<String, String> inscripcion = Map.of("tipo", "inscripcion", "facultad", nombreFacultad);
        envio.sendMore("");
        envio.send(gson.toJson(inscripcion));
        System.out.println("📤 Enviando solicitud de inscripción: " + gson.toJson(inscripcion));

        List<String> programasValidos = FACULTADES_PROGRAMAS.getOrDefault(nombreFacultad, Collections.emptyList());
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println("🟢 Facultad '" + nombreFacultad + "' esperando solicitudes...");

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg mensaje = ZMsg.recvMsg(recepcion);
            if (mensaje == null || mensaje.size() < 2) continue;

            ZMsg envelope = mensaje.duplicate();  // Envelope original
            String solicitudStr = new String(mensaje.getLast().getData(), ZMQ.CHARSET);
            System.out.println("📥 Mensaje recibido de programa: " + solicitudStr);

            Solicitud solicitud;
            try {
                solicitud = gson.fromJson(solicitudStr, Solicitud.class);
            } catch (Exception e) {
                envelope.addString("❌ Solicitud malformada: no se pudo interpretar el JSON.");
                envelope.send(recepcion);
                System.out.println("❌ Rechazada solicitud por error de formato.");
                continue;
            }

            String programa = solicitud.getPrograma();
            String facultadDestino = solicitud.getFacultad();

            System.out.printf("🔍 Validando facultad solicitada '%s' contra esta facultad '%s'%n",
                    facultadDestino, nombreFacultad);

            // Validar que la facultad destino coincida con la ejecutada
            if (!facultadDestino.trim().equalsIgnoreCase(nombreFacultad.trim())) {
                envelope.addString("❌ Solicitud rechazada: la solicitud fue enviada a la facultad '" + facultadDestino +
                        "', pero esta instancia corresponde a '" + nombreFacultad + "'.");
                envelope.send(recepcion);
                System.out.println("❌ Rechazada solicitud: facultad incorrecta → " + facultadDestino);
                continue;
            }

            // Validar que el programa pertenezca a la facultad
            if (!programasValidos.contains(programa)) {
                envelope.addString("❌ Programa '" + programa + "' no pertenece a la facultad '" + nombreFacultad + "'.");
                envelope.send(recepcion);
                System.out.println("❌ Rechazada solicitud: programa no válido.");
                continue;
            }

            // Solicitud válida, enviar al servidor
            System.out.println("✅ Facultad y programa válidos. Enviando al servidor...");
            pool.submit(new ManejadorSolicitudesFacultad(solicitud, envio, recepcion, envelope));
        }

        envio.close();
        recepcion.close();
        pool.shutdown();
    }
}

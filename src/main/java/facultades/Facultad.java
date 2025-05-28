package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZFrame;

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

        // Registro asincrónico
        Map<String, String> inscripcion = Map.of("tipo", "inscripcion", "facultad", nombreFacultadInput);
        envio.sendMore("");
        envio.send(gson.toJson(inscripcion));
        System.out.println("📤 Enviando solicitud de inscripción para facultad: " + nombreFacultad);

        List<String> programasValidos = FACULTADES_PROGRAMAS.getOrDefault(nombreFacultad, Collections.emptyList());
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println("🟢 Facultad '" + nombreFacultad + "' escuchando solicitudes de sus programas...");

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg mensaje = ZMsg.recvMsg(recepcion);
            if (mensaje == null || mensaje.size() < 2) continue;

            String identificadorPrograma = mensaje.popString();
            String solicitudStr = mensaje.popString();

            Solicitud solicitud = gson.fromJson(solicitudStr, Solicitud.class);
            if (solicitud == null || solicitud.getPrograma() == null) continue;

            String programa = solicitud.getPrograma();
            System.out.println("📥 Solicitud recibida de programa: '" + programa + "', ID: " + solicitud.getId());

            if (programasValidos.contains(programa)) {
                pool.submit(new ManejadorSolicitudesFacultad(solicitud, envio, mensaje));
            } else {
                String mensajeError = "ERROR: El programa '" + programa + "' no pertenece a la facultad '" + nombreFacultad + "'.";
                ZMsg respuesta = new ZMsg();
                respuesta.addString(identificadorPrograma);
                respuesta.addString(mensajeError);
                respuesta.send(recepcion);
                System.out.println("❌ Rechazada solicitud de " + programa + ": no corresponde a esta facultad.");
            }
        }

        envio.close();
        recepcion.close();
        pool.shutdown();
    }
}

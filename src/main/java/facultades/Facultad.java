package facultades;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Facultad {

    private static final int PUERTO_SERVIDOR = 5555;
    private static final int PUERTO_RECEPCION = 6000;
    private static final int TIMEOUT_MS = 3000;

    private static final Map<String, List<String>> FACULTADES_PROGRAMAS = Map.of(
            "Ciencias Sociales", List.of("Psicología", "Sociología", "Trabajo Social", "Antropología", "Comunicación"),
            "Ciencias Naturales", List.of("Biología", "Química", "Física", "Geología", "Ciencias Ambientales"),
            "Ingeniería",
            List.of("Ingeniería Civil", "Ingeniería Electrónica", "Ingeniería de Sistemas", "Ingeniería Mecánica",
                    "Ingeniería Industrial"),
            "Medicina", List.of("Medicina General", "Enfermería", "Odontología", "Farmacia", "Terapia Física"),
            "Derecho",
            List.of("Derecho Penal", "Derecho Civil", "Derecho Internacional", "Derecho Laboral",
                    "Derecho Constitucional"),
            "Artes", List.of("Bellas Artes", "Música", "Teatro", "Danza", "Diseño Gráfico"),
            "Educación",
            List.of("Educación Primaria", "Educación Secundaria", "Educación Especial", "Psicopedagogía",
                    "Administración Educativa"),
            "Ciencias Económicas",
            List.of("Administración de Empresas", "Contabilidad", "Economía", "Mercadotecnia", "Finanzas"),
            "Arquitectura",
            List.of("Arquitectura", "Urbanismo", "Diseño de Interiores", "Paisajismo", "Restauración de Patrimonio"),
            "Tecnología", List.of("Desarrollo de Software", "Redes y Telecomunicaciones", "Ciberseguridad",
                    "Inteligencia Artificial", "Big Data"));

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

        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        envio.setIdentity(("FAC-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
        envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

        Poller poller = context.poller(1);
        poller.register(envio, Poller.POLLIN);

        Map<String, String> inscripcion = Map.of("tipo", "inscripcion", "facultad", nombreFacultadInput);
        boolean inscrito = false;
        int intentos = 0;

        while (!inscrito && intentos < 5) {
            envio.send("", ZMQ.SNDMORE);
            envio.send(gson.toJson(inscripcion));
            System.out.println("📤 Intento de inscripción #" + (intentos + 1));

            if (poller.poll(TIMEOUT_MS) > 0) {
                String frame1 = envio.recvStr(); // Frame vacío (lo envió el servidor como delimitador)
                String frame2 = envio.recvStr(); // Frame con el mensaje real

                System.out.println("🧾 Frame 1 (vacío): '" + frame1 + "'");
                System.out.println("📬 Frame 2 (respuesta): '" + frame2 + "'");

                if ("Inscripción exitosa".equals(frame2)) {
                    inscrito = true;
                    System.out.println("✅ Facultad '" + nombreFacultad + "' aceptada por el servidor.");
                    break;
                } else {
                    System.out.println("⚠️ Facultad rechazada: " + frame2);
                }
            } else {
                System.out.println("⚠️ No hubo respuesta del servidor. Reintentando...");
            }

            intentos++;
        }

        if (!inscrito) {
            System.err.println("❌ No se pudo registrar la facultad. Cerrando.");
            recepcion.close();
            envio.close();
            return;
        }

        ExecutorService pool = Executors.newCachedThreadPool();
        List<String> programasValidos = FACULTADES_PROGRAMAS.getOrDefault(nombreFacultad, Collections.emptyList());

        System.out.println("🟢 Facultad '" + nombreFacultad + "' escuchando solicitudes de sus programas...");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String solicitudStr = recepcion.recvStr();
                Solicitud solicitud = gson.fromJson(solicitudStr, Solicitud.class);
                String programa = solicitud.getPrograma();

                System.out.println("📥 Solicitud recibida de programa: '" + programa + "', ID: " + solicitud.getId());

                if (programasValidos.contains(programa)) {
                    pool.submit(new facultades.ManejadorSolicitudesFacultad(solicitud, envio, recepcion));
                } else {
                    String mensaje = "ERROR: El programa '" + programa + "' no pertenece a la facultad '"
                            + nombreFacultad + "'.";
                    recepcion.send(mensaje);
                    System.out.println("❌ Rechazada solicitud de " + programa + ": no corresponde a esta facultad.");
                }

            } catch (Exception e) {
                System.err.println("❌ Error al recibir o procesar solicitud: " + e.getMessage());
            }
        }

        envio.close();
        recepcion.close();
        pool.shutdown();
    }
}

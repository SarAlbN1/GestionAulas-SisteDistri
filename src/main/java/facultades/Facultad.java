package facultades;

import org.zeromq.ZMQ;
import com.google.gson.Gson;

import java.util.*;

public class Facultad {

    private static final int PUERTO_RECEPCION = 6000;
    private static final int PUERTO_SERVIDOR = 5555;
    private static final int REINTENTO_MS = 3000;
    private static final int MAX_PROGRAMAS = 5;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Uso: java Facultad <ipServidor> <nombreFacultad>");
            return;
        }

        String ipServidor = args[0];
        String nombreFacultad = args[1];
        Queue<String> colaSolicitudes = new LinkedList<>();

        ZMQ.Context context = ZMQ.context(1);
        Gson gson = new Gson();

        // Socket para recibir solicitudes desde los programas académicos
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        // Socket para enviar solicitudes al servidor
        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        envio.setIdentity(("FAC-" + PUERTO_RECEPCION).getBytes(ZMQ.CHARSET));

        // Intento de inscripción al servidor
        boolean conectado = false;
        while (!conectado) {
            try {
                envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

                Map<String, String> inscripcion = Map.of(
                    "tipo", "inscripcion",
                    "facultad", nombreFacultad
                );

                envio.send(gson.toJson(inscripcion));
                String ack = envio.recvStr();

                if ("Inscripción exitosa".equals(ack)) {
                    System.out.println("[Facultad] Inscripción confirmada en el servidor.");
                    conectado = true;
                } else {
                    System.out.println("[Facultad] ⚠️ Respuesta inesperada. Esperando confirmación...");
                    Thread.sleep(REINTENTO_MS);
                }
            } catch (Exception e) {
                System.out.println("[Facultad] ❌ No se pudo conectar al servidor. Reintentando...");
                envio.close();
                envio = context.socket(ZMQ.DEALER);
                envio.setIdentity(("FAC-" + PUERTO_RECEPCION).getBytes(ZMQ.CHARSET));
                Thread.sleep(REINTENTO_MS);
            }
        }

        System.out.println("[Facultad][async] Esperando solicitudes en el puerto " + PUERTO_RECEPCION);

        int solicitudesAtendidas = 0;

        while (solicitudesAtendidas < MAX_PROGRAMAS) {
            try {
                // 1. Recibe solicitud del programa académico
                String json = recepcion.recvStr();
                colaSolicitudes.offer(json);

                // 2. Enviar al servidor con reintento
                boolean entregado = false;
                while (!entregado) {
                    try {
                        String actual = colaSolicitudes.peek();
                        envio.send(actual);
                        String respuesta = envio.recvStr();

                        recepcion.send(respuesta);
                        System.out.println("[Facultad][async] Respuesta enviada al programa académico.");

                        colaSolicitudes.poll(); // Eliminada de la cola
                        entregado = true;
                        solicitudesAtendidas++;
                    } catch (Exception e) {
                        System.out.println("[Facultad] ⚠️ No se pudo contactar al servidor. Reintentando...");
                        Thread.sleep(REINTENTO_MS);
                    }
                }

            } catch (Exception e) {
                System.out.println("[Facultad] ❌ Error al procesar solicitud entrante.");
            }
        }

        System.out.println("[Facultad] ✔️ Límite de programas alcanzado. Cerrando...");
        envio.close();
        recepcion.close();
        context.term();
    }
}

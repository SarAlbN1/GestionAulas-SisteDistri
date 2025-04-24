package facultades;

import org.zeromq.ZMQ;
import com.google.gson.Gson;
import java.util.*;

public class Facultad {

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
        recepcion.bind("tcp://*:" + 6000);  // Puerto para recibir solicitudes de los programas académicos

        // Socket para enviar solicitudes al servidor
        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        envio.setIdentity(("FAC-" + nombreFacultad).getBytes(ZMQ.CHARSET));  // Configuramos el clientId

        // Intento de inscripción al servidor
        boolean conectado = false;
        while (!conectado) {
            try {
                envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

                Map<String, String> inscripcion = Map.of(
                    "tipo", "inscripcion",
                    "facultad", nombreFacultad
                );

                // Enviar mensaje con el clientId y el cuerpo del mensaje
                envio.send("", ZMQ.SNDMORE);  // Primer frame vacío
                envio.send(gson.toJson(inscripcion));  // Enviar solicitud de inscripción

                String ack = envio.recvStr();  // Esperar respuesta

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
                envio = context.socket(ZMQ.DEALER);  // Reconectar
                envio.setIdentity(("FAC-" + nombreFacultad).getBytes(ZMQ.CHARSET));
                Thread.sleep(REINTENTO_MS);
            }
        }

        System.out.println("[Facultad] Esperando solicitudes en el puerto 6000");

        int solicitudesAtendidas = 0;

        // Procesar solicitudes de programas académicos
        while (solicitudesAtendidas < MAX_PROGRAMAS) {
            try {
                // Recibe solicitud del programa académico
                String json = recepcion.recvStr();
                colaSolicitudes.offer(json);

                // Enviar la solicitud al servidor
                boolean entregado = false;
                while (!entregado) {
                    try {
                        String actual = colaSolicitudes.peek();
                        // Enviar solicitud con el clientId
                        envio.send("", ZMQ.SNDMORE);  // Primer frame vacío
                        envio.send(actual);  // Enviar solicitud al servidor
                        String respuesta = envio.recvStr();  // Recibir respuesta

                        recepcion.send(respuesta);  // Enviar la respuesta al programa académico
                        System.out.println("[Facultad] Respuesta enviada al programa académico.");

                        colaSolicitudes.poll();  // Eliminar solicitud procesada
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

        System.out.println("[Facultad] Límite de programas alcanzado. Cerrando...");
        envio.close();
        recepcion.close();
        context.term();
    }
}

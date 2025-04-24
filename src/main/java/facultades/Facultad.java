package facultades;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * Facultad que recibe solicitudes de programas y las reenvía al servidor central.
 * Soporta dos modos:
 *  - sync  → patrón REQ/REP
 *  - async → patrón DEALER/ROUTER
 */
public class Facultad {

    private static final int PUERTO_RECEPCION = 6000;
    private static final int PUERTO_SERVIDOR   = 5555;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Facultad <sync|async> <ipServidor>");
            return;
        }
        String mode       = args[0];
        String ipServidor = args[1];

        ZMQ.Context context = ZMQ.context(1);

        // Socket REP para recibir solicitudes de los programas
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        // Socket para reenviar al servidor: REQ en sync, DEALER en async
        ZMQ.Socket envio = context.socket(
            mode.equals("async") ? ZMQ.DEALER : ZMQ.REQ
        );
        if (mode.equals("async")) {
            // Identidad necesaria para que ROUTER sepa a quién responder
            envio.setIdentity(("FAC-" + PUERTO_RECEPCION).getBytes(ZMQ.CHARSET));
        }
        envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);
        System.out.println("[Facultad][" + mode + "] conectada al servidor en tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

        Gson gson = new Gson();
        System.out.println("[Facultad][" + mode + "] esperando en " + PUERTO_RECEPCION);

        while (true) {
            // 1) Recibir JSON del programa académico
            String json = recepcion.recvStr();
            Solicitud s = gson.fromJson(json, Solicitud.class);
            System.out.println("[Facultad] Solicitud recibida del programa → Facultad = " + s.getFacultad()
    + ", Salones = " + s.getSalones()
    + ", Laboratorios = " + s.getLaboratorios());


            // 2) Enviar al servidor (no bloqueante si async)
            envio.send(json);

            // 3) Esperar respuesta del servidor
            String resp = envio.recvStr();
            System.out.println("[Facultad][" + mode + "] respuesta del servidor: " + resp);

            // 4) Devolver respuesta al programa
            recepcion.send(resp);
        }
    }
}

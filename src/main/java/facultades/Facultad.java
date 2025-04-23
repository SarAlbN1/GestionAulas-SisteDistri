package facultades;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * Facultad que envía solicitudes al Servidor Central usando DEALER (asíncrono).
 */
public class Facultad {

    private static final int PUERTO_RECEPCION = 6000;
    private static final String IP_SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5555;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Facultad <ipServidor>");
            return;
        }
        String ipServidor = args[0];

        ZMQ.Context context = ZMQ.context(1);

        // sigue RECIBIENDO peticiones de Programas
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        // ENVÍA de forma ASÍNCRONA al Servidor Central
        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        // le damos una identidad para que ROUTER pueda responder
        envio.setIdentity(("FAC-"+PUERTO_RECEPCION).getBytes(ZMQ.CHARSET));
        envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

        Gson gson = new Gson();
        System.out.println("[Facultad] Asíncrona, esperando solicitudes en " + PUERTO_RECEPCION);

        while (true) {
            // 1) Llega la petición JSON del Programa
            String json = recepcion.recvStr();
            Solicitud sol = gson.fromJson(json, Solicitud.class);
            System.out.println("[Facultad] Recibida de " + sol.getPrograma());

            // 2) Envío no bloqueante al Servidor
            envio.send(json);

            // 3) Bloquea hasta que la respuesta asíncrona regrese
            String respuesta = envio.recvStr();
            System.out.println("[Facultad] Respuesta del servidor: " + respuesta);

            // 4) Devuelve al Programa
            recepcion.send(respuesta);
        }
    }
}

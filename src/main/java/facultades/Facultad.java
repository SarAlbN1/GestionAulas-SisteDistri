package facultades;

import modelo.Solicitud;


import com.google.gson.Gson;
import org.zeromq.ZMQ;

/**
 * Facultad que recibe solicitudes de programas y las reenvía al servidor central.
 */
public class Facultad {

    private static final int PUERTO_RECEPCION = 6000;
    private static final String IP_SERVIDOR = "tcp://localhost:5555"; // por defecto

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Facultad <ipServidor>");
            return;
        }

        String ipServidor = args[0];

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        ZMQ.Socket envio = context.socket(ZMQ.REQ);
        envio.connect("tcp://" + ipServidor + ":5555");

        Gson gson = new Gson();

        while (true) {
            System.out.println("[Facultad] Esperando solicitud de programa...");
            String json = recepcion.recvStr();
            Solicitud solicitud = gson.fromJson(json, Solicitud.class);
            System.out.println("[Facultad] Solicitud recibida: " + solicitud);

            envio.send(json); // reenviar al servidor
            String respuestaServidor = envio.recvStr(); // espera respuesta
            recepcion.send(respuestaServidor); // reenviar a programa
        }
    }
}

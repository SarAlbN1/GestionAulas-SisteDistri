package facultades;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

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

        // 1) REP para recibir de programas
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        // 2) Según modo, REQ o DEALER para enviar al servidor
        ZMQ.Socket envio = context.socket(
            mode.equals("async") ? ZMQ.DEALER : ZMQ.REQ
        );
        if (mode.equals("async")) {
            envio.setIdentity(("FAC-"+PUERTO_RECEPCION).getBytes(ZMQ.CHARSET));
        }
        envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

        Gson gson = new Gson();
        System.out.println("[Facultad]["+mode+"] esperando en " + PUERTO_RECEPCION);

        while (true) {
            String json = recepcion.recvStr();
            Solicitud s = gson.fromJson(json, Solicitud.class);
            System.out.println("[Facultad]["+mode+"] recibida: " + s);

            envio.send(json);
            String resp = envio.recvStr();
            recepcion.send(resp);
        }
    }
}

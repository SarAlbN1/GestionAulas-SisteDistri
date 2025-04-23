package programas;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * ProgramaAcadémico genera y envía una solicitud a la Facultad indicada.
 * Puede operar en modo síncrono (REQ/REP) o asíncrono (DEALER/ROUTER) según el primer argumento.
 */
public class ProgramaAcademico {

    private static final int PUERTO_FACULTAD = 6000;

    public static void main(String[] args) {
        if (args.length < 7) {
            System.out.println("Uso: java ProgramaAcademico <sync|async> "
                + "<nombrePrograma> <codigoFacultad> <semestre> "
                + "<salones> <laboratorios> <ipFacultad>");
            return;
        }
        String mode         = args[0];
        String nombre       = args[1];
        String codFac       = args[2];
        int    semestre     = Integer.parseInt(args[3]);
        int    salones      = Integer.parseInt(args[4]);
        int    laboratorios = Integer.parseInt(args[5]);
        String ipFacultad   = args[6];

        // Crear objeto Solicitud
        Solicitud solicitud = new Solicitud(
            nombre, codFac, semestre, salones, laboratorios
        );

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket   = null;
        try {
            // Elegir REQ o DEALER según modo
            socket = context.socket(
                mode.equals("async") ? ZMQ.DEALER : ZMQ.REQ
            );
            if (mode.equals("async")) {
                socket.setIdentity(("PROG-" + nombre).getBytes(ZMQ.CHARSET));
            }
            socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FACULTAD);

            // Enviar y recibir
            String json = new Gson().toJson(solicitud);
            socket.send(json);
            String respuesta = socket.recvStr();
            System.out.println("[Programa][" + mode + "] respuesta: " + respuesta);
        } finally {
            if (socket != null) socket.close();
            context.term();
        }
    }
}

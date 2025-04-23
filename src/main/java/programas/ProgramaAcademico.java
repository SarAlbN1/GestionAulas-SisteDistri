package programas;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

public class ProgramaAcademico {

    private static final int PUERTO_FAC = 6000;

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Uso: java ProgramaAcademico <sync|async> <nombre> <codFac> <sem> <sal> <lab> <ipFac>");
            return;
        }
        String mode         = args[0];
        String nombre       = args[1];
        String codFac       = args[2];
        int    semestre     = Integer.parseInt(args[3]);
        int    salones      = Integer.parseInt(args[4]);
        int    laboratorios = Integer.parseInt(args[5]);
        String ipFacultad   = args[6];

        Solicitud sol = new Solicitud(nombre, codFac, semestre, salones, laboratorios);
        ZMQ.Context context = ZMQ.context(1);

        // REQ o DEALER hacia Facultad
        ZMQ.Socket socket = context.socket(
            mode.equals("async") ? ZMQ.DEALER : ZMQ.REQ
        );
        if (mode.equals("async")) {
            socket.setIdentity(("PROG-"+nombre).getBytes(ZMQ.CHARSET));
        }
        socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FAC);

        String json = new Gson().toJson(sol);
        socket.send(json);
        String resp = socket.recvStr();
        System.out.println("[Programa]["+mode+"] respuesta: " + resp);

        socket.close();
        context.term();
    }
}

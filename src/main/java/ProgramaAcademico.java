

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * ProgramaAcademico genera y envía una solicitud a la Facultad indicada.
 * Sigue un patrón síncrono REQ/REP con la Facultad o proxy.
 */
public class ProgramaAcademico {

    private static final int PUERTO_FACULTAD = 6000;
    private static final int ARGS_REQUERIDOS   = 6;

    /**
     * Punto de entrada del programa.
     *
     * @param args args[0]=nombrePrograma, args[1]=codigoFacultad, 
     *             args[2]=semestre, args[3]=salones, args[4]=laboratorios,
     *             args[5]=ipFacultad
     */
    public static void main(String[] args) {
        if (args.length < ARGS_REQUERIDOS) {
            System.out.println("Uso: java ProgramaAcademico <nombrePrograma> <codigoFacultad> "
                + "<semestre> <salones> <laboratorios> <ipFacultad>");
            return;
        }

        String nombrePrograma = args[0];
        String codigoFacultad = args[1];
        int semestre, salones, laboratorios;

        try {
            semestre     = Integer.parseInt(args[2]);
            salones      = Integer.parseInt(args[3]);
            laboratorios = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Error: semestre, salones y laboratorios deben ser enteros.");
            return;
        }

        if (semestre < 1 || salones < 0 || laboratorios < 0) {
            System.err.println("Error: valores inválidos. Semestre ≥1, salones ≥0, laboratorios ≥0.");
            return;
        }

        String ipFacultad = args[5];
        Solicitud solicitud = new Solicitud(
            nombrePrograma, codigoFacultad, semestre, salones, laboratorios
        );

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket   = null;
        try {
            socket = context.socket(ZMQ.REQ);
            socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FACULTAD);

            String json = new Gson().toJson(solicitud);
            socket.send(json);
            String respuesta = socket.recvStr();
            System.out.println("[Programa] Respuesta recibida: " + respuesta);
        } finally {
            if (socket != null) socket.close();
            context.term();
        }
    }
}

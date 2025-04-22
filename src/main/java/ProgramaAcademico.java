
import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * ProgramaAcademico genera y envía una solicitud a la Facultad indicada.
 */
public class ProgramaAcademico {

    // Puerto en el que escucha la Facultad (antes 6000 en literal)
    private static final int PUERTO_FACULTAD = 6000;

    // Número esperado de argumentos
    private static final int ARGS_REQUERIDOS = 6;

    public static void main(String[] args) {
        if (args.length < ARGS_REQUERIDOS) {
            System.out.println("Uso: java ProgramaAcademico "
                + "<nombrePrograma> <codigoFacultad> <semestre> "
                + "<salones> <laboratorios> <ipFacultad>");
            return;
        }

        String nombrePrograma = args[0];
        String codigoFacultad = args[1];
        int semestre, salones, laboratorios;

        try {
            semestre      = Integer.parseInt(args[2]);
            salones       = Integer.parseInt(args[3]);
            laboratorios  = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Error: semestre, salones y laboratorios deben ser enteros.");
            return;
        }

        // Validar que no sean negativos
        if (semestre < 1 || salones < 0 || laboratorios < 0) {
            System.err.println("Error: valores inválidos. Semestre ≥1, salones ≥0, laboratorios ≥0.");
            return;
        }

        String ipFacultad = args[5];

        // Crear objeto Solicitud con código de Facultad dinámico
        Solicitud solicitud = new Solicitud(
            nombrePrograma,
            codigoFacultad,
            semestre,
            salones,
            laboratorios
        );

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket   = null;

        try {
            // Inicializar socket REQ
            socket = context.socket(ZMQ.REQ);
            socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FACULTAD);

            // Serializar y enviar
            Gson gson = new Gson();
            String json = gson.toJson(solicitud);
            socket.send(json);

            // Esperar y mostrar respuesta
            String respuesta = socket.recvStr();
            System.out.println("[Programa] Respuesta recibida: " + respuesta);

        } catch (Exception e) {
            System.err.println("Error en comunicación ZeroMQ: " + e.getMessage());
        } finally {
            // Asegurar cierre de socket y contexto
            if (socket != null) socket.close();
            context.term();
        }
    }
}

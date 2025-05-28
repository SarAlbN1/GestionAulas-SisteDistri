package programas;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

public class ProgramaAcademico {

    private static final int PUERTO_FACULTAD = 6000;

    public static void main(String[] args) {
        if (args.length != 6) {
            System.err.println("❌ Número incorrecto de argumentos.");
            System.out.println("Uso: java ProgramaAcademico <nombrePrograma> <nombreFacultad> "
                    + "<semestre> <salones> <laboratorios> <ipFacultad>");
            return;
        }

        if (args.length < 6) {
            System.out.println("Uso: java ProgramaAcademico <nombrePrograma> <nombreFacultad> "
                    + "<semestre> <salones> <laboratorios> <ipFacultad>");
            return;
        }

        String nombrePrograma = args[0];
        String nombreFacultad = args[1];
        int semestre = Integer.parseInt(args[2]);
        int salones = Integer.parseInt(args[3]);
        int laboratorios = Integer.parseInt(args[4]);
        String ipFacultad = args[5];

        Solicitud solicitud = new Solicitud(nombrePrograma, nombreFacultad, semestre, salones, laboratorios);

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FACULTAD);

        Gson gson = new Gson();

        try {
            String json = gson.toJson(solicitud);
            System.out.println("[Programa " + nombrePrograma + "] 📤 Enviando solicitud: " + json);
            socket.send(json);

            String respuesta = socket.recvStr();
            System.out.println("[Programa " + nombrePrograma + "] 📥 Respuesta: " + respuesta);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar solicitud: " + e.getMessage());
        } finally {
            socket.close();
            context.term();
        }
    }
}
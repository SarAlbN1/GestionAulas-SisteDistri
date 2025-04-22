package programas;

import modelo.Solicitud;
import org.zeromq.ZMQ;
import com.google.gson.Gson;

/**
 * Programa académico que genera y envía una solicitud a su Facultad correspondiente.
 */
public class Programa1 {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Uso: java Programa1 <nombrePrograma> <semestre> <salones> <laboratorios> <ipFacultad>");
            return;
        }

        String nombrePrograma = args[0];
        int semestre = Integer.parseInt(args[1]);
        int salones = Integer.parseInt(args[2]);
        int laboratorios = Integer.parseInt(args[3]);
        String ipFacultad = args[4];

        // Crear solicitud
        Solicitud solicitud = new Solicitud(nombrePrograma, "FACULTAD_X", semestre, salones, laboratorios);

        // Inicializar ZeroMQ
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://" + ipFacultad + ":6000");

        // Enviar solicitud como JSON
        Gson gson = new Gson();
        String json = gson.toJson(solicitud);
        socket.send(json);

        // Esperar respuesta
        String respuesta = socket.recvStr();
        System.out.println("[Programa] Respuesta recibida: " + respuesta);

        socket.close();
        context.term();
    }
}

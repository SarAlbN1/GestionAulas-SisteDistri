package programas;

import org.zeromq.ZMQ;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import modelo.Solicitud;
import java.util.Map;

/**
 * ProgramaAcadémico que envía una solicitud a su Facultad usando comunicación asíncrona DEALER ↔ ROUTER.
 */
public class ProgramaAcademico {

    private static final int PUERTO_FACULTAD = 6000;

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Uso: java ProgramaAcademico <nombrePrograma> <nombreFacultad> "
                + "<semestre> <salones> <laboratorios> <ipFacultad>");
            return;
        }

        String nombrePrograma  = args[0];
        String nombreFacultad  = args[1];
        int semestre           = Integer.parseInt(args[2]);
        int salones            = Integer.parseInt(args[3]);
        int laboratorios       = Integer.parseInt(args[4]);
        String ipFacultad      = args[5];

        Solicitud solicitud = new Solicitud(
            nombrePrograma, nombreFacultad, semestre, salones, laboratorios
        );

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.DEALER);

        // Establecer identidad para trazabilidad desde Facultad
        socket.setIdentity(("PROG-" + nombrePrograma).getBytes(ZMQ.CHARSET));
        Gson gson = new Gson();

        try {
            socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FACULTAD);

            String json = gson.toJson(solicitud);
            socket.send(json);

            String respuestaJson = socket.recvStr();
            Map<String, Object> respuesta = gson.fromJson(respuestaJson, new TypeToken<Map<String, Object>>() {}.getType());

            System.out.println("\n📥 Respuesta de la Facultad:");
            System.out.println("  Estado: " + respuesta.get("estado"));

            if ("asignado".equals(respuesta.get("estado"))) {
                System.out.println("  Programa: " + respuesta.get("programa"));
                System.out.println("  Facultad: " + respuesta.get("facultad"));
                System.out.println("  Semestre: " + ((Double) respuesta.get("semestre")).intValue());
                System.out.println("  Salones asignados: " + ((Double) respuesta.get("salonesAsignados")).intValue());
                System.out.println("  Laboratorios asignados: " + ((Double) respuesta.get("laboratoriosAsignados")).intValue());
            } else {
                System.out.println("  Motivo: " + respuesta.get("motivo"));
            }

        } catch (Exception e) {
            System.err.println("❌ Error de comunicación con la Facultad: " + e.getMessage());
        } finally {
            socket.close();
            context.term();
        }
    }
}

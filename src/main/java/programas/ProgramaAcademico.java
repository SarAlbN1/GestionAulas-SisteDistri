// programas/ProgramaAcademico.java
package programas;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.UUID;

public class ProgramaAcademico {

    private static final int PUERTO_FACULTAD = 6000;

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Uso: java ProgramaAcademico <nombrePrograma> <nombreFacultad> <semestre> <salones> <laboratorios> <ipFacultad>");
            return;
        }

        String nombrePrograma = args[0];
        String nombreFacultad = args[1];
        int semestre = Integer.parseInt(args[2]);
        int salones = Integer.parseInt(args[3]);
        int laboratorios = Integer.parseInt(args[4]);
        String ipFacultad = args[5];

        Solicitud solicitud = new Solicitud(nombrePrograma, nombreFacultad, semestre, salones, laboratorios);
        Gson gson = new Gson();
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.DEALER);
        socket.setIdentity(("PROG-" + UUID.randomUUID()).getBytes(ZMQ.CHARSET));
        socket.connect("tcp://" + ipFacultad + ":" + PUERTO_FACULTAD);

        try {
            ZMsg msg = new ZMsg();
            msg.addString(""); // Simula encabezado vacio
            msg.addString(gson.toJson(solicitud));
            msg.send(socket);
            System.out.println("📤 Enviado JSON: " + gson.toJson(solicitud));

            ZMsg respuesta = ZMsg.recvMsg(socket);
            if (respuesta != null) {
                System.out.println("📥 Respuesta recibida:");
                respuesta.forEach(part -> System.out.println("🧩 Parte: " + part.toString()));
            } else {
                System.err.println("❌ No hubo respuesta de la facultad.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al enviar solicitud: " + e.getMessage());
        } finally {
            socket.close();
            context.term();
        }
    }
}

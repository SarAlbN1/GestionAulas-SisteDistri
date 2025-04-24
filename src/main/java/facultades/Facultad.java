package facultades;

import facultades.FacultadLauncher;
import org.zeromq.ZMQ;
import com.google.gson.Gson;
import java.util.*;

public class Facultad {

    private static final int PUERTO_SERVIDOR = 5555;
    private static final int REINTENTO_MS = 3000;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Uso: java Facultad <ipServidor> <nombreFacultad>");
            return;
        }

        String ipServidor = args[0];
        String nombreFacultad = args[1];

        // Lanzar todas las facultades si no están activas
        FacultadLauncher.lanzarFacultades();

        // Verificar si la facultad solicitada ya está en el mapa del launcher (sin importar espacios o mayúsculas)
        boolean encontrada = FacultadLauncher.existeFacultad(nombreFacultad);

        if (!encontrada) {
            System.out.println("[Facultad] ❌ La facultad '" + nombreFacultad + "' no está registrada en el sistema.");
            return;
        }

        // Simular inscripción de la facultad desde su propio hilo
        ZMQ.Context context = ZMQ.context(1);
        Gson gson = new Gson();

        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        envio.setIdentity(("FAC-" + nombreFacultad).getBytes(ZMQ.CHARSET));

        boolean conectado = false;
        while (!conectado) {
            try {
                envio.connect("tcp://" + ipServidor + ":" + PUERTO_SERVIDOR);

                Map<String, String> inscripcion = Map.of(
                    "tipo", "inscripcion",
                    "facultad", nombreFacultad
                );

                envio.send("", ZMQ.SNDMORE);
                envio.send(gson.toJson(inscripcion));

                String ack = envio.recvStr();
                if ("Inscripción exitosa".equals(ack)) {
                    System.out.println("[Facultad] Inscripción confirmada en el servidor para '" + nombreFacultad + "'.");
                    conectado = true;
                } else {
                    System.out.println("[Facultad] ⚠️ Respuesta inesperada. Esperando confirmación...");
                    Thread.sleep(REINTENTO_MS);
                }
            } catch (Exception e) {
                System.out.println("[Facultad] ❌ Error de conexión. Reintentando...");
                envio.close();
                envio = context.socket(ZMQ.DEALER);
                envio.setIdentity(("FAC-" + nombreFacultad).getBytes(ZMQ.CHARSET));
                Thread.sleep(REINTENTO_MS);
            }
        }

        System.out.println("[Facultad] ✅ Facultad activa y operando. Las solicitudes serán manejadas por su hilo correspondiente.");
    }
}
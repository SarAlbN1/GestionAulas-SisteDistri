package servidor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import modelo.Solicitud;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

/**
 * Servidor central. Acepta inscripciones de facultades y solicitudes de asignación.
 * Usa comunicación asíncrona DEALER ↔ ROUTER.
 */
public class Servidor {

    private static final int PUERTO = 5555;

    // Registro central de facultades inscritas (nombre ↔ identidad ZMQ)
    private static final Map<String, String> facultades = new HashMap<>();

    public static boolean esFacultadInscrita(String nombreFacultad) {
        return facultades.containsKey(nombreFacultad);
    }

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.ROUTER);
        socket.bind("tcp://0.0.0.0:" + PUERTO);

        AsignadorAulas asignador = new AsignadorAulas();
        Persistencia persistencia = new Persistencia();
        Gson gson = new Gson();

        System.out.println("[Servidor] 🟢 Escuchando en el puerto " + PUERTO);

        while (true) {
            try {
                byte[] identidad = socket.recv(); // Identificador de cliente
                socket.recv(); // Frame vacío (por protocolo ROUTER)
                String mensaje = socket.recvStr();

                System.out.println("\n[Servidor] 🧾 Identidad: " + new String(identidad));
                System.out.println("[Servidor] 📩 Contenido recibido: " + mensaje);

                // Verificar si es inscripción o solicitud
                Map<String, Object> datos = gson.fromJson(mensaje, new TypeToken<Map<String, Object>>() {}.getType());
                String tipo = (String) datos.get("tipo");

                // 🔹 INSCRIPCIÓN
                if ("inscripcion".equals(tipo)) {
                    String facultad = (String) datos.get("facultad");
                    facultades.put(facultad, new String(identidad));

                    socket.send(identidad, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send("Inscripción exitosa");

                    System.out.println("✅ Facultad registrada: " + facultad);
                    continue;
                }

                // 🔸 SOLICITUD DE ASIGNACIÓN
                new Thread(new ManejadorSolicitudesServidor(
                        identidad,
                        mensaje,
                        socket,
                        asignador,
                        persistencia
                )).start();

            } catch (Exception e) {
                System.err.println("❌ Error procesando mensaje en Servidor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

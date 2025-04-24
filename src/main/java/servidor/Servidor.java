package servidor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import org.zeromq.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import modelo.Solicitud;

public class Servidor {

    private static final int PUERTO = 5555;
    private static final int MAX_HILOS = 10;

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.ROUTER);
        socket.bind("tcp://0.0.0.0:" + PUERTO);  // Puerto único para la comunicación

        // Pool de hilos para manejar solicitudes concurrentemente
        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);
        AsignadorAulas asignador = new AsignadorAulas();
        Persistencia persistencia = new Persistencia();
        Gson gson = new Gson();

        System.out.println("[Servidor] Escuchando en el puerto " + PUERTO);

        while (true) {
            byte[] clientId = socket.recv();
            socket.recv(); // frame vacío
            String json = new String(socket.recv(), ZMQ.CHARSET);  // Solicitud recibida

            System.out.println("[Servidor] Conexión entrante desde: " + new String(clientId, ZMQ.CHARSET));

            // Verificamos si el mensaje recibido es un health-check
            if ("health-check".equals(json)) {
                // Responder a HealthChecker
                System.out.println("[Servidor] Respondíendo al HealthChecker...");
                socket.send(clientId, ZMQ.SNDMORE);
                socket.send("", ZMQ.SNDMORE);
                socket.send("OK");
                continue;  // Continuamos esperando nuevas solicitudes
            }

            // Si no es un health-check, procesamos la solicitud de aula
            pool.submit(() -> {  // Usamos submit para ejecutar en un hilo del pool
                try {
                    Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());

                    if ("inscripcion".equals(data.get("tipo"))) {
                        String nombreFacultad = (String) data.get("facultad");
                        System.out.println("📥 Inscripción recibida de Facultad: " + nombreFacultad);

                        // Respuesta de inscripción
                        socket.send(clientId, ZMQ.SNDMORE);
                        socket.send("", ZMQ.SNDMORE);
                        socket.send("Inscripción exitosa");
                        return;
                    }

                    // Procesar solicitud de aulas
                    Solicitud solicitud = gson.fromJson(json, Solicitud.class);
                    boolean ok = asignador.asignarAulas(solicitud);

                    // Crear respuesta
                    Map<String, Object> respuesta = Map.of(
                        "estado", ok ? "asignado" : "rechazado",
                        "programa", solicitud.getPrograma(),
                        "facultad", solicitud.getFacultad(),
                        "semestre", solicitud.getSemestre(),
                        "salonesAsignados", ok ? solicitud.getSalones() : 0,
                        "laboratoriosAsignados", ok ? solicitud.getLaboratorios() : 0,
                        "motivo", ok ? "" : "⚠️ No hay suficientes aulas disponibles."
                    );

                    // Guardar en persistencia
                    persistencia.guardar(ok ? "asignaciones" : "rechazos", gson.toJson(respuesta));

                    // Enviar respuesta al cliente
                    socket.send(clientId, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send(gson.toJson(respuesta));

                } catch (Exception e) {
                    System.err.println("❌ Error procesando mensaje: " + json);
                    socket.send(clientId, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send("ERROR");
                }
            });
        }
    }
}


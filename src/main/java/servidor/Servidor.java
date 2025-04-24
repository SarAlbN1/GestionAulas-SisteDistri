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
        socket.bind("tcp://0.0.0.0:" + PUERTO);

        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);
        AsignadorAulas asignador = new AsignadorAulas();
        Persistencia persistencia = new Persistencia();
        Gson gson = new Gson();

        System.out.println("[Servidor] Escuchando en el puerto " + PUERTO);

        while (true) {
            byte[] clientId = socket.recv();
            socket.recv(); // frame vacío
            String json = new String(socket.recv(), ZMQ.CHARSET);

            System.out.println("[Servidor] Conexión entrante desde: " + new String(clientId, ZMQ.CHARSET));
            System.out.println("[Servidor] Mensaje recibido: " + json);

            if ("health-check".equals(json)) {
                System.out.println("[Servidor] Respondíendo al HealthChecker...");
                socket.send(clientId, ZMQ.SNDMORE);
                socket.send("", ZMQ.SNDMORE);
                socket.send("OK");
                continue;
            }

            pool.submit(() -> {
                try {
                    Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());

                    if ("inscripcion".equals(data.get("tipo"))) {
                        String nombreFacultad = (String) data.get("facultad");
                        System.out.println("[Servidor] 📥 Inscripción recibida de Facultad: " + nombreFacultad);

                        socket.send(clientId, ZMQ.SNDMORE);
                        socket.send("", ZMQ.SNDMORE);
                        socket.send("Inscripción exitosa");
                        return;
                    }

                    Solicitud solicitud = gson.fromJson(json, Solicitud.class);
                    boolean ok = asignador.asignarAulas(solicitud);

                    Map<String, Object> respuesta = Map.of(
                        "estado", ok ? "asignado" : "rechazado",
                        "programa", solicitud.getPrograma(),
                        "facultad", solicitud.getFacultad(),
                        "semestre", solicitud.getSemestre(),
                        "salonesAsignados", ok ? solicitud.getSalones() : 0,
                        "laboratoriosAsignados", ok ? solicitud.getLaboratorios() : 0,
                        "motivo", ok ? "" : "⚠️ No hay suficientes aulas disponibles."
                    );

                    String respuestaJson = gson.toJson(respuesta);
                    persistencia.guardar(ok ? "asignaciones" : "rechazos", respuestaJson);

                    System.out.println("[Servidor] Enviando respuesta: " + respuestaJson);
                    socket.send(clientId, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send(respuestaJson);

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

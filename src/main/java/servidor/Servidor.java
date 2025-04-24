package servidor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

import org.zeromq.ZMQ;
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

        System.out.println("[Servidor][async] escuchando en el puerto " + PUERTO);

        while (true) {
            byte[] clientId = socket.recv();
            socket.recv(); // empty frame
            String json = new String(socket.recv(), ZMQ.CHARSET);

            System.out.println("[Servidor][async] conexión entrante desde: " + new String(clientId, ZMQ.CHARSET));

            pool.execute(() -> {
                try {
                    Map<String, Object> data = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());

                    if ("inscripcion".equals(data.get("tipo"))) {
                        String nombreFacultad = (String) data.get("facultad");
                        System.out.println("📥 Inscripción recibida de Facultad: " + nombreFacultad);

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
                        "motivo", ok ? "" : "⚠️ No hay suficientes aulas disponibles para satisfacer la solicitud."
                    );

                    if (!ok) {
                        System.out.println("⚠️ ALERTA: No hay recursos suficientes para " + solicitud.getPrograma());
                    }

                    String respuestaJson = gson.toJson(respuesta);
                    persistencia.guardar(ok ? "asignaciones" : "rechazos", respuestaJson);

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

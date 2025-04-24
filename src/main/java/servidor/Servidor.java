package servidor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.zeromq.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import modelo.Solicitud;

/**
 * Servidor central encargado de gestionar inscripciones y solicitudes de aulas.
 */
public class Servidor {

    private static final int PUERTO = 5555;
    private static final int MAX_HILOS = 10;
    private static final Set<String> facultadesInscritas = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        // Lanzar automáticamente el manejador de inscripción en hilo separado
        new Thread(() -> {
            try {
                inscripciones.main(null);
            } catch (Exception e) {
                System.err.println("[Servidor] ❌ Error al iniciar inscripciones: " + e.getMessage());
            }
        }).start();

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

                        boolean yaRegistrada = facultadesInscritas.contains(nombreFacultad);
                        if (!yaRegistrada) {
                            facultadesInscritas.add(nombreFacultad);
                            System.out.println("[Servidor] ✅ Facultad registrada como nueva: " + nombreFacultad);
                        } else {
                            System.out.println("[Servidor] ⚠️ Facultad ya estaba inscrita: " + nombreFacultad);
                        }

                        socket.send(clientId, ZMQ.SNDMORE);
                        socket.send("", ZMQ.SNDMORE);
                        socket.send("Inscripción exitosa");
                        return;
                    }

                    // Procesar solicitud académica
                    Runnable manejador = new ManejadorSolicitudesServidor(clientId, json, socket, asignador, persistencia);
                    manejador.run();

                } catch (Exception e) {
                    System.err.println("❌ Error procesando mensaje: " + json);
                    socket.send(clientId, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send("ERROR");
                }
            });
        }
    }

    public static Set<String> getFacultadesInscritas() {
        return facultadesInscritas;
    }
}

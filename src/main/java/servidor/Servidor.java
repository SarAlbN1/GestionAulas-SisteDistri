package servidor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.HashMap;
import java.util.Map;

public class Servidor {

    private static final int PUERTO = 5555;
    private static final Map<String, byte[]> facultades = new HashMap<>();

    public static boolean esFacultadInscrita(String nombre) {
        return facultades.containsKey(nombre);
    }

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.ROUTER);
            socket.bind("tcp://0.0.0.0:" + PUERTO);

            AsignadorAulas asignador = new AsignadorAulas();
            Persistencia persistencia = new Persistencia();
            Gson gson = new Gson();

            System.out.println("[Servidor] 🟢 Escuchando en el puerto " + PUERTO);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] identidad = socket.recv(0);
                socket.recv(0); // Frame vacío
                String mensaje = socket.recvStr(0);

                System.out.println("🔻 [Servidor] 🔻");
                System.out.println("📨 Identidad ZMQ: " + new String(identidad));
                System.out.println("📄 Contenido: " + mensaje);

                // 🔍 Si es un health-check (simple string, no JSON)
                if ("health-check".equalsIgnoreCase(mensaje)) {
                    System.out.println("💓 HealthChecker conectado.");
                    socket.send(identidad, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send("✅ Servidor en línea (pong)");
                    continue;
                }

                Map<String, Object> datos = gson.fromJson(mensaje, new TypeToken<Map<String, Object>>() {}.getType());
                String tipo = (String) datos.get("tipo");

                if ("inscripcion".equals(tipo)) {
                    String facultad = (String) datos.get("facultad");
                    facultades.put(facultad, identidad);

                    socket.send(identidad, ZMQ.SNDMORE);
                    socket.send("", ZMQ.SNDMORE);
                    socket.send("✅ Inscripción exitosa");
                    System.out.println("✅ Facultad '" + facultad + "' inscrita.");
                    continue;
                }

                new Thread(new ManejadorSolicitudesServidor(identidad, mensaje, socket, asignador, persistencia)).start();
            }
        }
    }
}

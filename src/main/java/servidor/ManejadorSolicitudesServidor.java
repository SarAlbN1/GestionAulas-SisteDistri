package servidor;

import org.zeromq.ZMQ;
import com.google.gson.Gson;
import modelo.Solicitud;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manejador de solicitudes enviadas desde programas académicos hacia el servidor.
 * Este manejador valida que la facultad esté registrada y procesa la asignación de aulas.
 */
public class ManejadorSolicitudesServidor implements Runnable {

    private final byte[] clientId;
    private final String solicitudJson;
    private final ZMQ.Socket socket;
    private final AsignadorAulas asignador;
    private final Persistencia persistencia;
    private static final Set<String> facultadesInscritas = Servidor.getFacultadesInscritas();

    public ManejadorSolicitudesServidor(byte[] clientId,
                                        String solicitudJson,
                                        ZMQ.Socket socket,
                                        AsignadorAulas asignador,
                                        Persistencia persistencia) {
        this.clientId = clientId;
        this.solicitudJson = solicitudJson;
        this.socket = socket;
        this.asignador = asignador;
        this.persistencia = persistencia;
    }

    @Override
    public void run() {
        Gson gson = new Gson();

        try {
            Solicitud sol = gson.fromJson(solicitudJson, Solicitud.class);
            String nombreFacultad = sol.getFacultad();

            if (!facultadesInscritas.contains(nombreFacultad)) {
                System.out.println("[Servidor] ❌ Facultad no registrada: " + nombreFacultad);
                socket.send(clientId, ZMQ.SNDMORE);
                socket.send("", ZMQ.SNDMORE);
                socket.send("ERROR: Facultad no registrada en el sistema");
                return;
            }

            System.out.println("[Servidor] 📚 Procesando solicitud de programa: " + sol.getPrograma());

            boolean ok = asignador.asignarAulas(sol);

            Map<String, Object> respuesta = Map.of(
                "estado", ok ? "asignado" : "rechazado",
                "programa", sol.getPrograma(),
                "facultad", sol.getFacultad(),
                "semestre", sol.getSemestre(),
                "salonesAsignados", ok ? sol.getSalones() : 0,
                "laboratoriosAsignados", ok ? sol.getLaboratorios() : 0,
                "motivo", ok ? "" : "⚠️ No hay suficientes aulas disponibles para satisfacer la solicitud."
            );

            String respuestaJson = gson.toJson(respuesta);
            String tipo = ok ? "asignaciones" : "rechazos";
            persistencia.guardar(tipo, respuestaJson);

            System.out.println("[Servidor] ✅ Respuesta enviada a programa: " + respuestaJson);
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("", ZMQ.SNDMORE);
            socket.send(respuestaJson);

        } catch (Exception e) {
            System.err.println("❌ Error en ManejadorSolicitudesServidor: " + e.getMessage());
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("", ZMQ.SNDMORE);
            socket.send("ERROR");
        }
    }
}

package servidor;

import org.zeromq.ZMQ;
import com.google.gson.Gson;
import modelo.Solicitud;
import java.util.Map;

/**
 * Manejador de solicitudes asíncronas en modo comunicación asíncrona DEALER ↔ ROUTER. Deserializa, procesa, responde con JSON estructurado.
 */
public class ManejadorSolicitudes implements Runnable {

    private final byte[] clientId;
    private final String solicitudJson;
    private final ZMQ.Socket socket;
    private final AsignadorAulas asignador;
    private final Persistencia persistencia;

    public ManejadorSolicitudes(byte[] clientId,
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

            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("",       ZMQ.SNDMORE);
            socket.send(respuestaJson);

        } catch (Exception e) {
            System.err.println("❌ Error en ManejadorSolicitudes: " + e.getMessage());
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("",       ZMQ.SNDMORE);
            socket.send("ERROR");
        }
    }
}

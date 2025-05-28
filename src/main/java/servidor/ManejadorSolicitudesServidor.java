package servidor;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;

import java.util.Map;

/**
 * Manejador de solicitudes asíncronas enviadas por facultades registradas.
 * Usa patrón DEALER ↔ ROUTER.
 */
public class ManejadorSolicitudesServidor implements Runnable {

    private final byte[] clientId;
    private final String solicitudJson;
    private final ZMQ.Socket socket;
    private final AsignadorAulas asignador;
    private final Persistencia persistencia;

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
            String facultad = sol.getFacultad();
            String id = sol.getId();

            // Validar facultad
            if (!Servidor.esFacultadInscrita(facultad)) {
                System.err.println("❌ Solicitud rechazada: facultad no registrada → " + facultad + " (ID: " + id + ")");
                socket.send(clientId, ZMQ.SNDMORE);
                socket.send("", ZMQ.SNDMORE);
                socket.send("ERROR: Facultad no registrada");
                return;
            }

            System.out.println("📚 Procesando solicitud ID " + id + " de programa '" + sol.getPrograma() + "' en facultad '" + facultad + "'");

            boolean ok = asignador.asignarAulas(sol);

            Map<String, Object> respuesta = Map.of(
                "id", id,
                "estado", ok ? "asignado" : "rechazado",
                "programa", sol.getPrograma(),
                "facultad", sol.getFacultad(),
                "semestre", sol.getSemestre(),
                "salonesAsignados", ok ? sol.getSalones() : 0,
                "laboratoriosAsignados", ok ? sol.getLaboratorios() : 0,
                "motivo", ok ? "" : "⚠️ No hay suficientes aulas disponibles."
            );

            String respuestaJson = gson.toJson(respuesta);
            String tipo = ok ? "asignaciones" : "rechazos";

            // Registro en disco
            persistencia.guardar(tipo, respuestaJson);

            // Envío de respuesta
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("", ZMQ.SNDMORE);
            socket.send(respuestaJson);

            System.out.println("✅ Respuesta enviada para solicitud ID: " + id);

        } catch (Exception e) {
            System.err.println("❌ Error en ManejadorSolicitudes: " + e.getMessage());
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("", ZMQ.SNDMORE);
            socket.send("ERROR");

            // Opcional: intentar extraer ID en caso de error para mejor log
            try {
                String id = new Gson().fromJson(solicitudJson, Solicitud.class).getId();
                System.err.println("❗ Ocurrió durante el procesamiento de la solicitud ID: " + id);
            } catch (Exception ignored) {
                System.err.println("❗ No se pudo extraer ID para trazabilidad.");
            }
        }
    }
}

package servidor;

import com.google.gson.Gson;
import modelo.Solicitud;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class ManejadorSolicitudesServidor implements Runnable {

    private final byte[] clientId;
    private final String solicitudJson;
    private final ZMQ.Socket socket;
    private final AsignadorAulas asignador;
    private final Persistencia persistencia;

    public ManejadorSolicitudesServidor(byte[] clientId, String solicitudJson, ZMQ.Socket socket, AsignadorAulas asignador, Persistencia persistencia) {
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

            if (!Servidor.esFacultadInscrita(facultad)) {
                System.err.println("❌ Solicitud rechazada: facultad no registrada → " + facultad + " (ID: " + id + ")");
                socket.send(clientId, ZMQ.SNDMORE);
                socket.send("", ZMQ.SNDMORE);
                socket.send("ERROR: Facultad no registrada");
                return;
            }

            System.out.println("📚 Procesando solicitud ID " + id + " de programa '" + sol.getPrograma() + "'");
            boolean ok = asignador.asignarAulas(sol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", id);
            respuesta.put("estado", ok ? "asignado" : "rechazado");
            respuesta.put("programa", sol.getPrograma());
            respuesta.put("facultad", sol.getFacultad());
            respuesta.put("semestre", sol.getSemestre());
            respuesta.put("salonesAsignados", ok ? sol.getSalones() : 0);
            respuesta.put("laboratoriosAsignados", ok ? sol.getLaboratorios() : 0);
            if (!ok) respuesta.put("motivo", "⚠️ No hay suficientes aulas disponibles.");

            String respuestaJson = gson.toJson(respuesta);
            String tipo = ok ? "asignaciones" : "rechazos";

            persistencia.guardar(tipo, respuestaJson);

            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("", ZMQ.SNDMORE);
            socket.send(respuestaJson);
            System.out.println("✅ Respuesta enviada para solicitud ID: " + id);

        } catch (Exception e) {
            System.err.println("❌ Error procesando solicitud: " + e.getMessage());
            socket.send(clientId, ZMQ.SNDMORE);
            socket.send("", ZMQ.SNDMORE);
            socket.send("ERROR procesando solicitud");
        }
    }
}
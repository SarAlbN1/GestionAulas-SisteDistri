package servidor;

import modelo.Solicitud;
import com.google.gson.Gson;
import org.zeromq.ZMQ;

/**
 * Hilo encargado de manejar cada solicitud recibida por el servidor.
 */
public class ManejadorSolicitudes implements Runnable {

    private final String solicitudJson;
    private final ZMQ.Socket socket;
    private final AsignadorAulas asignador;
    private final Persistencia persistencia;

    public ManejadorSolicitudes(String solicitudJson, ZMQ.Socket socket,
                                AsignadorAulas asignador, Persistencia persistencia) {
        this.solicitudJson = solicitudJson;
        this.socket = socket;
        this.asignador = asignador;
        this.persistencia = persistencia;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        Solicitud solicitud = gson.fromJson(solicitudJson, Solicitud.class);
        System.out.println("[Servidor] Procesando solicitud de " + solicitud.getPrograma());

        boolean asignado = asignador.asignarAulas(solicitud);
        String respuesta = asignado ? "Asignación exitosa" : "Sin aulas disponibles";

        persistencia.guardar(asignado ? "asignaciones" : "error", solicitudJson);
        socket.send(respuesta);
    }
}

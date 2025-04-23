package servidor;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * Atiende en paralelo cada solicitud asíncrona, reenviando la respuesta al DEALER correcto.
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
        this.clientId      = clientId;
        this.solicitudJson = solicitudJson;
        this.socket        = socket;
        this.asignador     = asignador;
        this.persistencia  = persistencia;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        Solicitud sol = gson.fromJson(solicitudJson, Solicitud.class);
        System.out.println("[Manejador] Procesando " + sol.getPrograma());

        boolean ok = asignador.asignarAulas(sol);
        String respuesta = ok ? "Asignación exitosa" : "Sin aulas disponibles";
        String tipo = ok ? "asignaciones" : "error";
        persistencia.guardar(tipo, solicitudJson);

        // RESPUESTA asíncrona: reenviamos el clientId + empty frame + cuerpo
        socket.send(clientId, ZMQ.SNDMORE);
        socket.send("",       ZMQ.SNDMORE);
        socket.send(respuesta);
    }
}

package facultades;

import org.zeromq.ZMQ;
import com.google.gson.Gson;
import modelo.Solicitud;
import java.util.List;


/**
 * Clase que maneja el trabajo de una Facultad en un hilo separado.
 * Recibe solicitudes y las envía al servidor usando su propio socket DEALER.
 */
public class FacultadWorker implements Runnable {

    private final String nombreFacultad;
    private final List<String> programas;
    private final ZMQ.Socket socketRecepcion;
    private final ZMQ.Context context;

    public FacultadWorker(String nombreFacultad, List<String> programas, ZMQ.Socket socketRecepcion, ZMQ.Context context) {
        this.nombreFacultad = nombreFacultad;
        this.programas = programas;
        this.socketRecepcion = socketRecepcion;
        this.context = context;
    }

    @Override
    public void run() {
        // Socket para enviar solicitudes al servidor
        ZMQ.Socket envio = context.socket(ZMQ.DEALER);
        envio.setIdentity(("FAC-" + nombreFacultad).getBytes(ZMQ.CHARSET));  // Asignamos un clientId único

        try {
            // Recibir solicitud del programa académico
            String solicitudJson = socketRecepcion.recvStr();
            System.out.println("[Facultad " + nombreFacultad + "] Solicitud recibida: " + solicitudJson);

            // Crear solicitud (en un caso real, esto sería más complejo)
            Gson gson = new Gson();
            Solicitud solicitud = gson.fromJson(solicitudJson, Solicitud.class);

            // Enviar solicitud al servidor
            envio.connect("tcp://localhost:5555");  // Dirección del servidor
            envio.send(gson.toJson(solicitud));  // Enviar solicitud al servidor

            // Recibir respuesta del servidor
            String respuesta = envio.recvStr();
            System.out.println("[Facultad " + nombreFacultad + "] Respuesta del servidor: " + respuesta);

            // Enviar respuesta de vuelta al programa académico
            socketRecepcion.send(respuesta);
        } catch (Exception e) {
            System.out.println("[Facultad " + nombreFacultad + "] Error: " + e.getMessage());
        } finally {
            // Cerrar el socket para esta Facultad
            envio.close();
        }
    }
}

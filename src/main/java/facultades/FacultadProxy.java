package facultades;

import org.zeromq.ZMQ;

/**
 * FacultadProxy se encarga de intermediar entre los programas académicos
 * y la Facultad real, reenviando solicitudes JSON a través de ZeroMQ.
 */
public class FacultadProxy {

    // Puerto en el que el proxy escuchará solicitudes entrantes de los programas académicos
    private static final int PUERTO_RECEPTOR_PROXY = 6001;

    // Dirección de la máquina donde corre la Facultad real (puede parametrizarse si se desea)
    private static final String IP_FACULTAD_REAL = "localhost";

    // Puerto en el que la Facultad real está escuchando (debe coincidir con PUERTO_RECEPCION de Facultad)
    private static final int PUERTO_FACULTAD_REAL = 6000;

    public static void main(String[] args) {
        // Crear un contexto ZMQ con un único hilo de I/O
        ZMQ.Context context = ZMQ.context(1);

        // Socket REP (reply) para recibir solicitudes de los clientes (programas académicos)
        ZMQ.Socket receptorProxy = context.socket(ZMQ.REP);
        // Vincular el socket a todas las interfaces en el puerto designado para el proxy
        receptorProxy.bind("tcp://*:" + PUERTO_RECEPTOR_PROXY);

        // Socket REQ (request) para reenviar solicitudes a la Facultad real
        ZMQ.Socket enviaFacultad = context.socket(ZMQ.REQ);
        // Conectarse al endpoint de la Facultad real
        enviaFacultad.connect("tcp://" + IP_FACULTAD_REAL + ":" + PUERTO_FACULTAD_REAL);

        System.out.println("[Proxy] En línea. Esperando solicitudes en el puerto " + PUERTO_RECEPTOR_PROXY + "...");

        // Bucle infinito para procesar solicitudes de forma continua
        while (true) {
            // 1. Esperar y recibir el mensaje JSON del programa académico
            String jsonSolicitud = receptorProxy.recvStr();
            System.out.println("[Proxy] Recibida solicitud del programa: " + jsonSolicitud);

            // 2. Reenviar EXACTAMENTE el JSON recibido a la Facultad real
            enviaFacultad.send(jsonSolicitud);
            System.out.println("[Proxy] Solicitud reenviada a Facultad real");

            // 3. Esperar la respuesta de la Facultad real
            String jsonRespuesta = enviaFacultad.recvStr();
            System.out.println("[Proxy] Respuesta recibida de Facultad real: " + jsonRespuesta);

            // 4. Devolver la respuesta al programa académico que realizó la petición
            receptorProxy.send(jsonRespuesta);
            System.out.println("[Proxy] Respuesta reenviada al programa académico");
        }
    }
}

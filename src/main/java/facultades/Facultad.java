package facultades;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * Facultad que recibe solicitudes de programas y las reenvía al servidor central.
 */
public class Facultad {

    // Puerto TCP donde está escuchando esta facultad para recibir peticiones de los programas
    private static final int PUERTO_RECEPCION = 6000;

    // Dirección por defecto del servidor central (se espera que la IP real sea recibida por args)
    private static final String IP_SERVIDOR = "tcp://localhost:5555";

    public static void main(String[] args) {
        // Validación de argumentos: requiere la dirección IP o host del servidor central
        if (args.length < 1) {
            System.out.println("Uso: java Facultad <ipServidor>");
            return;  // Salir si no se proporciona IP del servidor
        }

        // Obtener la dirección IP del servidor desde línea de comandos
        String ipServidor = args[0];

        // Crear un contexto ZMQ con un solo hilo de I/O
        ZMQ.Context context = ZMQ.context(1);

        // Socket de tipo REP (reply) para recibir solicitudes desde los programas
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        // Vincular el socket de recepción a todas las interfaces en el puerto PUERTO_RECEPCION
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);

        // Socket de tipo REQ (request) para reenviar la misma solicitud al servidor central
        ZMQ.Socket envio = context.socket(ZMQ.REQ);
        // Conectar el socket de envío a la dirección del servidor central en su puerto 5555
        envio.connect("tcp://" + ipServidor + ":5555");

        // Instancia de Gson para convertir entre objetos Java y JSON
        Gson gson = new Gson();

        // Bucle infinito para procesar solicitudes de forma continua
        while (true) {
            System.out.println("[Facultad] Esperando solicitud de programa...");

            // Recibir el mensaje JSON enviado por un programa cliente
            String json = recepcion.recvStr();

            // Convertir el JSON recibido en un objeto de tipo Solicitud
            Solicitud solicitud = gson.fromJson(json, Solicitud.class);
            System.out.println("[Facultad] Solicitud recibida: " + solicitud);

            // Reenviar el JSON de la solicitud al servidor central
            envio.send(json);

            // Esperar la respuesta del servidor central (JSON o cualquier cadena)
            String respuestaServidor = envio.recvStr();

            // Enviar la respuesta de vuelta al programa solicitante
            recepcion.send(respuestaServidor);
        }
    }
}

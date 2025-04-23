package servidor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * Servidor Central – Gestión de Aulas
 * Puede correr en modo síncrono (REQ/REP) o asíncrono (ROUTER/DEALER).
 */
public class Servidor {

    private static final int PUERTO    = 5555;
    private static final int MAX_HILOS = 10;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Servidor <sync|async>");
            return;
        }
        String mode = args[0];

        // Crear contexto ZeroMQ
        ZMQ.Context context = ZMQ.context(1);

        // Seleccionar socket REP o ROUTER según modo
        ZMQ.Socket socket = context.socket(
            mode.equals("async") ? ZMQ.ROUTER : ZMQ.REP
        );
        socket.bind("tcp://0.0.0.0:" + PUERTO);

        ExecutorService pool         = Executors.newFixedThreadPool(MAX_HILOS);
        Persistencia    persistencia = new Persistencia();
        AsignadorAulas  asignador    = new AsignadorAulas();
        Gson            gson         = new Gson();

        System.out.println("[Servidor][" + mode + "] escuchando en " + PUERTO);

        while (true) {
            byte[] clientId = null;
            String json;

            if (mode.equals("async")) {
                // En asíncrono recibimos identidad, empty frame y cuerpo
                clientId = socket.recv();
                socket.recv();
                json     = new String(socket.recv(), ZMQ.CHARSET);
            } else {
                // En síncrono recibimos directamente la cadena
                json = socket.recvStr();
            }

            final byte[]   id = clientId;
            final String   rq = json;

            pool.execute(() -> {
                // Deserializar y procesar la solicitud
                Solicitud solicitud = gson.fromJson(rq, Solicitud.class);
                boolean ok = asignador.asignarAulas(solicitud);
                String respuesta = ok ? "Asignación exitosa" : "Sin aulas disponibles";

                // Persistir el resultado
                persistencia.guardar(ok ? "asignaciones" : "error", rq);

                // Responder según el modo
                if (mode.equals("async")) {
                    socket.send(id,    ZMQ.SNDMORE);
                    socket.send("",    ZMQ.SNDMORE);
                    socket.send(respuesta);
                } else {
                    socket.send(respuesta);
                }
            });
        }
    }
}

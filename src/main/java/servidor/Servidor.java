package servidor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.zeromq.ZMQ;

/**
 * Servidor Central asíncrono – usa ROUTER para atender DEALER de Facultades.
 */
public class Servidor {

    private static final int PUERTO = 5555;
    private static final int MAX_HILOS = 10;

    public static void main(String[] args) {
        System.out.println("[Servidor] Iniciando en modo asíncrono (ROUTER)...");
        ZMQ.Context context = ZMQ.context(1);

        // ROUTER recibe primero identidad, luego empty frame, luego cuerpo
        ZMQ.Socket socket = context.socket(ZMQ.ROUTER);
        socket.bind("tcp://*:" + PUERTO);

        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);
        Persistencia persistencia = new Persistencia();
        AsignadorAulas asignador   = new AsignadorAulas();

        while (!Thread.currentThread().isInterrupted()) {
            // 1) identidad del cliente
            byte[] clientId = socket.recv();
            // 2) frame vacío (separador)
            socket.recv();
            // 3) JSON de solicitud
            String solicitudJson = new String(socket.recv(), ZMQ.CHARSET);
            System.out.println("[Servidor] Llega de " 
                + new String(clientId, ZMQ.CHARSET));

            // 4) despacha en hilo, pasándole también clientId
            pool.execute(new ManejadorSolicitudes(
                clientId, solicitudJson, socket, asignador, persistencia
            ));
        }

        pool.shutdown();
        socket.close();
        context.term();
    }
}

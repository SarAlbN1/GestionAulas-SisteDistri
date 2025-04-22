package servidor;

import modelo.Solicitud;
import servidor.ManejadorSolicitudes;
import servidor.Persistencia;
import servidor.AsignadorAulas;

import org.zeromq.ZMQ;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor Central - Gestión de Aulas
 * Recibe solicitudes de facultades y lanza hilos para atenderlas concurrentemente.
 *
 * Autor: Juan Diego Rojas Vargas
 * Pontificia Universidad Javeriana - Sistemas Distribuidos
 */
public class Servidor {

    private static final int PUERTO = 5555; // Puerto de escucha del servidor
    private static final int MAX_HILOS = 10; // Número máximo de hilos concurrentes

    public static void main(String[] args) {
        System.out.println("[Servidor] Iniciando Servidor Central...");

        // Crear contexto y socket de ZMQ
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP); // reply pattern
        socket.bind("tcp://*:" + PUERTO);

        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);
        Persistencia persistencia = new Persistencia();
        AsignadorAulas asignador = new AsignadorAulas();

        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("[Servidor] Esperando solicitudes...");

            // Recibir datos
            String solicitudJson = socket.recvStr();

            // Lanzar un hilo por cada solicitud recibida
            ManejadorSolicitudes manejador = new ManejadorSolicitudes(
                solicitudJson, socket, asignador, persistencia
            );
            pool.execute(manejador);
        }

        // Cierre
        pool.shutdown();
        socket.close();
        context.term();
    }
}

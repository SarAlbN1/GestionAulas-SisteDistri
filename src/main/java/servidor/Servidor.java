package servidor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.ZMQ;

/**
 * Servidor Central – Gestión de Aulas
 * Recibe solicitudes de las facultades y las procesa concurrentemente
 * usando un pool de hilos.
 *
 * Autor:Sara Albarracin
 * Pontificia Universidad Javeriana – Sistemas Distribuidos
 */
public class Servidor {

    // Puerto en el que el servidor escucha peticiones REQ de las facultades
    private static final int PUERTO = 5555;

    // Tamaño máximo del pool de hilos para procesar solicitudes simultáneas
    private static final int MAX_HILOS = 10;

    public static void main(String[] args) {
        System.out.println("[Servidor] Iniciando Servidor Central...");

        // 1. Crear contexto ZeroMQ con un sólo hilo de I/O
        ZMQ.Context context = ZMQ.context(1);

        // 2. Crear socket REP para atender las solicitudes entrantes
        ZMQ.Socket socket = context.socket(ZMQ.REP);
        // 2.1. Asociar (bind) el socket a todas las interfaces en el PUERTO definido
        socket.bind("tcp://*:" + PUERTO);

        // 3. Crear un pool de hilos con tamaño fijo para manejar concurrencia
        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);

        // 4. Instancias compartidas de lógica de persistencia y asignación
        Persistencia persistencia = new Persistencia();
        AsignadorAulas asignador   = new AsignadorAulas();

        // 5. Bucle principal: recibir y despachar solicitudes indefinidamente
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("[Servidor] Esperando solicitudes...");

            // 5.1. Bloquea hasta recibir un JSON con la solicitud de la facultad
            String solicitudJson = socket.recvStr();

            // 5.2. Construir un manejador de la solicitud, inyectando dependencias
            ManejadorSolicitudes manejador = new ManejadorSolicitudes(
                solicitudJson,
                socket,          // socket REP para enviar la respuesta
                asignador,       // lógica de asignación de aulas
                persistencia     // lógica de registro en disco
            );

            // 5.3. Enviar la tarea al pool para ejecución en un hilo disponible
            pool.execute(manejador);
        }

        // 6. Apagar ordenadamente el pool y liberar recursos de ZeroMQ
        pool.shutdown();    // No acepta nuevas tareas y espera que terminen las activas
        socket.close();     // Cierra el socket de red
        context.term();     // Libera el contexto de ZeroMQ
    }
}

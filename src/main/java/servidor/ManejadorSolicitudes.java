package servidor;

import org.zeromq.ZMQ;

import com.google.gson.Gson;

import modelo.Solicitud;

/**
 * Hilo encargado de manejar cada solicitud recibida por el servidor.
 * Implementa Runnable para poder procesar solicitudes de forma concurrente.
 */
public class ManejadorSolicitudes implements Runnable {

    // JSON de la solicitud recibida, aún sin procesar
    private final String solicitudJson;

    // Socket ZeroMQ REP con el que se comunicará la respuesta
    private final ZMQ.Socket socket;

    // Componente responsable de asignar aulas según disponibilidad
    private final AsignadorAulas asignador;

    // Componente responsable de persistir registros de asignaciones o errores
    private final Persistencia persistencia;

    /**
     * Constructor que inicializa el manejador con la solicitud y dependencias.
     *
     * @param solicitudJson JSON recibido con los datos de la solicitud académica
     * @param socket        Socket ZeroMQ REP para enviar la respuesta
     * @param asignador     Lógica de asignación de aulas
     * @param persistencia  Lógica para guardar eventos en almacenamiento
     */
    public ManejadorSolicitudes(String solicitudJson,
                                ZMQ.Socket socket,
                                AsignadorAulas asignador,
                                Persistencia persistencia) {
        this.solicitudJson = solicitudJson;
        this.socket = socket;
        this.asignador = asignador;
        this.persistencia = persistencia;
    }

    /**
     * Método principal del hilo. Convierte el JSON en un objeto Solicitud,
     * intenta la asignación de aulas, registra el resultado y envía la respuesta.
     */
    @Override
    public void run() {
        // 1. Crear instancia de Gson para deserializar JSON → objeto Java
        Gson gson = new Gson();

        // 2. Convertir la cadena JSON recibida en un objeto Solicitud
        Solicitud solicitud = gson.fromJson(solicitudJson, Solicitud.class);
        System.out.println("[Servidor] Procesando solicitud de " + solicitud.getPrograma());

        // 3. Intentar asignar aulas mediante la lógica de AsignadorAulas
        boolean asignado = asignador.asignarAulas(solicitud);

        // 4. Preparar mensaje de respuesta según éxito o fracaso de la asignación
        String respuesta = asignado
            ? "Asignación exitosa"      // Si pudo asignar
            : "Sin aulas disponibles";  // Si no pudo asignar

        // 5. Persistir el evento: 
        //    - Si se asignó, guardar en la colección/tablas "asignaciones"
        //    - Si falló, guardar en "error"
        String claveRegistro = asignado ? "asignaciones" : "error";
        persistencia.guardar(claveRegistro, solicitudJson);

        // 6. Enviar la respuesta de vuelta al cliente a través del socket REP
        socket.send(respuesta);
    }
}

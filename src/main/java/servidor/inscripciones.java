package servidor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.ZMQ;

public class inscripciones {

    private static final int PUERTO_RECEPCION = 6000;
    private static final List<String> FACULTADES = java.util.Arrays.asList("Fisica", "Matematicas", "Quimica");

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);

        // Socket REP compartido para recibir desde Programas Académicos
        ZMQ.Socket recepcion = context.socket(ZMQ.REP);
        recepcion.bind("tcp://*:" + PUERTO_RECEPCION);
        System.out.println("[FacultadMain] 📡 Facultad escuchando solicitudes en puerto " + PUERTO_RECEPCION);

        ExecutorService pool = Executors.newFixedThreadPool(FACULTADES.size());

        for (String nombreFacultad : FACULTADES) {
            pool.submit(() -> {
                System.out.println("[Facultad-" + nombreFacultad + "] 🎓 Intentando inscripción...");
                Facultad.iniciar("localhost", nombreFacultad, recepcion, context);
            });
        }

        // Nota: no cerramos el context ni recepcion porque están compartidos entre hilos
    }
}

package facultades;

import org.zeromq.ZMQ;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase principal para ejecutar múltiples Facultades en el mismo puerto (6000).
 * Cada Facultad se ejecuta en un hilo distinto, usando el mismo socket REP compartido.
 */
public class FacultadLauncher {

    private static final int PUERTO = 6000;
    private static boolean yaLanzadas = false; // Variable para controlar lanzamiento único
    private static final Map<String, List<String>> FACULTADES = new HashMap<>();

    static {
        FACULTADES.put("Facultad de Ciencias Sociales", Arrays.asList(
            "Programa de Psicología", "Programa de Sociología", "Programa de Trabajo Social",
            "Programa de Antropología", "Programa de Comunicación"));

        FACULTADES.put("Facultad de Ciencias Naturales", Arrays.asList(
            "Programa de Biología", "Programa de Química", "Programa de Física",
            "Programa de Geología", "Programa de Ciencias Ambientales"));

        FACULTADES.put("Facultad de Ingeniería", Arrays.asList(
            "Programa de Ingeniería Civil", "Programa de Ingeniería Electrónica",
            "Programa de Ingeniería de Sistemas", "Programa de Ingeniería Mecánica",
            "Programa de Ingeniería Industrial"));

        FACULTADES.put("Facultad de Medicina", Arrays.asList(
            "Programa de Medicina General", "Programa de Enfermería", "Programa de Odontología",
            "Programa de Farmacia", "Programa de Terapia Física"));

        FACULTADES.put("Facultad de Derecho", Arrays.asList(
            "Programa de Derecho Penal", "Programa de Derecho Civil", "Programa de Derecho Internacional",
            "Programa de Derecho Laboral", "Programa de Derecho Constitucional"));

        FACULTADES.put("Facultad de Artes", Arrays.asList(
            "Programa de Bellas Artes", "Programa de Música", "Programa de Teatro",
            "Programa de Danza", "Programa de Diseño Gráfico"));

        FACULTADES.put("Facultad de Educación", Arrays.asList(
            "Programa de Educación Primaria", "Programa de Educación Secundaria",
            "Programa de Educación Especial", "Programa de Psicopedagogía",
            "Programa de Administración Educativa"));

        FACULTADES.put("Facultad de Ciencias Económicas", Arrays.asList(
            "Programa de Administración de Empresas", "Programa de Contabilidad",
            "Programa de Economía", "Programa de Mercadotecnia", "Programa de Finanzas"));

        FACULTADES.put("Facultad de Arquitectura", Arrays.asList(
            "Programa de Arquitectura", "Programa de Urbanismo", "Programa de Diseño de Interiores",
            "Programa de Paisajismo", "Programa de Restauración de Patrimonio"));

        FACULTADES.put("Facultad de Tecnología", Arrays.asList(
            "Programa de Desarrollo de Software", "Programa de Redes y Telecomunicaciones",
            "Programa de Ciberseguridad", "Programa de Inteligencia Artificial",
            "Programa de Big Data"));
    }

    public static void main(String[] args) throws InterruptedException {
        lanzarFacultades();
    }

    public static void lanzarFacultades() {
        if (yaLanzadas) return;
        yaLanzadas = true;

        new Thread(() -> {
            try {
                // Crear contexto ZMQ común
                ZMQ.Context context = ZMQ.context(1);
                ZMQ.Socket socketRecepcion = context.socket(ZMQ.REP);
                socketRecepcion.bind("tcp://*:" + PUERTO);

                ExecutorService executor = Executors.newFixedThreadPool(FACULTADES.size());
                for (Map.Entry<String, List<String>> entry : FACULTADES.entrySet()) {
                    String nombreFacultad = entry.getKey();
                    List<String> programas = entry.getValue();
                    executor.submit(new FacultadWorker(nombreFacultad, programas, socketRecepcion, context));
                }

                executor.shutdown();

            } catch (Exception e) {
                System.out.println("[Launcher] ❌ Error al lanzar facultades: " + e.getMessage());
            }
        }).start();
    }

    public static boolean existeFacultad(String nombre) {
        return FACULTADES.containsKey(nombre);
    }

    public static List<String> programasFacultad(String nombre) {
        return FACULTADES.getOrDefault(nombre, List.of());
    }
}
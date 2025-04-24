package servidor;

import modelo.Solicitud;

/**
 * AsignadorAulas gestiona la distribución de recursos educativos (salones y laboratorios), asegurando integridad en entornos concurrentes asincrónicos.
 */
public class AsignadorAulas {

    // Recursos totales iniciales del sistema
    private static final int TOTAL_SALONES = 380;
    private static final int TOTAL_LABORATORIOS = 60;
    private static final int LIMITE_AULAS_MOVILES = 40;

    // Recursos disponibles durante la ejecución
    private int salonesDisponibles = TOTAL_SALONES;
    private int laboratoriosDisponibles = TOTAL_LABORATORIOS;
    private int aulasMovilesUsadas = 0;

    public synchronized boolean asignarAulas(Solicitud solicitud) {
        int reqSalones = solicitud.getSalones();
        int reqLabs = solicitud.getLaboratorios();

        // 🟢 Caso 1: recursos físicos disponibles
        if (reqSalones <= salonesDisponibles && reqLabs <= laboratoriosDisponibles) {
            salonesDisponibles -= reqSalones;
            laboratoriosDisponibles -= reqLabs;
            return true;
        }

        // 🟡 Caso 2: se requieren aulas móviles para suplir laboratorios faltantes
        boolean puedeSuplirConMoviles = reqLabs > laboratoriosDisponibles &&
                                        aulasMovilesUsadas + reqLabs <= LIMITE_AULAS_MOVILES &&
                                        reqSalones <= salonesDisponibles;

        if (puedeSuplirConMoviles) {
            salonesDisponibles -= reqSalones;
            aulasMovilesUsadas += reqLabs;
            return true;
        }

        // 🔴 Caso 3: no es posible satisfacer la solicitud
        return false;
    }

    // Métodos para monitoreo/logging si deseas imprimir en consola o verificar en pruebas
    public synchronized int getSalonesDisponibles()       { return salonesDisponibles; }
    public synchronized int getLaboratoriosDisponibles()  { return laboratoriosDisponibles; }
    public synchronized int getAulasMovilesUsadas()       { return aulasMovilesUsadas; }
}

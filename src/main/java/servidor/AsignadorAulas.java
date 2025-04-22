package servidor;

import modelo.Constantes;
import modelo.Solicitud;

/**
 * Clase encargada de validar y asignar aulas de acuerdo a la disponibilidad.
 */
public class AsignadorAulas {

    // Número total de salones físicos disponibles (inicializado desde Constantes)
    private int salonesDisponibles = Constantes.TOTAL_SALONES;

    // Número total de laboratorios disponibles (inicializado desde Constantes)
    private int laboratoriosDisponibles = Constantes.TOTAL_LABORATORIOS;

    // Contador de laboratorios que serán suplidos con aulas móviles
    private int aulasMoviles = 0;

    /**
     * Intenta asignar los espacios solicitados (salones y laboratorios).
     * Este método está sincronizado para garantizar la consistencia en entornos multihilo.
     *
     * @param solicitud objeto que contiene la cantidad de salones y laboratorios requeridos
     * @return true si la asignación fue exitosa; false en caso contrario
     */
    public synchronized boolean asignarAulas(Solicitud solicitud) {
        // Caso 1: hay suficientes salones y laboratorios físicos disponibles
        if (solicitud.getSalones() <= salonesDisponibles &&
            solicitud.getLaboratorios() <= laboratoriosDisponibles) {

            // Reducir el conteo de espacios físicos restantes
            salonesDisponibles    -= solicitud.getSalones();
            laboratoriosDisponibles -= solicitud.getLaboratorios();

            return true;
        }
        // Caso 2: no hay laboratorios físicos suficientes, pero podemos usar aulas móviles
        else if (solicitud.getLaboratorios() > laboratoriosDisponibles &&
                 aulasMoviles + solicitud.getLaboratorios() <= Constantes.LIMITE_AULAS_MOVILES) {

            // Reducir sólo los salones físicos (asume que salones pueden suplir parte del requerimiento)
            salonesDisponibles    -= solicitud.getSalones();

            // Aumentar el número de aulas móviles utilizadas
            aulasMoviles          += solicitud.getLaboratorios();

            return true;
        }
        // Caso 3: no es posible satisfacer la solicitud ni con aulas móviles
        else {
            return false;
        }
    }
}

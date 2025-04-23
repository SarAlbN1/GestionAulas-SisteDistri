package servidor;

import modelo.Constantes;
import modelo.Solicitud;

//Clase encargada de validar y asignar aulas de acuerdo a la disponibilidad.

public class AsignadorAulas {

    // (inicializado desde Constantes)
    private int salonesDisponibles = Constantes.TOTAL_SALONES;
    private int laboratoriosDisponibles = Constantes.TOTAL_LABORATORIOS;

    // Contador laboratorios que serán suplidos con aulas móviles
    private int aulasMoviles = 0;

    /**
     * Intenta asignar los espacios solicitados (salones y laboratorios).
     * Este método está sincronizado para garantizar la consistencia en entornos
     * multihilo.
     *
     * @param solicitud objeto que contiene la cantidad de salones y laboratorios
     *                  requeridos
     * @return true si la asignación fue exitosa; false en caso contrario
     */
    public synchronized boolean asignarAulas(Solicitud solicitud) {
        // Caso 1: hay suficientes salones y laboratorios físicos disponibles
        if (solicitud.getSalones() <= salonesDisponibles &&
                solicitud.getLaboratorios() <= laboratoriosDisponibles) {

            // Reducir el conteo de espacios físicos restantes
            salonesDisponibles -= solicitud.getSalones();
            laboratoriosDisponibles -= solicitud.getLaboratorios();

            return true;
        }
        // Caso 2: no hay laboratorios físicos suficientes, se usan aulas móviles
        else if (solicitud.getLaboratorios() > laboratoriosDisponibles && aulasMoviles + solicitud.getLaboratorios() <= Constantes.LIMITE_AULAS_MOVILES) {
            salonesDisponibles -= solicitud.getSalones();  // Reducir sólo los salones físicos
            aulasMoviles += solicitud.getLaboratorios(); // Aumentar el número de aulas móviles utilizadas
            return true;
        }
        // Caso 3: no es posible satisfacer la solicitud ni con aulas móviles
        else {
            return false;
        }
    }
}

package servidor;

import modelo.Constantes;
import modelo.Solicitud;

/**
 * Clase encargada de validar y asignar aulas de acuerdo a la disponibilidad.
 */
public class AsignadorAulas {
    private int salonesDisponibles = Constantes.TOTAL_SALONES;
    private int laboratoriosDisponibles = Constantes.TOTAL_LABORATORIOS;
    private int aulasMoviles = 0;

    public synchronized boolean asignarAulas(Solicitud solicitud) {
        if (solicitud.getSalones() <= salonesDisponibles &&
            solicitud.getLaboratorios() <= laboratoriosDisponibles) {
            salonesDisponibles -= solicitud.getSalones();
            laboratoriosDisponibles -= solicitud.getLaboratorios();
            return true;
        } else if (solicitud.getLaboratorios() > laboratoriosDisponibles &&
                   aulasMoviles + solicitud.getLaboratorios() <= Constantes.LIMITE_AULAS_MOVILES) {
            salonesDisponibles -= solicitud.getSalones();
            aulasMoviles += solicitud.getLaboratorios(); // adaptar salones
            return true;
        } else {
            return false;
        }
    }
}

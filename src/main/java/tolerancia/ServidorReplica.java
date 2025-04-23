package tolerancia;

import servidor.Servidor;

/**
 * Servidor de respaldo que actúa igual que el principal en caso de falla.
 */
public class ServidorReplica {
    public static void main(String[] args) {
        System.out.println("[Servidor Réplica] Activado por falla del servidor principal");
        Servidor.main(args); // Reutiliza la lógica del servidor original
    }
}

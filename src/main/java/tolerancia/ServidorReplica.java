package tolerancia;

import servidor.Servidor;

/**
 * Servidor de respaldo que actúa igual que el principal en caso de falla.
 * Reutiliza la lógica del Servidor principal.
 */
public class ServidorReplica {
    public static void main(String[] args) {
        System.out.println("[Servidor Réplica] 🔁 Activado por falla del servidor principal");

        if (args.length < 2) {
            System.out.println("[Servidor Réplica] ⚠️ No se especificaron IP y puerto del servidor principal.");
            System.out.println("Uso: java tolerancia.ServidorReplica <ipServidor> <puertoServidor>");
        } else {
            System.out.println("[Servidor Réplica] 🧭 Escuchando mientras el principal (" + args[0] + ":" + args[1] + ") está caído.");
        }

        Servidor.main(args);
    }
}

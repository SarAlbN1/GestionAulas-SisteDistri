package tolerancia;

import org.zeromq.ZMQ;

/**
 * HealthChecker que monitorea periódicamente al servidor principal (modo async).
 * Si no obtiene respuesta, activa automáticamente el servidor réplica.
 */
public class HealthChecker {

    private static final String IP_SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5555;
    private static final int INTERVALO_MS = 5000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[HealthChecker][async] Iniciando monitoreo al servidor en modo asíncrono...");

        while (true) {
            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket socket = context.socket(ZMQ.DEALER);
            socket.setIdentity("HEALTH".getBytes(ZMQ.CHARSET));

            try {
                socket.connect("tcp://" + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);
                socket.send("ping");

                // Esperamos respuesta con timeout
                boolean respondio = socket.poll(INTERVALO_MS / 2, ZMQ.Poller.POLLIN);

                if (!respondio || socket.recvStr() == null) {
                    System.out.println("[HealthChecker] ❌ El servidor no respondió. Activando réplica...");
                    Runtime.getRuntime().exec("java tolerancia.ServidorReplica");
                    break;
                }

                System.out.println("[HealthChecker] ✅ Servidor activo.");
            } catch (Exception e) {
                System.out.println("[HealthChecker] ⚠️ Error de conexión: " + e.getMessage());
            } finally {
                socket.close();
                context.term();
            }

            Thread.sleep(INTERVALO_MS);
        }
    }
}

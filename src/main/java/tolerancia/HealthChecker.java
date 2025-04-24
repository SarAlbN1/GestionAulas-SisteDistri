package tolerancia;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

/**
 * HealthChecker que monitorea periódicamente al servidor principal (modo async).
 * Si no obtiene respuesta, activa automáticamente el servidor réplica.
 */
public class HealthChecker {

    private static final String IP_SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5555;
    private static final int INTERVALO_MS = 10000;  // Tiempo de espera aumentado a 10 segundos

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[HealthChecker][async] Iniciando monitoreo al servidor en modo asíncrono...");

        while (true) {
            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket socket = context.socket(ZMQ.DEALER);
            socket.setIdentity("HEALTH".getBytes(ZMQ.CHARSET));  // Asignamos un clientId

            try {
                socket.connect("tcp://" + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);

                // Enviar health-check con el clientId y frame vacío
                socket.send("", ZMQ.SNDMORE);  // Primer frame vacío
                socket.send("health-check");  // Segundo frame con el mensaje

                // Crear un poller para manejar el timeout
                Poller poller = context.poller(1);  // Solo un socket
                poller.register(socket, Poller.POLLIN);

                // Verificar si la respuesta llegó antes del timeout
                int rc = poller.poll(INTERVALO_MS);  // Espera hasta INTERVALO_MS milisegundos

                if (rc == 0) {
                    // No se recibió respuesta en el tiempo establecido
                    System.out.println("[HealthChecker] ❌ El servidor no respondió. Activando réplica...");
                    Runtime.getRuntime().exec("java tolerancia.ServidorReplica");
                    break;
                }

                // Si respondieron, simplemente verificamos si recibimos algo
                String respuesta = socket.recvStr();
                if (respuesta != null) {
                    System.out.println("[HealthChecker] ✅ Servidor activo. Respuesta recibida: " + respuesta);
                } else {
                    System.out.println("[HealthChecker] ❌ El servidor no respondió correctamente.");
                }

            } catch (Exception e) {
                System.out.println("[HealthChecker] ⚠️ Error de conexión: " + e.getMessage());
            } finally {
                socket.close();
                context.term();
            }

            // Esperar antes de realizar el siguiente intento
            Thread.sleep(INTERVALO_MS);
        }
    }
}

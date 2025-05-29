package tolerancia;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZPoller;
import org.zeromq.ZContext;

public class HealthChecker {

    private static final String IP_SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5555;
    private static final int INTERVALO_MS = 10000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[HealthChecker][async] Iniciando monitoreo al servidor...");

        while (true) {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
                socket.setIdentity("HEALTH".getBytes(ZMQ.CHARSET));
                socket.connect("tcp://" + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);

                socket.send("", ZMQ.SNDMORE);
                socket.send("health-check");

                ZPoller poller = new ZPoller(context);
                poller.register(socket, ZPoller.IN);

                if (poller.poll(INTERVALO_MS) > 0) {
                    String respuesta = socket.recvStr();
                    System.out.println("[HealthChecker] ✅ Respuesta recibida: " + respuesta);
                } else {
                    System.out.println("[HealthChecker] ❌ Sin respuesta. Activando réplica...");
                    Runtime.getRuntime().exec("java tolerancia.ServidorReplica");
                    break;
                }
            } catch (Exception e) {
                System.out.println("[HealthChecker] ⚠️ Error: " + e.getMessage());
            }
            Thread.sleep(INTERVALO_MS);
        }
    }
}
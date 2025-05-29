package tolerancia;

import org.zeromq.*;

public class HealthChecker {

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.out.println("Uso: java HealthChecker <ipServidor> <puertoServidor>");
            return;
        }

        final String IP_SERVIDOR = args[0];
        final int PUERTO_SERVIDOR = Integer.parseInt(args[1]);
        final int INTERVALO_MS = 10000;

        System.out.println("[HealthChecker][async] Iniciando monitoreo al servidor en " + IP_SERVIDOR + ":" + PUERTO_SERVIDOR + "...");

        while (true) {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
                socket.setIdentity("HEALTH".getBytes(ZMQ.CHARSET));
                socket.connect("tcp://" + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);

                socket.sendMore("");
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

package tolerancia;

import org.zeromq.ZMQ;

/**
 * HealthChecker que monitorea al servidor principal y activa el backup si detecta una caída.
 */
public class HealthChecker {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[HealthChecker] Iniciando monitoreo...");

        while (true) {
            try {
                ZMQ.Context context = ZMQ.context(1);
                ZMQ.Socket socket = context.socket(ZMQ.REQ);
                socket.connect("tcp://localhost:5555");
                socket.send("ping");

                String respuesta = socket.recvStr(ZMQ.DONTWAIT);
                if (respuesta == null) {
                    System.out.println("[HealthChecker] Sin respuesta del servidor. Activando réplica...");
                    Runtime.getRuntime().exec("java tolerancia.ServidorReplica");
                    break;
                }

                socket.close();
                context.term();
                Thread.sleep(5000); // cada 5 segundos
            } catch (Exception e) {
                System.out.println("[HealthChecker] Error al verificar servidor: " + e.getMessage());
            }
        }
    }
}

package tolerancia;

import org.zeromq.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class HealthChecker {

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.out.println("Uso: java HealthChecker <ipServidor> <puertoServidor>");
            return;
        }

        final String IP_SERVIDOR = args[0];
        final int PUERTO_SERVIDOR = Integer.parseInt(args[1]);
        final int INTERVALO_MS = 10000;

        Process replicaProcess = null;
        boolean replicaActiva = false;

        System.out.println("[HealthChecker] 🔍 Iniciando monitoreo a " + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);

        while (true) {
            boolean servidorActivo = false;

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
                    System.out.println("[HealthChecker] ✅ Respuesta del servidor principal: " + respuesta);
                    servidorActivo = true;
                } else {
                    System.out.println("[HealthChecker] ❌ Sin respuesta del servidor principal");
                }
            } catch (Exception e) {
                System.out.println("[HealthChecker] ⚠️ Error al contactar el servidor: " + e.getMessage());
            }

            if (!servidorActivo && !replicaActiva) {
                try {
                    System.out.println("[HealthChecker] 🚨 Activando Servidor Réplica...");
                    
                    ProcessBuilder builder = new ProcessBuilder(
                        "cmd", "/c", "runWin\\resilience\\run_backup.bat", IP_SERVIDOR, String.valueOf(PUERTO_SERVIDOR)
                    );
                    builder.directory(new File(System.getProperty("user.dir"))); // Asegura ejecución desde raíz del proyecto
                    replicaProcess = builder.start();
                    replicaActiva = true;

                    BufferedReader reader = new BufferedReader(new InputStreamReader(replicaProcess.getInputStream()));
                    new Thread(() -> reader.lines().forEach(line -> System.out.println("[ServidorReplica] " + line))).start();

                } catch (Exception e) {
                    System.out.println("[HealthChecker] ❌ Error al iniciar réplica: " + e.getMessage());
                }
            }

            if (servidorActivo && replicaActiva) {
                System.out.println("[HealthChecker] 🟢 Servidor principal activo. Deteniendo réplica...");
                if (replicaProcess != null && replicaProcess.isAlive()) {
                    replicaProcess.destroy();
                    System.out.println("[HealthChecker] 🛑 Réplica detenida.");
                }
                replicaActiva = false;
            }

            Thread.sleep(INTERVALO_MS);
        }
    }
}


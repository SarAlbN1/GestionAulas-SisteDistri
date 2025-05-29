package tolerancia;

import org.zeromq.*;

import java.io.*;
import java.util.*;

public class HealthChecker {

    private static final int PUERTO_PUB = 7000;
    private static final int INTERVALO_MS = 10000;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.out.println("Uso: java HealthChecker <ipServidor> <puertoServidor>");
            return;
        }

        final String IP_SERVIDOR = args[0];
        final int PUERTO_SERVIDOR = Integer.parseInt(args[1]);

        Process replicaProcess = null;
        boolean replicaActiva = false;

        try (ZContext context = new ZContext()) {
            ZMQ.Socket pub = context.createSocket(SocketType.PUB);
            pub.bind("tcp://*:" + PUERTO_PUB);
            System.out.println("[HealthChecker] 📡 Canal PUB habilitado en puerto " + PUERTO_PUB);

            System.out.println("[HealthChecker] 🔍 Monitoreando servidor en " + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);

            while (true) {
                boolean servidorActivo = false;

                try (ZMQ.Socket socket = context.createSocket(SocketType.DEALER)) {
                    socket.setIdentity("HEALTH".getBytes(ZMQ.CHARSET));
                    socket.connect("tcp://" + IP_SERVIDOR + ":" + PUERTO_SERVIDOR);

                    socket.sendMore("");
                    socket.send("health-check");

                    ZPoller poller = new ZPoller(context);
                    poller.register(socket, ZPoller.IN);

                    if (poller.poll(INTERVALO_MS) > 0) {
                        socket.recvStr(); // descartar frame vacío
                        String respuesta = socket.recvStr();
                        System.out.println("[HealthChecker] ✅ Respuesta recibida: " + respuesta);
                        servidorActivo = true;
                    } else {
                        System.out.println("[HealthChecker] ❌ Sin respuesta del servidor principal.");
                    }

                } catch (Exception e) {
                    System.out.println("[HealthChecker] ⚠️ Error de conexión: " + e.getMessage());
                }

                if (!servidorActivo && !replicaActiva) {
                    try {
                        System.out.println("[HealthChecker] 🚨 Iniciando servidor de respaldo local...");

                        List<String> comando = Arrays.asList(
                            "mvn", "exec:java",
                            "-Dexec.mainClass=servidor.Servidor",
                            "-Dexec.args=backup"
                        );

                        ProcessBuilder builder = new ProcessBuilder(comando);
                        builder.redirectErrorStream(true);
                        builder.directory(new File(System.getProperty("user.dir")));

                        replicaProcess = builder.start();
                        replicaActiva = true;

                        // Notificar redirección a facultades
                        pub.send("REDIRIGIR");

                        // Imprimir logs del servidor de respaldo
                        BufferedReader reader = new BufferedReader(new InputStreamReader(replicaProcess.getInputStream()));
                        new Thread(() -> reader.lines().forEach(line -> System.out.println("[ServidorReplica] " + line))).start();

                    } catch (Exception e) {
                        System.out.println("[HealthChecker] ❌ Error al iniciar réplica local: " + e.getMessage());
                    }
                }

                if (servidorActivo && replicaActiva) {
                    System.out.println("[HealthChecker] 🟢 Servidor principal volvió. Terminando réplica.");
                    if (replicaProcess != null && replicaProcess.isAlive()) {
                        replicaProcess.destroy();
                        System.out.println("[HealthChecker] 🛑 Réplica detenida.");
                    }
                    replicaActiva = false;

                    // Notificar a las facultades que deben volver al servidor principal
                    pub.send("VOLVER");
                }

                Thread.sleep(INTERVALO_MS);
            }
        }
    }
}

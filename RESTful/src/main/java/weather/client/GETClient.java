package weather.client;

import weather.lamport.LamportClockImpl;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class GETClient {
    private static final Logger logger = Logger.getLogger(GETClient.class.getName());

    public static void main(String[] args) {
        String host = "localhost";
        int port = 4567;
        int numberOfClients = 10;

        // Parse command-line arguments
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                logger.severe("Invalid port number provided. Using default port: 4567");
            }
        }
        if (args.length >= 3) {
            try {
                numberOfClients = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                logger.severe("Invalid number of clients provided. Using default number of clients: 5");
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfClients);
        LamportClockImpl clock = new LamportClockImpl();

        for (int i = 0; i < numberOfClients; i++) {
            ClientConfig clientConfig = new ClientConfig(host, port, null);

            GETHandler getHandler = new GETHandler(clientConfig);
            Future<String> future = executorService.submit(() -> {
                try {
                    Thread.sleep(500);  // Delay the task execution
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return getHandler.requestData(); // Proceed the actual task
            });
            try {
                String response = future.get();
                int logicTime = clock.incrementAndGet();
                System.out.println("Current Logic Time: " + logicTime);
                System.out.println("Current System Time:" + System.nanoTime());
                System.out.println(response);
                Thread.sleep(500);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Gracefully shut down the executor after tasks are done
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                executorService.shutdownNow(); // Force shutdown if tasks are still running after timeout
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}

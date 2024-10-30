package weather.Content;

import weather.lamport.LamportClockImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ContentServer {
    private static final Logger logger = Logger.getLogger(ContentServer.class.getName());

    public static void main(String[] args) {
        // Default values for testing in the IDE
        String serverHostname = "localhost";
        int serverPort = 4567;
        String filePath = "Data/weatherTest.txt";  // Default file path
        int numberOfContent = 5;  // Default number of servers

        // Parse command-line arguments
        if (args.length >= 1) {
            serverHostname = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                logger.severe("Invalid port number provided. Please provide a valid integer.");
                return;
            }
        }
        if (args.length >= 3) {
            filePath = args[2];
        }
        if (args.length >= 4) {
            try {
                // Parse the number of PUT request
                numberOfContent = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                logger.severe("Invalid number of servers provided. Using default value: 5.");
            }
        }

        // Create a fixed thread pool using the number of content servers
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfContent);
        // Server Logic Time initialization
        LamportClockImpl lamportClock = new LamportClockImpl();

        // Launch multiple content servers
        for (int i = 1; i <= numberOfContent; i++) {
            ContentServerConfig config = new ContentServerConfig(serverHostname, serverPort, filePath);
            ContentServerActivator activator = new ContentServerActivator(config, lamportClock);

            /* Core Step: Submit each content server to the executor service */
            executorService.submit(activator::run);
            System.out.println("Started Content Server " + i + " with file: " + filePath);

            // Introduce a small delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

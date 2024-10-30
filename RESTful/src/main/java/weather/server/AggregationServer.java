package weather.server;

import java.util.logging.Logger;

public class AggregationServer {
    private static final Logger logger = Logger.getLogger(AggregationServer.class.getName());

    public static void main(String[] args) {
        String host = "localhost"; // Default host
        int port = 4567; // Default port
        int backlog = 10; // Default backlog

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
                backlog = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid backlog number provided. Using default backlog: 10");
            }
        }

        ServerConfig config = new ServerConfig(host, port, backlog);
        ServerActivator serverActivator = new ServerActivator(config);
        /* Core server execution */
        serverActivator.run();
    }
}

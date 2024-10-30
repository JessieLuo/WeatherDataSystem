package weather.server;

import weather.lamport.LamportClockImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerActivator {
    private static final Logger logger = Logger.getLogger(ServerActivator.class.getName());
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private final ServerConfig config;
    private final LamportClockImpl clock;

    public ServerActivator(ServerConfig config) {
        this.config = config;
        this.clock = new LamportClockImpl();
    }

    public void run() {
        setUpShutdownHook();

        try (ServerSocket serverSocket = initializeServer()) {
            handleClientConnections(serverSocket); // Process RESTFul API
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server encountered an issue and needs to shut down.", e);
        } finally {
            logger.info("Server has stopped.");
        }
    }

    private void setUpShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
            logger.info("Shutdown signal received. Stopping server...");
        }));
    }

    private ServerSocket initializeServer() throws IOException {
        InetAddress address = InetAddress.getByName(config.host());
        ServerSocket serverSocket = new ServerSocket(config.port(), config.backlog(), address);
        logger.info("Server started on " + config.host() + ":" + config.port());
        return serverSocket;
    }

    private void handleClientConnections(ServerSocket serverSocket) {
        while (running.get()) {
            try {
                // Listening income connection request
                Socket clientSocket = serverSocket.accept(); // a blocking call that waits until a client tries to connect to the server
                logger.info("Accepted client connection from " + clientSocket.getInetAddress().getHostAddress());
                /* Core Step: Handling the connected client requests */
                new Thread(new RequestHandler(clientSocket, clock)).start();
            } catch (IOException e) {
                if (running.get()) {
                    logger.log(Level.WARNING, "Failed to accept connection.", e);
                } else {
                    logger.info("Server is shutting down, no more connections will be accepted.");
                }
            }
        }
    }
}

package weather.Content;

import weather.lamport.LamportClockImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentServerActivator {
    private static final Logger logger = Logger.getLogger(ContentServerActivator.class.getName());
    private final ContentServerConfig config;
    private final LamportClockImpl clock;

    public ContentServerActivator(ContentServerConfig config, LamportClockImpl clock) {
        this.config = config;
        this.clock = clock;
    }

    public void run() {
        try (Socket socket = new Socket(config.getServerHostname(), config.getServerPort())) {
            ContentReaderService readerService = new ContentReaderService(config.getFilePath());
            ContentSenderService senderService = new ContentSenderService(socket, clock);
            ContentServerService serverService = new ContentServerService(readerService, senderService);

            Thread sendThread = new Thread(serverService::processAndSendFile);
            sendThread.start();
            sendThread.join();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to connect to the server or read/write data.", e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread was interrupted.", e);
            Thread.currentThread().interrupt();  // Restore the interrupted status
        }
    }
}

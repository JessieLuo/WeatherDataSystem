package weather.Content;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentServerService {
    private static final Logger logger = Logger.getLogger(ContentServerService.class.getName());

    private final ContentReaderService readerService;
    private final ContentSenderService senderService;

    public ContentServerService(ContentReaderService readerService, ContentSenderService senderService) {
        this.readerService = readerService;
        this.senderService = senderService;
    }

    public void processAndSendFile() {
        try {
            String data = readerService.readFile();
            senderService.sendJsonToServer(data);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing and sending file.", e);
            throw new RuntimeException("Error processing and sending file.", e);
        }
    }
}

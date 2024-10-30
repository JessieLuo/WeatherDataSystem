package weather.Content;

import weather.lamport.LamportClock;
import weather.lamport.LamportClockImpl;
import weather.utils.JSONParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Send the received data to aggregation server
 * */
public class ContentSenderService {
    private static final Logger logger = Logger.getLogger(ContentSenderService.class.getName());
    private final Socket socket;
    private final LamportClock clock;

    public ContentSenderService(Socket socket, LamportClockImpl clock) {
        this.socket = socket;
        this.clock = clock;
    }

    public void sendJsonToServer(String data) {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            int logicTime = clock.incrementAndGet();
            // Add timestamp to the data
            long systemTime = System.nanoTime();
            Map<String, Object> extraData = Map.of("SystemTimestamp", systemTime, "LogicTimestamp", logicTime);
            data = JSONParser.formatDataAsJson(data, extraData);

            /* Core Step: write the json data to server by socket */
            transitContent(data, out);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send data to the server.", e);
            throw new RuntimeException("Failed to send data to the server. Please check network connection.", e);
        } finally {
            closeSocket();
        }
    }

    private void transitContent(String data, BufferedWriter out) throws IOException {
        // Calculate the content length of the JSON data
        int contentLength = data.getBytes().length;

        // Write the PUT request line
        out.write("PUT /weather HTTP/1.1\r\n");

        // Write the headers
        out.write("Content-Type: application/json\r\n");
        out.write("Content-Length: " + contentLength + "\r\n");

        // Write the blank line to indicate the end of headers
        out.write("\r\n");
        out.flush();  // Flush the stream to ensure headers are sent separately

        // Write the body content
        out.write(data);
        out.flush();

        // Debug Track
        System.out.println("Data sent to server:\n" + data);
    }

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Socket closed successfully.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to close the socket.", e);
        }
    }
}

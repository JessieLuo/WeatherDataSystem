package weather.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GETHandler {
    private static final Logger logger = Logger.getLogger(GETHandler.class.getName());
    private final ClientConfig clientConfig;

    protected GETHandler(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public String requestData() {
        StringBuilder getData = new StringBuilder();

        try (
                Socket socket = (clientConfig.mockSocket() != null) ? clientConfig.mockSocket() : new Socket(clientConfig.serverHostname(), clientConfig.serverPort());
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            logger.log(Level.INFO, "Connected to server: " + clientConfig.serverHostname() + ":" + clientConfig.serverPort());

            // Send the GET request
            out.write("GET /weather\r\n");
            out.flush();
            logger.log(Level.INFO, "Sent GET request to server");

            // Read the HTTP status line
            String statusLine = in.readLine();
            logger.log(Level.INFO, "Received status line: " + statusLine);

            // Check if the status is 204 No Content
            if (statusLine.contains("204")) {
                logger.log(Level.WARNING, "Server returned 204 No Content");
                return "No content available from the server.";
            }

            // Read the rest of the response
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                getData.append(responseLine).append("\n");
            }

            logger.log(Level.INFO, "Received data from server");

        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unknown host: " + clientConfig.serverHostname(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error occurred when communicating with the server", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred", e);
        }

        // Check if data is empty or only contains an empty JSON object
        if (getData.isEmpty() || getData.toString().trim().equals("{}")) {
            logger.log(Level.WARNING, "No data received from the server");
            return "No data received from the server.";
        }

        return getData.toString();
    }
}

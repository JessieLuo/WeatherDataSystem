package weather.server;

import weather.data.WeatherDataStore;
import weather.lamport.LamportClockImpl;
import weather.utils.JSONParser;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Help Aggregation Server handle HTTP requests
 */
public class RequestHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private static int currentIndex = 0;
    private final Socket clientSocket;
    private final WeatherDataStore database;
    private final LamportClockImpl serverClock;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        database = new WeatherDataStore();
        serverClock = new LamportClockImpl();
    }

    public RequestHandler(Socket clientSocket, LamportClockImpl serverClock) {
        this.clientSocket = clientSocket;
        this.serverClock = serverClock;
        this.database = new WeatherDataStore();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String request = in.readLine();
            if (request == null) {
                logger.log(Level.SEVERE, "Received null request, possibly due to client disconnection.");
                return;
            }
            logger.info("Received request: " + request);

            /* Only design PUT & GET; Add POST or other HTTP request in future */
            if (request.startsWith("PUT")) {
                // From `in` read Header, use `out` return result
                handlePutRequest(in, out);
            } else if (request.startsWith("GET")) {
                handleGetRequest(out);
            } else {
                handleUnsupportedMethod(out, request);
            }
            out.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException occurred while processing client request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to close client socket", e);
            }
        }
    }

    private void handlePutRequest(BufferedReader in, BufferedWriter out) throws IOException {
        StringBuilder headers = new StringBuilder(); // TODO: Unknown how to process the header now
        StringBuilder body = new StringBuilder();
        String line;
        boolean isBody = false;

        // Separate headers and body
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                isBody = true; // Blank line indicates start of the body
                continue;
            }
            if (isBody) {
                body.append(line).append("\n"); // Append to body after the blank line
            } else {
                headers.append(line).append("\n"); // Collect headers before the blank line
            }
        }

        if (body.isEmpty()) {
            logger.log(Level.WARNING, "Received PUT request with no body content.");
            out.write("HTTP/1.1 400 Bad Request\r\n\r\n");
            return;
        }

        // Log the raw headers and body to debug
        logger.info("Received headers: " + headers);
        logger.info("Received body: " + body.toString().trim());

        /* Store the JSON body data in file system */
        database.saveWeatherData(body.toString().trim());
        logger.info("Stored weather data:\n" + body.toString().trim()); // Debug track

        out.write("HTTP/1.1 200 OK\r\n\r\n");
        out.flush();
        logger.info("Response sent: 200 OK");
    }

    private void handleGetRequest(BufferedWriter out) throws IOException {
        logger.info("Handling GET request. Sending stored weather data.");

        // Retrieve data using the WeatherDataStore
        String weatherData = database.readWeatherData();

        if (weatherData == null || weatherData.isEmpty() || weatherData.trim().equals("{}")) {
            out.write("HTTP/1.1 204 No Content\r\n\r\n");
            logger.info("Response sent: 204 No Content");
            return;
        }

        int logicTime = serverClock.incrementAndGet();
        String extractedJsonObject = JSONParser.extractJsonObjectByIndex(weatherData, currentIndex);

        // Check if extracted JSON object is null or empty
        if (extractedJsonObject == null) {
            out.write("HTTP/1.1 204 No Content\r\n\r\n");
            logger.info("Response sent: 204 No Content");
            return;
        }

        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Content-Type: application/json\r\n\r\n");
        out.write(extractedJsonObject);
        out.flush();

        logger.info("Response sent: 200 OK with weather data");
        System.out.println("Current Logic Time: " + logicTime);

        // Update the index for the next request
        currentIndex++;
        System.out.println("Current index: " + currentIndex);
        int jsonArrayLength = JSONParser.getJsonArrayLength(weatherData);
        if (jsonArrayLength != -1 && currentIndex >= jsonArrayLength) {
            currentIndex = 0;  // Reset the index if we have sent all objects
        }
    }

    private void handleUnsupportedMethod(BufferedWriter out, String request) throws IOException {
        logger.warning("Unsupported request method: " + request);

        out.write("HTTP/1.1 405 Method Not Allowed\r\n\r\n");
        out.flush();

        logger.info("Response sent: 405 Method Not Allowed");
    }
}

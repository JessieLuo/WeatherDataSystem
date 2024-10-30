package weather.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import weather.data.WeatherDataStore;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Mock the client-server interaction to avoid using actual sockets.
 */
class AggregationServerTest {
    private Socket mockSocket;
    private StringWriter outputWriter;
    private RequestHandler requestHandler;
    private WeatherDataStore database;

    @BeforeEach
    void setUp() {
        // Mock the socket and streams
        mockSocket = mock(Socket.class);
        outputWriter = new StringWriter();  // This will capture the output

        // Initialize the database
        database = new WeatherDataStore();

        // Initialize the handler with the mocked socket
        requestHandler = new RequestHandler(mockSocket);
    }

    @AfterEach
    void tearDown() {
        database.clearWeatherData();
    }

    @Test
    void testHandlePutRequest() throws IOException {
        // Simulate a client sending a PUT request with data
        String putRequest = "PUT /weather HTTP/1.1\r\nContent-Length: 43\r\n\r\n{ \"id\": \"IDS60901\", \"name\": \"Test Location\" }";
        BufferedReader putIn = new BufferedReader(new StringReader(putRequest));
        when(mockSocket.getInputStream()).thenReturn(new ReaderInputStream(putIn));
        when(mockSocket.getOutputStream()).thenReturn(new WriterOutputStream(outputWriter));

        // Run the handler logic to process the PUT request
        requestHandler.run();

        // Ensure that output was written
        String actualOutput = outputWriter.toString();
        assertFalse(actualOutput.isEmpty(), "The output should not be empty");

        // Validate the response header
        String expectedResponse = "HTTP/1.1 200 OK\r\n\r\n";
        assertEquals(expectedResponse, actualOutput, "The server response should be '200 OK'");

        // Validate that the data was stored correctly in the database
        String actualData = database.readWeatherData().trim();
        String expectedData = "{ \"id\": \"IDS60901\", \"name\": \"Test Location\" }";
        System.out.println("\nActual Content\n" + actualData + "\nExpected Content\n" + expectedData);
    }

    @Test
    void testHandleGetRequest() throws IOException {
        // Simulate the PUT request to store data in the server
        BufferedReader putIn = new BufferedReader(new StringReader("""
                PUT /weather HTTP/1.1\r
                Content-Length: 43\r
                \r
                { "id": "IDS60901", "name": "Test Location" }
                """));
        when(mockSocket.getInputStream()).thenReturn(new ReaderInputStream(putIn));
        when(mockSocket.getOutputStream()).thenReturn(new WriterOutputStream(outputWriter));
        requestHandler.run();

        // Simulate the GET request to retrieve the stored data
        BufferedReader getIn = new BufferedReader(new StringReader("GET /weather HTTP/1.1\r\n\r\n"));
        when(mockSocket.getInputStream()).thenReturn(new ReaderInputStream(getIn));
        outputWriter.getBuffer().setLength(0); // Clear the outputWriter buffer for new request

        // Run the handler logic for GET request
        requestHandler.run();

        // Ensure that output was written
        String actualOutput = outputWriter.toString();
        assertFalse(actualOutput.isEmpty(), "The output should not be empty");

        // Debugging output for analysis
        System.out.println("Actual Output:\n" + actualOutput);

        // Normalize expected and actual output
        String expectedOutputStart = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n";
        String expectedData = "{ \"id\": \"IDS60901\", \"name\": \"Test Location\" }";

        // Check if the response starts with the expected headers
        assertTrue(actualOutput.startsWith(expectedOutputStart.trim()), "The response should start with the expected HTTP header");

        // Extract the body of the response and normalize it
        String responseBody = actualOutput.substring(expectedOutputStart.length()).trim();

        // Validate that the response contains the expected weather data
        System.out.println("\n" + "Respond body content\n" + responseBody + "\n" + "Expected Content\n" + expectedData.trim());
    }

    // Helper methods to convert between InputStream/OutputStream and Reader/Writer
    private static class WriterOutputStream extends OutputStream {
        private final Writer writer;

        public WriterOutputStream(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void write(int b) throws IOException {
            writer.write(b);
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            writer.write(new String(b, off, len));
        }
    }

    private static class ReaderInputStream extends InputStream {
        private final Reader reader;
        private final char[] charBuffer = new char[1];

        public ReaderInputStream(Reader reader) {
            this.reader = reader;
        }

        @Override
        public int read() throws IOException {
            int n = reader.read(charBuffer, 0, 1);
            return (n == -1) ? -1 : (int) charBuffer[0];
        }
    }
}

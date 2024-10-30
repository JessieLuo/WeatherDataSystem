package weather.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Never Use the class.
 * It just retained for potential future usage.
 */
@Deprecated
class WeatherDatabaseTest {
    private WeatherDatabase weatherDatabase;

    @BeforeEach
    void setUp() {
        createTableIfNotExists();
        weatherDatabase = new WeatherDatabase();
        weatherDatabase.clearWeatherData(); // Clear the database before each test to ensure a clean state
    }

    @Test
    void testWeatherDataSaving() {
        String testData = "{\"temperature\": 22, \"humidity\": 60}";

        // Save data to the database
        weatherDatabase.saveWeatherData(testData);

        // Check if data was saved correctly by querying the database directly
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/weatherdb", "psqluser", "98psql");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT json_data FROM weather_data ORDER BY id DESC LIMIT 1")) {

            assertTrue(rs.next(), "No data was returned by the query.");
            String retrievedData = rs.getString("json_data");
            assertEquals(testData, retrievedData, "The data retrieved from the database does not match the saved data.");
        } catch (SQLException e) {
            fail("Database connection failed during test: " + e.getMessage());
        }
    }

    @Test
    void testWeatherDataReading() {
        String expectedData = "{\"temperature\": 25, \"humidity\": 50}";

        // Insert known test data into the database
        insertTestData(expectedData);

        // Read the data back from the database using the method being tested
        String retrievedData = weatherDatabase.readWeatherData();

        // Verify that the data retrieved matches the expected data
        assertEquals(expectedData, retrievedData, "The data retrieved from the database does not match the expected data.");
    }

    private void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/weatherdb", "psqluser", "98psql");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS weather_data (" +
                    "id SERIAL PRIMARY KEY, " +
                    "json_data TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table before tests: " + e.getMessage(), e);
        }
    }

    private void insertTestData(String data) {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/weatherdb", "psqluser", "98psql");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO weather_data (json_data) VALUES ('" + data.replace("'", "''") + "')");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert test data: " + e.getMessage(), e);
        }
    }
}
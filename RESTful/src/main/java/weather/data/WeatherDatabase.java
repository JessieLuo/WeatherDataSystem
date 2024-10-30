package weather.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeatherDatabase implements WeatherDataInterface {
    private static final Logger logger = Logger.getLogger(WeatherDatabase.class.getName());
    private String dbUrl;
    private String user;
    private String password;

    public WeatherDatabase() {
        // Load database properties
        loadDatabaseProperties();

        // Load PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "PostgreSQL JDBC Driver not found", e);
        }
    }

    private void loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.log(Level.SEVERE, "Sorry, unable to find database.properties");
                return;
            }
            // Load the properties file
            props.load(input);
            // Get the property values
            dbUrl = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading database properties", ex);
        }
    }

    @Override
    public void saveWeatherData(String data) {
        String sql = "INSERT INTO weather_data (json_data) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.executeUpdate();
            logger.info("Weather data saved to database successfully.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save weather data to database", e);
        }
    }

    @Override
    public String readWeatherData() {
        // TODO: Carefully decide how to read the specified GET requested Data
        // Now it only simply return the latest recorded data, not really care the order and id
        String sql = "SELECT json_data FROM weather_data ORDER BY id DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("json_data");
            } else {
                logger.warning("No weather data found in database.");
                return "{}"; // Return empty JSON if no data found
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to read weather data from database", e);
            return "{}"; // Return empty JSON in case of an error
        }
    }

    @Override
    public void clearWeatherData() {
        String sql = "DELETE FROM weather_data";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int rowsDeleted = pstmt.executeUpdate();
            logger.info("Cleared " + rowsDeleted + " rows from the weather_data table.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to clear weather data from database", e);
        }
    }
}

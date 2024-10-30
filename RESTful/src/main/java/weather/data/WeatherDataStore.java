package weather.data;

import weather.utils.JSONParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All the json data in server only retained in local machine file system.
 */
public class WeatherDataStore implements WeatherDataInterface {
    private static final Logger logger = Logger.getLogger(WeatherDataStore.class.getName());
    // Save all content in single file
    private static final String DATA_FILE_PATH = "Data/weatherData.json";

    @Override
    public void saveWeatherData(String data) {
        try {
            // appending the JSON object to the file by jsonArray
            JSONParser.appendJsonObject(data, DATA_FILE_PATH);
            logger.info("Weather data appended successfully to " + DATA_FILE_PATH);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save weather data", e);
        }
    }

    @Override
    public String readWeatherData() {
        Path path = Paths.get(DATA_FILE_PATH);
        try {
            if (Files.exists(path)) {
                return Files.readString(path);
            } else {
                logger.warning("No weather data found.");
                return "{}"; // Return empty JSON if no data found
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read weather data", e);
            return "{}"; // Return empty JSON in case of an error
        }
    }

    @Override
    public void clearWeatherData() {
        Path path = Paths.get(DATA_FILE_PATH);
        try {
            if (Files.exists(path)) {
                // Overwrite file with empty JSON object Array
                Files.writeString(path, "[]");
                logger.info("Weather data cleared successfully in " + DATA_FILE_PATH);
            } else {
                logger.warning("No weather data file found to clear.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to clear weather data", e);
        }
    }
}

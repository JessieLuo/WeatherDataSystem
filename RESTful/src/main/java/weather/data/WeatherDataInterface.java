package weather.data;

/**
 * Interface for managing weather data storage.
 */
public interface WeatherDataInterface {
    /**
     * Saves weather data to a persistent storage.
     * @param data The weather data to be saved, represented as a JSON string.
     */
    void saveWeatherData(String data);

    /**
     * Reads all stored weather data from persistent storage.
     * @return A JSON string representing the stored weather data.
     */
    String readWeatherData();

    /**
     * Clears all stored weather data from persistent storage.
     */
    void clearWeatherData();
}

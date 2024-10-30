package weather.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Helper Class to handle JSON-related tasks, including formatting data, ensuring valid JSON,
 * appending to JSON arrays in files, and handling multiple write operations.
 */
public class JSONParser {

    /**
     * Formats the given data as JSON and adds additional key-value pairs.
     * <p>
     * Example usage:
     * <pre>
     * String json = JSONParser.formatDataAsJson("{\"name\": \"John\"}", "timestamp", System.nanoTime(), "id", 12345);
     * </pre>
     *
     * @param data    The main content to include inside the "data" field.
     * @param newData Additional key-value pairs to include at the top level of the JSON.
     *                Each key must be followed by its value.
     * @return A formatted JSON string.
     * @throws IllegalArgumentException If the number of arguments in keyValuePairs is not even.
     */
    public static String formatDataAsJson(String data, Map<String, Object> newData) {
        // Create the main JSON object
        JSONObject jsonObject = new JSONObject();

        // Parse the incoming data and add it as the main content under the "data" field
        jsonObject.put("data", ensureJsonFormat(data));  // Ensure the original data is valid JSON

        // Loop through the key-value pairs in the map and add them to the JSON object
        for (Map.Entry<String, Object> entry : newData.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue());  // Add the key-value pair to the JSON
        }

        // Return the formatted JSON string with proper indentation
        return jsonObject.toString(4);
    }

    /**
     * Ensures the content is valid JSON
     */
    public static JSONObject ensureJsonFormat(String content) {
        try {
            // Try to parse the content as JSON and return a formatted json object
            return new JSONObject(content);
        } catch (JSONException e) {
            // If content is not valid JSON, wrap it as a JSON object
            return wrapTextAsJson(content);  // Return the formatted JSON object
        }
    }

    /**
     * Appends a JSON object to the existing JSON array in the file, then writes it back
     */
    public static void appendJsonObject(String data, String filePath) throws IOException {
        // Read existing array or create a new one
        JSONArray jsonArray = readJsonArrayFrom(filePath);
        // Convert the string data to a JSONObject
        JSONObject jsonObject = ensureJsonFormat(data);
        jsonArray.put(jsonObject);  // Append new JSON object to array
        writeJsonArrayTo(jsonArray, filePath);  // Write updated array back to file
    }

    // Reads the JSON array from the source
    private static JSONArray readJsonArrayFrom(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            String content = Files.readString(path);
            if (!content.isBlank()) {
                return new JSONArray(content);  // Parse and return the JSON array
            }
        }
        return new JSONArray();  // Return an empty array if file doesn't exist or is blank
    }

    // Writes the JSON array to the destination
    private static void writeJsonArrayTo(JSONArray jsonArray, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());  // Ensure directory exists
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(jsonArray.toString(4));  // Write the formatted JSON array to the file
        }
    }

    // Converts plain text data into a valid JSON object; wrapping the text as key-value pairs
    private static JSONObject wrapTextAsJson(String content) {
        JSONObject jsonObject = new JSONObject();

        // Split the plain text by lines and create key-value pairs
        String[] lines = content.split("\n");
        for (String line : lines) {
            // Remove any trailing comma
            line = line.trim().replaceFirst(",$", "");

            // Split by the first colon to extract key and value
            String[] keyValue = line.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");  // Clean up the key
                String value = keyValue[1].trim();  // Clean up the value

                // Process value to either number or string
                jsonObject.put(key, parseValue(value));
            }
        }

        return jsonObject;
    }

    // Parses the value into its correct type (string, integer, or double).
    private static Object parseValue(String value) {
        // Remove quotes from the value if present
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);  // Return as string without quotes
        }

        // Try to parse it as a number (double or integer)
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);  // Parse as double if there's a decimal point
            } else {
                return Integer.parseInt(value);  // Parse as integer
            }
        } catch (NumberFormatException e) {
            return value;  // If parsing fails, return it as a raw string
        }
    }

    /**
     * Extracts the JSON object from the JSON array string at the given index.
     *
     * @param jsonArrayStr The JSON array string.
     * @param index        The index of the object to extract.
     * @return The JSON object as a string, or null if no valid object is found.
     */
    public static String extractJsonObjectByIndex(String jsonArrayStr, int index) {
        try {
            // Parse the content as a JSON array
            JSONArray jsonArray = new JSONArray(jsonArrayStr);

            // Check if the index is valid
            if (index < 0 || index >= jsonArray.length()) {
                return null;
            }

            // Return the JSON object at the given index as a formatted string
            return jsonArray.getJSONObject(index).toString(4);
        } catch (JSONException e) {
            return null;  // Return null if parsing fails
        }
    }

    /**
     * Returns the length of the JSON array string.
     *
     * @param jsonArrayStr The JSON array string.
     * @return The length of the JSON array, or -1 if parsing fails.
     */
    public static int getJsonArrayLength(String jsonArrayStr) {
        try {
            // Parse the content as a JSON array
            JSONArray jsonArray = new JSONArray(jsonArrayStr);
            return jsonArray.length();  // Return the length of the array
        } catch (JSONException e) {
            return -1;  // Return -1 if parsing fails
        }
    }
}

package weather.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Never Use the class.
 * It just retained for potential future usage.
 */
@Deprecated
class DatabaseConnectionTest {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/weatherdb";
    private static final String USER = "psqluser";
    private static final String PASSWORD = "98psql";

    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Try to establish a connection to the database
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            if (conn != null) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e) {
            // Handle errors for JDBC
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        } finally {
            // Close the connection if it was successful
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestDBConnection {
    private static final Logger LOGGER = Logger.getLogger(TestDBConnection.class.getName());
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        Connection conn = MySQLConnection.getConnection();
        if (conn != null) {
            System.out.println("SUCCESS: Connected to the database!");
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing connection", e);
            }
        } else {
            System.out.println("FAILURE: Could not connect to the database.");
            System.out.println("Please check your db.properties file and ensure MySQL is running.");
        }
    }
}

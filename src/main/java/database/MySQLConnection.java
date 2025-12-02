package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnection {
    private static final Logger LOGGER = Logger.getLogger(MySQLConnection.class.getName());
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3307/food_delivery_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "password";
    
    private static Connection connection;

    private MySQLConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "MySQL Driver not found!", e);
                }
                // Load database configuration from src/main/resources/db.properties if present
                Properties props = new Properties();
                String url = DEFAULT_URL;
                String user = DEFAULT_USER;
                String password = DEFAULT_PASSWORD;
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                    if (is != null) {
                        props.load(is);
                        url = props.getProperty("db.url", url);
                        user = props.getProperty("db.user", user);
                        password = props.getProperty("db.password", password);
                    } else {
                        System.out.println("db.properties not found in resources; using default config.");
                    }
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, "Unable to load db.properties, using default values.", ioe);
                }

                // Allow overrides from system properties
                String sysUrl = System.getProperty("db.url");
                String sysUser = System.getProperty("db.user");
                String sysPassword = System.getProperty("db.password");
                if (sysUrl != null && !sysUrl.isEmpty()) url = sysUrl;
                if (sysUser != null && !sysUser.isEmpty()) user = sysUser;
                if (sysPassword != null && !sysPassword.isEmpty()) password = sysPassword;

                // Allow overrides from environment variables
                String envUrl = System.getenv("DB_URL");
                String envUser = System.getenv("DB_USER");
                String envPassword = System.getenv("DB_PASSWORD");
                if (envUrl != null && !envUrl.isEmpty()) url = envUrl;
                if (envUser != null && !envUser.isEmpty()) user = envUser;
                if (envPassword != null && !envPassword.isEmpty()) password = envPassword;

                // If URL changed (e.g., in tests or environment), close the existing connection to reconnect
                try {
                    if (connection != null && !connection.isClosed()) {
                        String currentUrl = connection.getMetaData().getURL();
                        if (!currentUrl.equals(url)) {
                            connection.close();
                            connection = null;
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Could not compare or close existing DB connection", e);
                    connection = null;
                }

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Database connected successfully!");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database! Please check src/main/resources/db.properties and ensure the database is running and credentials are correct.", e);
        }
        return connection;
    }
}
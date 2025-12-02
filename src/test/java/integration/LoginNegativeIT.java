package integration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertNull;
import org.junit.Test;

import ecosystem.EcoSystem;

public class LoginNegativeIT {

    public static org.testcontainers.containers.MySQLContainer<?> mysql =
        new org.testcontainers.containers.MySQLContainer<>(org.testcontainers.utility.DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("food_delivery_db").withUsername("test").withPassword("test");

    private void runSqlScript(String jdbcUrl, String user, String pass, File script) throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass)) {
            String sql = new String(java.nio.file.Files.readAllBytes(script.toPath()));
            // Remove any database creation and USE statements so the container user doesn't need global CREATE privileges
            sql = sql.replaceAll("(?i)CREATE DATABASE[^;]*;", "");
            sql = sql.replaceAll("(?i)USE [^;]*;", "");
            for (String stmt : sql.split(";")) {
                if (!stmt.trim().isEmpty()) conn.createStatement().execute(stmt);
            }
        }
    }

    @Test
    public void testInvalidLogins() throws Exception {
        mysql.start();
        String jdbcUrl = mysql.getJdbcUrl();
        String user = mysql.getUsername();
        String pass = mysql.getPassword();

        // Run DB schema and seed script
        File script = new File("./db/schema.sql");
        runSqlScript(jdbcUrl, user, pass, script);

        // Configure connection properties for our app
        System.setProperty("db.url", jdbcUrl);
        System.setProperty("db.user", user);
        System.setProperty("db.password", pass);

        // Ensure demo reset is enabled so the password migration/ensuring code runs
        System.setProperty("DEMO_RESET", "true");
        EcoSystem system = EcoSystem.getInstance();

        // Empty username
        assertNull("Empty username should not authenticate", system.login("", "customer1"));

        // Empty password
        assertNull("Empty password should not authenticate", system.login("customer1", ""));

        // Wrong password
        assertNull("Wrong password should not authenticate", system.login("customer1", "wrongpassword"));

        // Unknown username
        assertNull("Unknown username should not authenticate", system.login("unknownuser", "whatever"));

        // Leading/trailing whitespace with wrong password
        assertNull("Whitespace-trimmed username but wrong password should not authenticate", system.login(" customer1 ", "wrongpassword"));

        mysql.stop();
    }
}
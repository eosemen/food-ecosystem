package integration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import ecosystem.EcoSystem;
import model.User;

public class LoginIT {

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
    public void testLoginAsCustomer() throws Exception {
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
        User customer = system.login("customer1", "customer1");
        assertNotNull("Login must succeed for seeded customer1", customer);
        // Password should be hashed in DB (bcrypt); ensure we get a hashed string
        assertNotNull("Password should be present", customer.getPassword());
        org.junit.Assert.assertTrue("Stored password should be bcrypt hash", customer.getPassword().startsWith("$2"));

        // Also verify manager and delivery logins
        User manager = system.login("manager1", "manager1");
        assertNotNull("Login must succeed for seeded manager1", manager);

        User delivery = system.login("delivery1", "delivery1");
        assertNotNull("Login must succeed for seeded delivery1", delivery);

        // Check that login is resilient to case and whitespace
        User customerUpper = system.login("CUSTOMER1", "customer1");
        assertNotNull("Uppercase username should still login for customer1", customerUpper);

        // Uppercase/whitespace variants for other seeded users
        User managerUpper = system.login("MANAGER1", "manager1");
        assertNotNull("Uppercase username should still login for manager1", managerUpper);

        User deliveryTrim = system.login(" delivery1 ", "delivery1");
        assertNotNull("Username with whitespace should still login for delivery1", deliveryTrim);

        User managerTrim = system.login(" manager1 ", "manager1");
        assertNotNull("Username with whitespace should still login for manager1", managerTrim);

        mysql.stop();
    }
}

package dev;

import ecosystem.EcoSystem;
import model.User;

public class LoginTester {
    public static void main(String[] args) {
        // Enable demo reset to ensure consistent seed password migration for the demo
        System.setProperty("DEMO_RESET", "true");
        EcoSystem system = EcoSystem.getInstance();
        testLogin(system, "customer1", "customer1");
        testLogin(system, "customer1 ", "customer1");
        testLogin(system, "customer1", " customer1 ");
        testLogin(system, "CUSTOMER1", "customer1");
        testLogin(system, "manager1", "manager1");
    }

    private static void testLogin(EcoSystem system, String u, String p) {
        System.out.println("Attempting login: '" + u + "' / '" + p + "'");
        User user = system.login(u, p);
        if (user != null) {
            System.out.println(" SUCCESS: logged in as " + user.getUsername() + " (" + user.getRole() + ")\n");
        } else {
            System.out.println(" FAILURE: invalid credentials\n");
        }
    }
}

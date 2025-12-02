package ui;

import ecosystem.EcoSystem;

import javax.swing.*;
import java.awt.*;

public class MainJFrame extends JFrame {
    private final EcoSystem system;
    private JPanel container;
    private CardLayout cardLayout;

    public MainJFrame() {
        system = EcoSystem.getInstance();
        initComponents();
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Food Delivery Ecosystem");
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        LoginPanel loginPanel = new LoginPanel(this, system);
        container.add("LoginPanel", loginPanel);

        this.add(container);
        cardLayout.show(container, "LoginPanel");
    }

    public void logout() {
        system.logout();
        container.removeAll();
        LoginPanel loginPanel = new LoginPanel(this, system);
        container.add("LoginPanel", loginPanel);
        cardLayout.show(container, "LoginPanel");
    }

    public void navigateTo(JPanel panel, String key) {
        container.add(key, panel);
        cardLayout.show(container, key);
    }
}

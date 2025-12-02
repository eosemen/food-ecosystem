package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ecosystem.EcoSystem;
import model.Enterprise;
import model.User;

public class AnalyticsPanel extends JPanel {
    private final EcoSystem system;

    public AnalyticsPanel(EcoSystem system) {
        this.system = system;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        JLabel titleLabel = new JLabel("System Analytics Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        List<User> allUsers = system.getAllUsers();
        List<Enterprise> allEnterprises = system.getAllEnterprises();

        long customerCount = allUsers.stream().filter(u -> "Customer".equals(u.getRole())).count();
        long deliveryCount = allUsers.stream().filter(u -> "DeliveryMan".equals(u.getRole())).count();
        long managerCount = allUsers.stream().filter(u -> "Manager".equals(u.getRole())).count();

        gbc.gridwidth = 1; gbc.gridy = 1;

        gbc.gridx = 0;
        add(createStatCard("Total Users", String.valueOf(allUsers.size()), Color.BLUE), gbc);

        gbc.gridx = 1;
        add(createStatCard("Total Enterprises", String.valueOf(allEnterprises.size()), Color.GREEN), gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(createStatCard("Customers", String.valueOf(customerCount), Color.ORANGE), gbc);

        gbc.gridx = 1;
        add(createStatCard("Delivery Personnel", String.valueOf(deliveryCount), Color.MAGENTA), gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(createStatCard("Managers", String.valueOf(managerCount), Color.RED), gbc);

        gbc.gridx = 1;
        add(createStatCard("Active Orders", "N/A", Color.CYAN), gbc);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }
}

package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ecosystem.EcoSystem;
import model.User;

public class LoginPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(LoginPanel.class.getName());
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginPanel(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 248, 255)); // Alice Blue

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblTitle = new JLabel("Food Delivery Ecosystem");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(25, 25, 112)); // Midnight Blue
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblTitle, gbc);

        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(lblUsername, gbc);

        txtUsername = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(txtPassword, gbc);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(30, 144, 255)); // Dodger Blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> login());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(btnLogin, gbc);
    }

    private void login() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        password = password.trim();
        // Ensure not null for static analysis
        username = username == null ? "" : username;
        password = password == null ? "" : password;
        // Debugging: only enabled when DEBUG_LOGIN environment variable is set
        String debugEnv = System.getenv("DEBUG_LOGIN");
        if (debugEnv != null && (debugEnv.equals("1") || debugEnv.equalsIgnoreCase("true"))) {
            // ADD THIS DEBUG CODE
            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Attempting login with username: [" + username + "]");
            String masked = "*".repeat(Math.max(0, password.length()));
            System.out.println("Password (masked): [" + masked + "]");
            
            // Debug: Check database connection and users
            try (java.sql.Connection conn = database.MySQLConnection.getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT username, password, role FROM users")) {
                System.out.println("Users in database:");
                while (rs.next()) {
                    // Mask the stored password when printing
                    String pw = rs.getString("password");
                    String maskedPw = (pw == null) ? "" : "*".repeat(Math.max(0, pw.length()));
                    System.out.println("  - " + rs.getString("username") + " / " + maskedPw + " (" + rs.getString("role") + ")");
                }
            } catch (java.sql.SQLException e) {
                LOGGER.log(Level.SEVERE, "Database error while listing users", e);
            }
            System.out.println("==================");
        }
        // END DEBUG CODE

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = system.login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Welcome " + user.getName());
            // Navigate based on role
            switch (user.getRole()) {
                case "SystemAdmin" -> mainFrame.navigateTo(new SystemAdminWorkArea(mainFrame, system), "SystemAdmin");
                case "Customer" -> mainFrame.navigateTo(new CustomerWorkArea(mainFrame, system), "Customer");
                case "Manager" -> mainFrame.navigateTo(new RestaurantManagerWorkArea(mainFrame, system), "Manager"); // Restaurant Manager
                case "DeliveryMan" -> mainFrame.navigateTo(new DeliveryManWorkArea(mainFrame, system), "DeliveryMan");
                case "EnterpriseAdmin" -> mainFrame.navigateTo(new EnterpriseAdminWorkArea(mainFrame, system), "EnterpriseAdmin");
                default -> JOptionPane.showMessageDialog(this, "Role not supported yet.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

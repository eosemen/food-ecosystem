package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ecosystem.EcoSystem;
import model.Enterprise;
import model.Order;
import util.BackgroundTask;

public class PlaceOrderPanel extends JPanel {
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JComboBox<String> cmbRestaurants;
    private JTextArea txtComment;
    private JTextField txtAddress;

    public PlaceOrderPanel(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
        populateRestaurants();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("<< Back");
        btnBack.addActionListener(e -> mainFrame.navigateTo(new CustomerWorkArea(mainFrame, system), "Customer"));
        headerPanel.add(btnBack);
        headerPanel.add(new JLabel("Place Order"));
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2));

        formPanel.add(new JLabel("Select Restaurant:"));
        cmbRestaurants = new JComboBox<>();
        formPanel.add(cmbRestaurants);

        formPanel.add(new JLabel("Delivery Address:"));
        txtAddress = new JTextField();
        formPanel.add(txtAddress);

        formPanel.add(new JLabel("Order Details / Comment:"));
        txtComment = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(txtComment));

        JButton btnPlace = new JButton("Place Order");
        btnPlace.addActionListener(e -> placeOrder(e));
        formPanel.add(btnPlace);

        add(formPanel, BorderLayout.CENTER);
    }

    private void populateRestaurants() {
        // For simplicity, we are listing Enterprises of type 'Restaurant'
        // In a real app, we might list Organizations or specific Restaurant objects
        List<Enterprise> enterprises = system.getAllEnterprises();
        for (Enterprise ent : enterprises) {
            if ("Restaurant".equalsIgnoreCase(ent.getType())) {
                cmbRestaurants.addItem(ent.getName() + " (ID: " + ent.getId() + ")");
            }
        }
    }

    private void placeOrder(java.awt.event.ActionEvent e) {
        String restaurantStr = (String) cmbRestaurants.getSelectedItem();
        String address = txtAddress.getText().trim();
        String comment = txtComment.getText().trim();

        // Validation
        if (restaurantStr == null || restaurantStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a restaurant", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Delivery address is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (address.length() < 10) {
            JOptionPane.showMessageDialog(this, "Please enter a complete delivery address", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // No additional null checks required here -- earlier validation covers missing inputs

        // Parse ID using regex: from the string "Name (ID: 1)"
        int restaurantId;
        Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");
        Matcher m = idPattern.matcher(restaurantStr);
        if (!m.find()) {
            JOptionPane.showMessageDialog(this, "Invalid restaurant selection", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            restaurantId = Integer.parseInt(m.group(1));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid restaurant ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JButton source = (JButton) e.getSource();
        source.setEnabled(false);
        source.setText("Processing...");

        Order order = new Order();
        order.setCustomerId(system.getCurrentUser().getId());
        order.setRestaurantId(restaurantId);
        order.setStatus("Placed");
        order.setDeliveryAddress(address);
        order.setComment(comment);

        BackgroundTask.execute(
            () -> { system.placeOrder(order); return order.getId(); },
            (orderId) -> {
                source.setEnabled(true);
                source.setText("Place Order");
                // Ask user if they want to add items only after the order is successfully created
                java.util.List<model.MenuItem> menuAfter = system.getMenu(restaurantId);
                if (menuAfter != null && !menuAfter.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Available Menu Items:\n");
                    for (model.MenuItem m2 : menuAfter) {
                        sb.append(m2.getId()).append(": ").append(m2.getName()).append(" (Price: ").append(m2.getPrice()).append(")\n");
                    }
                    sb.append("\nEnter items as id:qty separated by commas (e.g. 1:2,2:1). Leave blank if none.");
                    String inputItems = JOptionPane.showInputDialog(this, sb.toString());
                    if (inputItems != null && !inputItems.trim().isEmpty()) {
                        String[] parts = inputItems.split(",");
                        // add order items in a background task so UI doesn't freeze
                        BackgroundTask.execute(() -> {
                                for (String p : parts) {
                                    String[] kv = p.split(":");
                                    if (kv.length == 2) {
                                        try {
                                            int menuItemId = Integer.parseInt(kv[0].trim());
                                            int qty = Integer.parseInt(kv[1].trim());
                                            model.MenuItem selected = null;
                                            for (model.MenuItem m3 : menuAfter) {
                                                if (m3.getId() == menuItemId) { selected = m3; break; }
                                            }
                                            if (selected != null) {
                                                model.OrderItem oi = new model.OrderItem();
                                                oi.setOrderId(orderId);
                                                oi.setMenuItemName(selected.getName());
                                                oi.setPrice(selected.getPrice());
                                                oi.setQuantity(qty);
                                                system.createOrderItem(oi);
                                            }
                                        } catch (NumberFormatException nfe) {
                                            // ignore malformed entries
                                        }
                                    }
                                }
                                return true;
                            },
                            (r) -> {},
                            (err) -> {
                                JOptionPane.showMessageDialog(this,
                                    "Failed to add some order items: " + err.getMessage(),
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        );
                    }
                }
                JOptionPane.showMessageDialog(this, "Order placed successfully! Order ID: " + orderId);
                mainFrame.navigateTo(new CustomerWorkArea(mainFrame, system), "Customer");
            },
            (error) -> {
                source.setEnabled(true);
                source.setText("Place Order");
                JOptionPane.showMessageDialog(this,
                        "Failed to place order: " + error.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
        // success/error handling and navigation are performed in the callback above
    }
}

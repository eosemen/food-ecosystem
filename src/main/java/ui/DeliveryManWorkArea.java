package ui;

import ecosystem.EcoSystem;
import model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeliveryManWorkArea extends JPanel {
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JTable tblOrders;

    public DeliveryManWorkArea(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
        populateOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> mainFrame.logout());
        headerPanel.add(new JLabel("Delivery Man Area"));
        headerPanel.add(btnLogout);
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        tblOrders = new JTable();
        contentPanel.add(new JScrollPane(tblOrders), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton btnPickUp = new JButton("Pick Up");
        btnPickUp.addActionListener(e -> updateStatus("OutForDelivery"));
        JButton btnDeliver = new JButton("Delivered");
        btnDeliver.addActionListener(e -> updateStatus("Delivered"));

        actionPanel.add(btnPickUp);
        actionPanel.add(btnDeliver);
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void populateOrders() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Order ID");
        model.addColumn("Restaurant");
        model.addColumn("Address");
        model.addColumn("Status");

        // Show orders assigned to this delivery man OR available orders?
        // For simplicity, let's show orders assigned to this delivery man.
        List<Order> orders = system.getOrdersForDeliveryMan(system.getCurrentUser().getId());
        for (Order o : orders) {
            model.addRow(new Object[] { o.getId(), o.getRestaurantId(), o.getDeliveryAddress(), o.getStatus() });
        }
        tblOrders.setModel(model);
    }

    private void updateStatus(String status) {
        int selectedRow = tblOrders.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tblOrders.getValueAt(selectedRow, 0);
            system.updateOrderStatus(orderId, status, system.getCurrentUser().getId());
            populateOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order");
        }
    }
}

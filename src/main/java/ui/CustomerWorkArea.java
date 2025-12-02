package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ecosystem.EcoSystem;
import model.Order;
// We will reference util.BackgroundTask explicitly below to avoid static import resolution issues in some tools

public class CustomerWorkArea extends JPanel {
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JTable tblOrders;
    private JProgressBar progressBar;
    private JButton btnNewOrder;

    public CustomerWorkArea(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
        populateOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> mainFrame.logout());
        headerPanel.add(new JLabel("Welcome " + system.getCurrentUser().getName()));
        headerPanel.add(btnLogout);
        add(headerPanel, BorderLayout.NORTH);

        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        contentPanel.add(progressBar, BorderLayout.NORTH);

        // Orders table
        tblOrders = new JTable();
        contentPanel.add(new JScrollPane(tblOrders), BorderLayout.CENTER);

        // New order button
        btnNewOrder = new JButton("Place New Order");
        btnNewOrder.addActionListener(e -> 
            mainFrame.navigateTo(new PlaceOrderPanel(mainFrame, system), "PlaceOrder")
        );
        contentPanel.add(btnNewOrder, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void populateOrders() {
        // Show loading state
        progressBar.setVisible(true);
        btnNewOrder.setEnabled(false);

        util.BackgroundTask.execute(
            // Background task
            () -> system.getOrdersForCustomer(system.getCurrentUser().getId()),

            // Success
            (orders) -> {
                updateOrdersTable(orders);
                progressBar.setVisible(false);
                btnNewOrder.setEnabled(true);
            },

            // Error
            (error) -> {
                progressBar.setVisible(false);
                btnNewOrder.setEnabled(true);
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to load orders: " + error.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        );
    }

    private void updateOrdersTable(List<Order> orders) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Order ID");
        model.addColumn("Restaurant");
        model.addColumn("Status");
        model.addColumn("Date");

        for (Order o : orders) {
            model.addRow(new Object[]{
                o.getId(),
                o.getRestaurantId(),
                o.getStatus(),
                o.getOrderDate()
            });
        }

        tblOrders.setModel(model);
    }
}

package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ecosystem.EcoSystem;
import model.MenuItem;
import model.Order;
import model.User;

public class RestaurantManagerWorkArea extends JPanel {
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JTable tblOrders;
    private JTable tblMenu;

    public RestaurantManagerWorkArea(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
        populateOrders();
        populateMenu();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> mainFrame.logout());
        headerPanel.add(new JLabel("Restaurant Manager Area"));
        headerPanel.add(btnLogout);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Orders Tab
        JPanel ordersPanel = new JPanel(new BorderLayout());
        tblOrders = new JTable();
        ordersPanel.add(new JScrollPane(tblOrders), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton btnAccept = new JButton("Accept Order");
        btnAccept.addActionListener(e -> updateStatus("Accepted"));
        
        JButton btnReady = new JButton("Ready for Pickup");
        btnReady.addActionListener(e -> assignToDelivery());
        
        JButton btnViewDetails = new JButton("View Details");
        btnViewDetails.addActionListener(e -> viewOrderDetails());

        actionPanel.add(btnAccept);
        actionPanel.add(btnReady);
        actionPanel.add(btnViewDetails);
        ordersPanel.add(actionPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Manage Orders", ordersPanel);

        // Menu Tab
        JPanel menuPanel = new JPanel(new BorderLayout());
        tblMenu = new JTable();
        menuPanel.add(new JScrollPane(tblMenu), BorderLayout.CENTER);

        JPanel menuActionPanel = new JPanel();
        JButton btnAddItem = new JButton("Add Item");
        btnAddItem.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Item Name:");
            if (name == null || name.trim().isEmpty()) return;
            String priceStr = JOptionPane.showInputDialog(this, "Price:");
            if (priceStr == null || priceStr.trim().isEmpty()) return;
            String desc = JOptionPane.showInputDialog(this, "Description:");
            try {
                java.math.BigDecimal price = new java.math.BigDecimal(priceStr);
                MenuItem item = new MenuItem();
                item.setName(name);
                item.setPrice(price);
                item.setDescription(desc);
                item.setRestaurantId(system.getCurrentUser().getOrganizationId());
                system.addMenuItem(item);
                populateMenu();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid price");
            }
        });

        JButton btnEditItem = new JButton("Edit Item");
        btnEditItem.addActionListener(e -> editSelectedMenuItem());
        
        JButton btnDeleteItem = new JButton("Delete Item");
        btnDeleteItem.addActionListener(e -> deleteSelectedMenuItem());
        
        menuActionPanel.add(btnAddItem);
        menuActionPanel.add(btnEditItem);
        menuActionPanel.add(btnDeleteItem);
        menuPanel.add(menuActionPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Manage Menu", menuPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void populateOrders() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Order ID");
        model.addColumn("Customer ID");
        model.addColumn("Status");
        model.addColumn("Delivery Man");
        model.addColumn("Date");

        int restaurantId = system.getCurrentUser().getOrganizationId();
        List<Order> orders = system.getOrdersForRestaurant(restaurantId);
        
        for (Order o : orders) {
            String deliveryMan = o.getDeliveryManId() > 0 ? "Assigned (#" + o.getDeliveryManId() + ")" : "Not Assigned";
            model.addRow(new Object[] { 
                o.getId(), 
                o.getCustomerId(), 
                o.getStatus(), 
                deliveryMan,
                o.getOrderDate() 
            });
        }
        tblOrders.setModel(model);
    }

    private void populateMenu() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Price");
        model.addColumn("Description");

        int restaurantId = system.getCurrentUser().getOrganizationId();
        java.util.List<MenuItem> items = system.getMenu(restaurantId);
        for (MenuItem item : items) {
            model.addRow(new Object[] { item.getId(), item.getName(), item.getPrice(), item.getDescription() });
        }
        tblMenu.setModel(model);
    }

    private void assignToDelivery() {
        int selectedRow = tblOrders.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tblOrders.getValueAt(selectedRow, 0);
            String currentStatus = (String) tblOrders.getValueAt(selectedRow, 2);
            
            // Check if order is in correct status
            if (!"Accepted".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this, 
                    "Please accept the order first before marking it ready for pickup", 
                    "Order Not Accepted", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get list of available delivery personnel
            List<User> allUsers = system.getAllUsers();
            List<User> deliveryPersonnel = allUsers.stream()
                .filter(u -> "DeliveryMan".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
            
            if (deliveryPersonnel.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No delivery personnel available in the system", 
                    "No Delivery Personnel", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Let manager choose a delivery person
            String[] deliveryOptions = deliveryPersonnel.stream()
                .map(u -> u.getName() + " (ID: " + u.getId() + ")")
                .toArray(String[]::new);
            
            String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select delivery person to assign:",
                "Assign Delivery",
                JOptionPane.QUESTION_MESSAGE,
                null,
                deliveryOptions,
                deliveryOptions[0]
            );
            
            if (selected != null) {
                // Extract delivery person ID from selection
                int deliveryManId = -1;
                for (User u : deliveryPersonnel) {
                    if (selected.contains("ID: " + u.getId())) {
                        deliveryManId = u.getId();
                        break;
                    }
                }
                
                if (deliveryManId > 0) {
                    // Update order status and assign delivery person
                    system.updateOrderStatus(orderId, "ReadyForPickup", deliveryManId);
                    JOptionPane.showMessageDialog(this, 
                        "Order assigned to delivery person successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    populateOrders();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order");
        }
    }

    private void viewOrderDetails() {
        int selectedRow = tblOrders.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tblOrders.getValueAt(selectedRow, 0);
            
            // Get order items
            List<model.OrderItem> items = system.getOrderItems(orderId);
            
            StringBuilder details = new StringBuilder();
            details.append("Order ID: ").append(orderId).append("\n");
            details.append("Customer ID: ").append(tblOrders.getValueAt(selectedRow, 1)).append("\n");
            details.append("Status: ").append(tblOrders.getValueAt(selectedRow, 2)).append("\n");
            details.append("Date: ").append(tblOrders.getValueAt(selectedRow, 4)).append("\n\n");
            
            if (items.isEmpty()) {
                details.append("No items in this order");
            } else {
                details.append("Order Items:\n");
                details.append("-".repeat(40)).append("\n");
                for (model.OrderItem item : items) {
                    details.append(item.getMenuItemName())
                           .append(" x").append(item.getQuantity())
                           .append(" @ $").append(item.getPrice())
                           .append("\n");
                }
            }
            
            JOptionPane.showMessageDialog(this, details.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order");
        }
    }

    private void deleteSelectedMenuItem() {
        int selectedRow = tblMenu.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblMenu.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Menu Item ID: " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                system.deleteMenuItem(id);
                populateMenu();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a menu item to delete");
        }
    }

    private void editSelectedMenuItem() {
        int selectedRow = tblMenu.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblMenu.getValueAt(selectedRow, 0);
            String name = (String) tblMenu.getValueAt(selectedRow, 1);
            java.math.BigDecimal price = (java.math.BigDecimal) tblMenu.getValueAt(selectedRow, 2);
            String desc = (String) tblMenu.getValueAt(selectedRow, 3);

            String newName = JOptionPane.showInputDialog(this, "Enter new name:", name);
            if (newName == null || newName.trim().isEmpty()) return;
            String priceStr = JOptionPane.showInputDialog(this, "Enter new price:", price.toPlainString());
            if (priceStr == null || priceStr.trim().isEmpty()) return;
            String newDesc = JOptionPane.showInputDialog(this, "Enter new description:", desc);
            if (newDesc == null) newDesc = "";
            try {
                java.math.BigDecimal newPrice = new java.math.BigDecimal(priceStr);
                model.MenuItem item = new model.MenuItem();
                item.setId(id);
                item.setName(newName);
                item.setPrice(newPrice);
                item.setDescription(newDesc);
                item.setRestaurantId(system.getCurrentUser().getOrganizationId());
                system.updateMenuItem(item);
                populateMenu();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid price");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a menu item to edit");
        }
    }

    private void updateStatus(String status) {
        int selectedRow = tblOrders.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tblOrders.getValueAt(selectedRow, 0);
            system.updateOrderStatus(orderId, status, 0);
            populateOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order");
        }
    }
}
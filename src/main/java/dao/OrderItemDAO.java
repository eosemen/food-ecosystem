package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import database.MySQLConnection;
import model.OrderItem;

public class OrderItemDAO {
    private static final Logger LOGGER = Logger.getLogger(OrderItemDAO.class.getName());

    public void createOrderItem(OrderItem item) {
        String query = "INSERT INTO order_items (order_id, menu_item_name, price, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.getOrderId());
            stmt.setString(2, item.getMenuItemName());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setInt(4, item.getQuantity());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create order item", e);
        }
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new OrderItem(rs.getInt("id"), rs.getInt("order_id"), rs.getString("menu_item_name"), rs.getBigDecimal("price"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch order items", e);
        }
        return items;
    }

    public void deleteOrderItem(int id) {
        String query = "DELETE FROM order_items WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete order item", e);
        }
    }

    public void updateOrderItem(OrderItem item) {
        String query = "UPDATE order_items SET menu_item_name = ?, price = ?, quantity = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.getMenuItemName());
            stmt.setBigDecimal(2, item.getPrice());
            stmt.setInt(3, item.getQuantity());
            stmt.setInt(4, item.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update order item", e);
        }
    }
}

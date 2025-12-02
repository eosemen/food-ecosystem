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
import model.MenuItem;

public class MenuItemDAO {
    private static final Logger LOGGER = Logger.getLogger(MenuItemDAO.class.getName());

    public void createMenuItem(MenuItem item) {
        String query = "INSERT INTO menu_items (restaurant_id, name, price, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.getRestaurantId());
            stmt.setString(2, item.getName());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setString(4, item.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create menu item", e);
        }
    }

    public List<MenuItem> getMenuItemsByRestaurant(int restaurantId) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE restaurant_id = ?";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch menu items", e);
        }
        return items;
    }

    public void deleteMenuItem(int id) {
        String query = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to delete menu item", e);
        }
    }

    public void updateMenuItem(MenuItem item) {
        String query = "UPDATE menu_items SET restaurant_id = ?, name = ?, price = ?, description = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.getRestaurantId());
            stmt.setString(2, item.getName());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setString(4, item.getDescription());
            stmt.setInt(5, item.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to update menu item", e);
        }
    }
}

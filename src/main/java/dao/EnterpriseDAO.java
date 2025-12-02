package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import database.MySQLConnection;
import model.Enterprise;

public class EnterpriseDAO {
    private static final Logger LOGGER = Logger.getLogger(EnterpriseDAO.class.getName());

    public void createEnterprise(Enterprise enterprise) {
        String query = "INSERT INTO enterprises (name, type) VALUES (?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, enterprise.getName());
            stmt.setString(2, enterprise.getType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create enterprise", e);
        }
    }

    public List<Enterprise> getAllEnterprises() {
        List<Enterprise> enterprises = new ArrayList<>();
        String query = "SELECT * FROM enterprises";
        try (Connection conn = MySQLConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                enterprises.add(new Enterprise(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch enterprises", e);
        }
        return enterprises;
    }

    public void deleteEnterprise(int id) {
        String query = "DELETE FROM enterprises WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to delete enterprise", e);
        }
    }

    public void updateEnterprise(Enterprise enterprise) {
        String query = "UPDATE enterprises SET name = ?, type = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, enterprise.getName());
            stmt.setString(2, enterprise.getType());
            stmt.setInt(3, enterprise.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to update enterprise", e);
        }
    }
}

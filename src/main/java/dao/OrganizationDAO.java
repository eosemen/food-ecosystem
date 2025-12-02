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
import model.Organization;

public class OrganizationDAO {
    private static final Logger LOGGER = Logger.getLogger(OrganizationDAO.class.getName());

    public void createOrganization(Organization org) {
        String query = "INSERT INTO organizations (name, type, enterprise_id) VALUES (?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, org.getName());
            stmt.setString(2, org.getType());
            stmt.setInt(3, org.getEnterpriseId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create organization", e);
        }
    }

    public List<Organization> getOrganizationsByEnterpriseId(int enterpriseId) {
        List<Organization> orgs = new ArrayList<>();
        String query = "SELECT * FROM organizations WHERE enterprise_id = ?";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, enterpriseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orgs.add(new Organization(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("enterprise_id")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch organizations", e);
        }
        return orgs;
    }

    public List<Organization> getAllOrganizations() {
        List<Organization> orgs = new ArrayList<>();
        String query = "SELECT * FROM organizations";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                orgs.add(new Organization(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("enterprise_id")));
            }
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to fetch all organizations", e);
        }
        return orgs;
    }

    public Organization getOrganizationById(int id) {
        String query = "SELECT * FROM organizations WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Organization(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("enterprise_id"));
            }
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to fetch organization by id", e);
        }
        return null;
    }

    public void deleteOrganization(int id) {
        String query = "DELETE FROM organizations WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to delete organization", e);
        }
    }

    public void updateOrganization(Organization org) {
        String query = "UPDATE organizations SET name = ?, type = ?, enterprise_id = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, org.getName());
            stmt.setString(2, org.getType());
            stmt.setInt(3, org.getEnterpriseId());
            stmt.setInt(4, org.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to update organization", e);
        }
    }
}

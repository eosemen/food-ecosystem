package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import database.MySQLConnection;
import model.WorkRequest;

public class WorkRequestDAO {
    private static final Logger LOGGER = Logger.getLogger(WorkRequestDAO.class.getName());

    public void createWorkRequest(WorkRequest wr) {
        String query = "INSERT INTO work_requests (type, sender_enterprise_id, receiver_enterprise_id, related_order_id, status, message) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, wr.getType());
            stmt.setInt(2, wr.getSenderEnterpriseId());
            stmt.setInt(3, wr.getReceiverEnterpriseId());
            if (wr.getRelatedOrderId() != null) stmt.setInt(4, wr.getRelatedOrderId()); else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, wr.getStatus());
            stmt.setString(6, wr.getMessage());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create work request", e);
        }
    }

    public List<WorkRequest> getAllWorkRequests() {
        List<WorkRequest> list = new ArrayList<>();
        String query = "SELECT * FROM work_requests ORDER BY created_at DESC";
        try (Connection conn = MySQLConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch work requests", e);
        }
        return list;
    }

    public List<WorkRequest> getWorkRequestsByReceiver(int enterpriseId) {
        List<WorkRequest> list = new ArrayList<>();
        String query = "SELECT * FROM work_requests WHERE receiver_enterprise_id = ? ORDER BY created_at DESC";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, enterpriseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch work requests by receiver", e);
        }
        return list;
    }

    public List<WorkRequest> getWorkRequestsBySender(int enterpriseId) {
        List<WorkRequest> list = new ArrayList<>();
        String query = "SELECT * FROM work_requests WHERE sender_enterprise_id = ? ORDER BY created_at DESC";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, enterpriseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch work requests by sender", e);
        }
        return list;
    }

    public WorkRequest getWorkRequest(int id) {
        String query = "SELECT * FROM work_requests WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch work request by id", e);
        }
        return null;
    }

    public void updateWorkRequestStatus(int id, String status) {
        String query = "UPDATE work_requests SET status = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update work request status", e);
        }
    }

    public void deleteWorkRequest(int id) {
        String query = "DELETE FROM work_requests WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete work request", e);
        }
    }

    private WorkRequest map(ResultSet rs) throws SQLException {
        WorkRequest wr = new WorkRequest();
        wr.setId(rs.getInt("id"));
        wr.setType(rs.getString("type"));
        wr.setSenderEnterpriseId(rs.getInt("sender_enterprise_id"));
        wr.setReceiverEnterpriseId(rs.getInt("receiver_enterprise_id"));
        int orderId = rs.getInt("related_order_id");
        if (!rs.wasNull()) wr.setRelatedOrderId(orderId);
        wr.setStatus(rs.getString("status"));
        wr.setMessage(rs.getString("message"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) wr.setCreatedAt(ts.toLocalDateTime());
        return wr;
    }
}

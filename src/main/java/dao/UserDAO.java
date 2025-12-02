package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import database.MySQLConnection;
import model.User;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public User authenticate(String username, String password) {
        // Use case-insensitive username lookup; password check done in Java for hashed passwords
        String query = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            // Trim username and password to be tolerant of leading/trailing whitespace
            String trimmedUsername = (username == null) ? null : username.trim();
            String trimmedPassword = (password == null) ? null : password.trim();
            // Early return for empty/invalid credentials
            if (trimmedUsername == null || trimmedUsername.isEmpty() || trimmedPassword == null || trimmedPassword.isEmpty()) {
                return null;
            }
            stmt.setString(1, trimmedUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User found = extractUserFromResultSet(rs);
                String storedPw = found.getPassword();
                if (storedPw == null) return null;
                boolean ok = false;
                // If password appears to be a bcrypt hash, verify using BCrypt
                if (storedPw.startsWith("$2a$") || storedPw.startsWith("$2b$") || storedPw.startsWith("$2y$")) {
                    ok = BCrypt.checkpw(trimmedPassword, storedPw);
                } else {
                    // Old plain-text stored password — compare, and on success, migrate to bcrypt hash
                    if (trimmedPassword.equals(storedPw)) {
                        ok = true;
                        // Migrate stored password to bcrypt hash for safety
                        String hashed = BCrypt.hashpw(trimmedPassword, BCrypt.gensalt(12));
                        found.setPassword(hashed);
                        updateUser(found);
                    }
                }
                return ok ? found : null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to authenticate user", e);
        }
        return null;
    }

    public void createUser(User user) {
        String query = "INSERT INTO users (username, password, role, name, phone, email, organization_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            String username = user.getUsername() == null ? null : user.getUsername().trim();
            String password = user.getPassword() == null ? null : user.getPassword().trim();
            // If password is not already a bcrypt hash, hash it before storing
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                password = BCrypt.hashpw(password, BCrypt.gensalt(12));
            }
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getEmail());
            if (user.getOrganizationId() > 0) {
                stmt.setInt(7, user.getOrganizationId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create user", e);
        }
    }

    public User getUserById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get user by id", e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = MySQLConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get all users", e);
        }
        return users;
    }

    public void deleteUser(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to delete user", e);
        }
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET username = ?, password = ?, role = ?, name = ?, phone = ?, email = ?, organization_id = ? WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String username = user.getUsername() == null ? null : user.getUsername().trim();
            String password = user.getPassword() == null ? null : user.getPassword().trim();
            // If password is not already a bcrypt hash, hash it before storing
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                password = BCrypt.hashpw(password, BCrypt.gensalt(12));
            }
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getEmail());
            if (user.getOrganizationId() > 0) {
                stmt.setInt(7, user.getOrganizationId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            stmt.setInt(8, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to update user", e);
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getInt("organization_id"));
    }
}

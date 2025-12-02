package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import ecosystem.EcoSystem;
import model.Enterprise;

public class SystemAdminWorkArea extends JPanel {
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JTable tblEnterprises;
    private JTable tblUsers;
    private JTable tblOrganizations;
    private JTextField txtName;
    private JComboBox<String> cmbType;

    public SystemAdminWorkArea(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
        populateTable();
        populateUsers();
        populateOrganizations();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> mainFrame.logout());
        headerPanel.add(new JLabel("System Admin Area"));
        headerPanel.add(btnLogout);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JTabbedPane tabbedPane = new JTabbedPane();

        // Enterprise Management Tab
        JPanel enterprisePanel = new JPanel(new BorderLayout());

        // Table
        tblEnterprises = new JTable();
        enterprisePanel.add(new JScrollPane(tblEnterprises), BorderLayout.CENTER);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        formPanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel("Type:"));
        cmbType = new JComboBox<>(new String[] { "Restaurant", "Delivery" });
        formPanel.add(cmbType);

        JButton btnCreate = new JButton("Create Enterprise");
        btnCreate.addActionListener(e -> createEnterprise());
        formPanel.add(btnCreate);

        JButton btnEdit = new JButton("Edit Enterprise");
        btnEdit.addActionListener(e -> editSelectedEnterprise());
        formPanel.add(btnEdit);

        JButton btnDelete = new JButton("Delete Enterprise");
        btnDelete.addActionListener(e -> deleteSelectedEnterprise());
        formPanel.add(btnDelete);

        enterprisePanel.add(formPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Manage Enterprises", enterprisePanel);

        // Manage Enterprise Admins (Simplified for now)
        JPanel adminPanel = new JPanel();
        adminPanel.add(new JLabel("Manage Enterprise Admins (To be implemented)"));
        tabbedPane.addTab("Manage Admins", adminPanel);

        // Users Management Tab
        JPanel usersPanel = new JPanel(new BorderLayout());
        tblUsers = new JTable();
        usersPanel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);
        JPanel usersAction = new JPanel();
        JButton btnEditUser = new JButton("Edit User");
        btnEditUser.addActionListener(e -> editSelectedUser());
        usersAction.add(btnEditUser);

        JButton btnDeleteUser = new JButton("Delete User");
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
        usersAction.add(btnDeleteUser);
        usersPanel.add(usersAction, BorderLayout.SOUTH);
        tabbedPane.addTab("Manage Users", usersPanel);

        // Organizations Management Tab
        JPanel orgPanel = new JPanel(new BorderLayout());
        tblOrganizations = new JTable();
        orgPanel.add(new JScrollPane(tblOrganizations), BorderLayout.CENTER);
        JPanel orgAction = new JPanel();
        JButton btnEditOrg = new JButton("Edit Organization");
        btnEditOrg.addActionListener(e -> editSelectedOrganization());
        orgAction.add(btnEditOrg);

        JButton btnDeleteOrg = new JButton("Delete Organization");
        btnDeleteOrg.addActionListener(e -> deleteSelectedOrganization());
        orgAction.add(btnDeleteOrg);
        orgPanel.add(orgAction, BorderLayout.SOUTH);
        tabbedPane.addTab("Manage Organizations", orgPanel);

        // Analytics
        tabbedPane.addTab("Analytics", new AnalyticsPanel(system));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void populateTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Type");

        List<Enterprise> enterprises = system.getAllEnterprises();
        for (Enterprise ent : enterprises) {
            model.addRow(new Object[] { ent.getId(), ent.getName(), ent.getType() });
        }
        tblEnterprises.setModel(model);
    }

    private void createEnterprise() {
        String name = txtName.getText();
        String type = (String) cmbType.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty");
            return;
        }

        system.createEnterprise(name, type);
        populateTable();
        txtName.setText("");
        JOptionPane.showMessageDialog(this, "Enterprise created successfully");
    }

    private void populateUsers() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Username");
        model.addColumn("Name");
        model.addColumn("Role");
        model.addColumn("Phone");
        model.addColumn("Email");
        model.addColumn("Org ID");

        List<model.User> users = system.getAllUsers();
        for (model.User u : users) {
            model.addRow(new Object[] { u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getPhone(), u.getEmail(), u.getOrganizationId() });
        }
        tblUsers.setModel(model);
    }

    private void deleteSelectedUser() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblUsers.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete User ID: " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                system.deleteUser(id);
                populateUsers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete");
        }
    }

    private void populateOrganizations() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Type");
        model.addColumn("Enterprise ID");

        List<model.Organization> orgs = system.getAllOrganizations();
        for (model.Organization org : orgs) {
            model.addRow(new Object[] { org.getId(), org.getName(), org.getType(), org.getEnterpriseId() });
        }
        tblOrganizations.setModel(model);
    }

    private void deleteSelectedOrganization() {
        int selectedRow = tblOrganizations.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblOrganizations.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Organization ID: " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                system.deleteOrganization(id);
                populateOrganizations();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an organization to delete");
        }
    }

    private void editSelectedEnterprise() {
        int selectedRow = tblEnterprises.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblEnterprises.getValueAt(selectedRow, 0);
            String name = (String) tblEnterprises.getValueAt(selectedRow, 1);
            String type = (String) tblEnterprises.getValueAt(selectedRow, 2);

            String newName = JOptionPane.showInputDialog(this, "Enter new name:", name);
            if (newName == null || newName.trim().isEmpty()) return;
            String[] types = {"Restaurant", "Delivery"};
            String newType = (String) JOptionPane.showInputDialog(this, "Select Type:", "Type", JOptionPane.QUESTION_MESSAGE, null, types, type);
            if (newType == null) return;

            system.updateEnterprise(new model.Enterprise(id, newName, newType));
            populateTable();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an enterprise to edit");
        }
    }

    private void editSelectedUser() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblUsers.getValueAt(selectedRow, 0);
            String username = (String) tblUsers.getValueAt(selectedRow, 1);
            String name = (String) tblUsers.getValueAt(selectedRow, 2);
            String role = (String) tblUsers.getValueAt(selectedRow, 3);
            String phone = (String) tblUsers.getValueAt(selectedRow, 4);
            String email = (String) tblUsers.getValueAt(selectedRow, 5);
            Integer orgId = (Integer) tblUsers.getValueAt(selectedRow, 6);

            String newName = JOptionPane.showInputDialog(this, "Enter new name:", name);
            if (newName == null || newName.trim().isEmpty()) return;
            String[] roles = {"SystemAdmin","EnterpriseAdmin","Manager","Customer","DeliveryMan"};
            String newRole = (String) JOptionPane.showInputDialog(this, "Select Role:", "Role", JOptionPane.QUESTION_MESSAGE, null, roles, role);
            if (newRole == null) return;

            model.User u = new model.User();
            u.setId(id);
            u.setUsername(username);
            u.setPassword("password"); // leave default; or prompt for new
            u.setName(newName);
            u.setRole(newRole);
            u.setPhone(phone);
            u.setEmail(email);
            if (orgId != null && orgId > 0) u.setOrganizationId(orgId);

            system.updateUser(u);
            populateUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to edit");
        }
    }

    private void editSelectedOrganization() {
        int selectedRow = tblOrganizations.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblOrganizations.getValueAt(selectedRow, 0);
            String name = (String) tblOrganizations.getValueAt(selectedRow, 1);
            String type = (String) tblOrganizations.getValueAt(selectedRow, 2);
            int enterpriseId = (int) tblOrganizations.getValueAt(selectedRow, 3);

            String newName = JOptionPane.showInputDialog(this, "Enter new name:", name);
            if (newName == null || newName.trim().isEmpty()) return;
            String[] types = {"Admin","Kitchen","DeliveryTeam"};
            String newType = (String) JOptionPane.showInputDialog(this, "Select Type:", "Type", JOptionPane.QUESTION_MESSAGE, null, types, type);
            if (newType == null) return;

            model.Organization o = new model.Organization();
            o.setId(id);
            o.setName(newName);
            o.setType(newType);
            o.setEnterpriseId(enterpriseId);
            system.updateOrganization(o);
            populateOrganizations();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an organization to edit");
        }
    }

    private void deleteSelectedEnterprise() {
        int selectedRow = tblEnterprises.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblEnterprises.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Enterprise ID: " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                system.deleteEnterprise(id);
                populateTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an enterprise to delete");
        }
    }
}

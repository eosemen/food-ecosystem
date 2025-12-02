package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import ecosystem.EcoSystem;
import model.Organization;
import model.User;
import model.WorkRequest;

public class EnterpriseAdminWorkArea extends JPanel {
    private final MainJFrame mainFrame;
    private final EcoSystem system;
    private JTable tblOrganizations;
    private JTable tblUsers;
    private JTable tblRequests;
    private int enterpriseId;

    public EnterpriseAdminWorkArea(MainJFrame mainFrame, EcoSystem system) {
        this.mainFrame = mainFrame;
        this.system = system;
        initComponents();
        determineEnterpriseId();
        try {
            populateOrganizations();
            populateUsers();
            populateRequests();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void determineEnterpriseId() {
        // Deduce current user's enterprise by finding their organization and its enterprise id
        int orgId = system.getCurrentUser() != null ? system.getCurrentUser().getOrganizationId() : 0;
        List<Organization> allOrgs = system.getAllOrganizations();
        for (Organization o : allOrgs) {
            if (o.getId() == orgId) {
                enterpriseId = o.getEnterpriseId();
                break;
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> mainFrame.logout());
        headerPanel.add(new JLabel("Enterprise Admin Area"));
        headerPanel.add(btnLogout);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Organizations tab
        JPanel orgPanel = new JPanel(new BorderLayout());
        tblOrganizations = new JTable();
        orgPanel.add(new JScrollPane(tblOrganizations), BorderLayout.CENTER);
        JPanel orgAction = new JPanel();
        JButton btnAddOrg = new JButton("Add Organization");
        btnAddOrg.addActionListener(e -> addOrganization());
        JButton btnEditOrg = new JButton("Edit Organization");
        btnEditOrg.addActionListener(e -> editSelectedOrganization());
        JButton btnDeleteOrg = new JButton("Delete Organization");
        btnDeleteOrg.addActionListener(e -> deleteSelectedOrganization());
        orgAction.add(btnAddOrg);
        orgAction.add(btnEditOrg);
        orgAction.add(btnDeleteOrg);
        orgPanel.add(orgAction, BorderLayout.SOUTH);
        tabbedPane.addTab("Organizations", orgPanel);

        // Users tab
        JPanel usersPanel = new JPanel(new BorderLayout());
        tblUsers = new JTable();
        usersPanel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);
        JPanel usersAction = new JPanel();
        JButton btnAddUser = new JButton("Add User");
        btnAddUser.addActionListener(e -> addUser());
        JButton btnEditUser = new JButton("Edit User");
        btnEditUser.addActionListener(e -> editSelectedUser());
        JButton btnDeleteUser = new JButton("Delete User");
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
        usersAction.add(btnAddUser);
        usersAction.add(btnEditUser);
        usersAction.add(btnDeleteUser);
        usersPanel.add(usersAction, BorderLayout.SOUTH);
        tabbedPane.addTab("Users", usersPanel);

        // Requests tab
        JPanel requestsPanel = new JPanel(new BorderLayout());
        tblRequests = new JTable();
        requestsPanel.add(new JScrollPane(tblRequests), BorderLayout.CENTER);
        JPanel requestsAction = new JPanel();
        JButton btnNewRequest = new JButton("New Request");
        btnNewRequest.addActionListener(e -> createNewRequest());
        JButton btnUpdateStatus = new JButton("Update Status");
        btnUpdateStatus.addActionListener(e -> updateSelectedRequestStatus());
        
        // NEW DELIVERY REQUEST BUTTON
        JButton btnRequestDelivery = new JButton("Request Delivery Service");
        btnRequestDelivery.addActionListener(e -> {
            WorkRequest wr = new WorkRequest();
            wr.setType("DeliveryAssignment");
            wr.setSenderEnterpriseId(enterpriseId);
            // Prefer a dynamic lookup for the Quick Delivery Service enterprise id so we don't assume a static id
            int quickDeliveryId = -1; // sentinel if not found
            java.util.List<model.Enterprise> allEnts = system.getAllEnterprises();
            for (model.Enterprise ent : allEnts) {
                if (ent.getName() != null && "Quick Delivery Service".equalsIgnoreCase(ent.getName().trim())) {
                    quickDeliveryId = ent.getId();
                    break;
                }
            }
            if (quickDeliveryId == -1) {
                JOptionPane.showMessageDialog(this, "Quick Delivery Service enterprise not found. Please ensure it is registered in the system.", "Delivery Service Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            wr.setReceiverEnterpriseId(quickDeliveryId);
            wr.setStatus("New");
            wr.setMessage("Need delivery personnel for pending orders");
            try {
                system.createWorkRequest(wr);
                populateRequests();
                JOptionPane.showMessageDialog(this, "Delivery request sent to Quick Delivery Service!");
            } catch (IllegalStateException ise) {
                // Unauthorized to create a request
                JOptionPane.showMessageDialog(this, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException iae) {
                // Validation error when constructing the request
                JOptionPane.showMessageDialog(this, "Invalid delivery request: " + iae.getMessage(), "Invalid Request", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException re) {
                // Generic runtime fallback (should be rare): surface a user-friendly message
                JOptionPane.showMessageDialog(this, "Failed to send delivery request: " + re.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        requestsAction.add(btnNewRequest);
        requestsAction.add(btnUpdateStatus);
        requestsAction.add(btnRequestDelivery); // Add the new button
        requestsPanel.add(requestsAction, BorderLayout.SOUTH);
        tabbedPane.addTab("Work Requests", requestsPanel);

        // Reports tab (simple counts)
        JPanel reportPanel = new JPanel(new GridLayout(4, 1));
        reportPanel.add(new JLabel("Enterprise Reports (summary): "));
        reportPanel.add(new JLabel(" - Organizations: "));
        reportPanel.add(new JLabel(" - Users: "));
        reportPanel.add(new JLabel(" - Orders: "));
        tabbedPane.addTab("Reports", reportPanel);

        // Add a split pane: left is the management area, right is a details pane
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, detailsScroll);
        split.setResizeWeight(0.75);
        add(split, BorderLayout.CENTER);

        // Configure tables and selection listeners to show details
        tblOrganizations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblRequests.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        tblOrganizations.getSelectionModel().addListSelectionListener(e -> {
            int r = tblOrganizations.getSelectedRow();
            if (r >= 0) {
                String orgDetails = String.format("""
                        Organization Details:

                        ID: %s
                        Name: %s
                        Type: %s
                        """,
                        tblOrganizations.getValueAt(r, 0),
                        tblOrganizations.getValueAt(r, 1),
                        tblOrganizations.getValueAt(r, 2));
                detailsArea.setText(orgDetails);
            }
        });

        tblUsers.getSelectionModel().addListSelectionListener(e -> {
            int r = tblUsers.getSelectedRow();
            if (r >= 0) {
                String userDetails = String.format("""
                        User Details:

                        ID: %s
                        Username: %s
                        Name: %s
                        Role: %s
                        Phone: %s
                        Email: %s
                        Organization ID: %s
                        """,
                        tblUsers.getValueAt(r, 0),
                        tblUsers.getValueAt(r, 1),
                        tblUsers.getValueAt(r, 2),
                        tblUsers.getValueAt(r, 3),
                        tblUsers.getValueAt(r, 4),
                        tblUsers.getValueAt(r, 5),
                        tblUsers.getValueAt(r, 6));
                detailsArea.setText(userDetails);
            }
        });

        tblRequests.getSelectionModel().addListSelectionListener(e -> {
            int r = tblRequests.getSelectedRow();
            if (r >= 0) {
                String requestDetails = String.format("""
                        Work Request Details:

                        ID: %s
                        Type: %s
                        Sender: %s
                        Receiver: %s
                        Order: %s
                        Status: %s
                        Created: %s
                        """,
                        tblRequests.getValueAt(r, 0),
                        tblRequests.getValueAt(r, 1),
                        tblRequests.getValueAt(r, 2),
                        tblRequests.getValueAt(r, 3),
                        tblRequests.getValueAt(r, 4),
                        tblRequests.getValueAt(r, 5),
                        tblRequests.getValueAt(r, 6));
                detailsArea.setText(requestDetails);
            }
        });
    }

    private void populateOrganizations() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Type");

        List<Organization> orgs = system.getOrganizations(enterpriseId);
        for (Organization org : orgs) {
            model.addRow(new Object[] { org.getId(), org.getName(), org.getType() });
        }
        tblOrganizations.setModel(model);
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

        // Show users that belong to organizations in this enterprise
        List<Organization> orgs = system.getOrganizations(enterpriseId);
        List<Integer> orgIds = orgs.stream().map(Organization::getId).collect(Collectors.toList());
        List<User> users = system.getAllUsers().stream().filter(u -> orgIds.contains(u.getOrganizationId())).collect(Collectors.toList());
        for (User u : users) {
            model.addRow(new Object[] { u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getPhone(), u.getEmail(), u.getOrganizationId() });
        }
        tblUsers.setModel(model);
    }

    private void populateRequests() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Type");
        model.addColumn("Sender");
        model.addColumn("Receiver");
        model.addColumn("Related Order");
        model.addColumn("Status");
        model.addColumn("Created At");

        List<WorkRequest> requests;
        try {
            requests = system.getWorkRequestsForEnterprise(enterpriseId);
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            tblRequests.setModel(model);
            return;
        }
        for (WorkRequest r : requests) {
            model.addRow(new Object[] { r.getId(), r.getType(), r.getSenderEnterpriseId(), r.getReceiverEnterpriseId(), r.getRelatedOrderId(), r.getStatus(), r.getCreatedAt() });
        }
        tblRequests.setModel(model);
    }

    private void addOrganization() {
        String name = JOptionPane.showInputDialog(this, "Organization Name:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Organization name is required");
            return;
        }
        String[] types = {"Admin", "Kitchen", "DeliveryTeam", "Support"};
        String type = (String) JOptionPane.showInputDialog(this, "Type:", "Type", JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (type == null) return;
        try {
            system.createOrganization(name, type, enterpriseId);
            populateOrganizations();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedOrganization() {
        int selectedRow = tblOrganizations.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblOrganizations.getValueAt(selectedRow, 0);
            String name = (String) tblOrganizations.getValueAt(selectedRow, 1);
            String type = (String) tblOrganizations.getValueAt(selectedRow, 2);
            String newName = JOptionPane.showInputDialog(this, "Enter new name:", name);
            if (newName == null || newName.trim().isEmpty()) return;
            String[] types = {"Admin", "Kitchen", "DeliveryTeam", "Support"};
            String newType = (String) JOptionPane.showInputDialog(this, "Select Type:", "Type", JOptionPane.QUESTION_MESSAGE, null, types, type);
            if (newType == null) return;
            Organization o = new Organization();
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

    private void deleteSelectedOrganization() {
        int selectedRow = tblOrganizations.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblOrganizations.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Organization ID: " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    system.deleteOrganization(id);
                    populateOrganizations();
                    populateUsers();
                } catch (IllegalStateException ise) {
                    JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an organization to delete");
        }
    }

    private void addUser() {
        String username = JOptionPane.showInputDialog(this, "Username:");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Username is required");
            return;
        }
        String trimmedUsername = username.trim();
        boolean exists = system.getAllUsers().stream().anyMatch(u -> trimmedUsername.equalsIgnoreCase(u.getUsername() == null ? "" : u.getUsername().trim()));
        if (exists) {
            JOptionPane.showMessageDialog(mainFrame, "Username already exists");
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Password:");
        if (password == null) return;
        String name = JOptionPane.showInputDialog(this, "Full Name:");
        String phone = JOptionPane.showInputDialog(this, "Phone:");
        String email = JOptionPane.showInputDialog(this, "Email:");

        // Choose role and organization
        String[] roles = {"EnterpriseAdmin", "Manager", "Customer", "DeliveryMan"};
        String role = (String) JOptionPane.showInputDialog(this, "Role:", "Role", JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
        if (role == null) return;

        // Choose organization from enterprise organizations
        List<Organization> orgs = system.getOrganizations(enterpriseId);
        String[] orgOptions = orgs.stream().map(o -> o.getId() + ": " + o.getName()).toArray(String[]::new);
        if (orgOptions.length == 0) {
            JOptionPane.showMessageDialog(this, "No organizations exist for this enterprise. Create one first.");
            return;
        }
        String sel = (String) JOptionPane.showInputDialog(this, "Organization:", "Organization", JOptionPane.QUESTION_MESSAGE, null, orgOptions, orgOptions[0]);
        if (sel == null) return;
        int orgId = Integer.parseInt(sel.split(":")[0].trim());

        model.User u = new model.User();
        u.setUsername(trimmedUsername);
        u.setPassword(password);
        u.setRole(role);
        u.setName(name);
        u.setPhone(phone);
        u.setEmail(email);
        u.setOrganizationId(orgId);

        try {
            system.createUser(u);
            populateUsers();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Error creating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            String[] roles = {"EnterpriseAdmin", "Manager", "Customer", "DeliveryMan"};
            String newRole = (String) JOptionPane.showInputDialog(this, "Select Role:", "Role", JOptionPane.QUESTION_MESSAGE, null, roles, role);
            if (newRole == null) return;

            // Choose organization
            List<Organization> orgs = system.getOrganizations(enterpriseId);
            String[] orgOptions = orgs.stream().map(o -> o.getId() + ": " + o.getName()).toArray(String[]::new);
            String defaultOrg = Arrays.stream(orgOptions).filter(s -> s.startsWith(orgId + ":")).findFirst().orElse(orgOptions[0]);
            String sel = (String) JOptionPane.showInputDialog(this, "Organization:", "Organization", JOptionPane.QUESTION_MESSAGE, null, orgOptions, defaultOrg);
            if (sel == null) return;
            int newOrgId = Integer.parseInt(sel.split(":")[0].trim());

            model.User u = new model.User();
            u.setId(id);
            u.setUsername(username);
            // Prompt for new password — leaving blank keeps existing password
            model.User existing = system.getAllUsers().stream().filter(x -> x.getId() == id).findFirst().orElse(null);
            if (existing == null) {
                JOptionPane.showMessageDialog(this, "Existing user not found. The user may have been deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String newPassword = JOptionPane.showInputDialog(this, "New Password (leave blank to keep existing):");
            if (newPassword == null) {
                // User cancelled — do nothing
                return;
            }
            if (newPassword.trim().isEmpty()) {
                u.setPassword(existing.getPassword());
            } else {
                u.setPassword(newPassword);
            }
            u.setName(newName);
            u.setRole(newRole);
            u.setPhone(phone);
            u.setEmail(email);
            u.setOrganizationId(newOrgId);
            try {
                system.updateUser(u);
                populateUsers();
            } catch (IllegalStateException ise) {
                JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to edit");
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblUsers.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete User ID: " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    system.deleteUser(id);
                    populateUsers();
                } catch (IllegalStateException ise) {
                    JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete");
        }
    }

    private void createNewRequest() {
        // Choose request type
        String[] types = {"OrderRequest", "SupplyRequest", "DeliveryAssignment"};
        String type = (String) JOptionPane.showInputDialog(this, "Request Type:", "Type", JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (type == null) return;

        // Choose a receiver enterprise
        List<model.Enterprise> enterprises = system.getAllEnterprises();
        String[] entOptions = enterprises.stream().map(e -> e.getId() + ": " + e.getName()).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Receiver Enterprise:", "Receiver", JOptionPane.QUESTION_MESSAGE, null, entOptions, entOptions[0]);
        if (sel == null) return;
        int receiverId = Integer.parseInt(sel.split(":")[0].trim());

        String message = JOptionPane.showInputDialog(this, "Message:");
        Integer relatedOrderId = null;
        if ("DeliveryAssignment".equals(type) || "OrderRequest".equals(type)) {
            // collect orders within this enterprise
            List<Organization> orgs = system.getOrganizations(enterpriseId);
            java.util.List<Integer> orderIds = new java.util.ArrayList<>();
            for (Organization o : orgs) {
                List<model.Order> orders = system.getOrdersForRestaurant(o.getId());
                for (model.Order or : orders) orderIds.add(or.getId());
            }
            if (!orderIds.isEmpty()) {
                String[] orderOptions = orderIds.stream().map(Object::toString).toArray(String[]::new);
                String selOrder = (String) JOptionPane.showInputDialog(this, "Related Order:", "Order", JOptionPane.QUESTION_MESSAGE, null, orderOptions, orderOptions[0]);
                relatedOrderId = (selOrder == null) ? null : Integer.valueOf(selOrder);
            }
        }

        model.WorkRequest wr = new model.WorkRequest();
        wr.setType(type);
        wr.setSenderEnterpriseId(enterpriseId);
        wr.setReceiverEnterpriseId(receiverId);
        wr.setMessage(message);
        wr.setRelatedOrderId(relatedOrderId);
        wr.setStatus("New");

        try {
            system.createWorkRequest(wr);
            populateRequests();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedRequestStatus() {
        int selectedRow = tblRequests.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tblRequests.getValueAt(selectedRow, 0);
            String[] statuses = {"New", "InProgress", "Completed", "Rejected"};
            String newStatus = (String) JOptionPane.showInputDialog(this, "Select status:", "Status", JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
            if (newStatus == null) return;
            try {
                system.updateWorkRequestStatus(id, newStatus);
                populateRequests();
            } catch (IllegalStateException ise) {
                JOptionPane.showMessageDialog(mainFrame, "Unauthorized: " + ise.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a request to update");
        }
    }
}
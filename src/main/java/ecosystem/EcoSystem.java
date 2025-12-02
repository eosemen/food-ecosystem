package ecosystem;

import java.math.BigDecimal;
import java.util.List;

import dao.EnterpriseDAO;
import dao.MenuItemDAO;
import dao.OrderDAO;
import dao.OrderItemDAO;
import dao.OrganizationDAO;
import dao.UserDAO;
import dao.WorkRequestDAO;
import model.Enterprise;
import model.MenuItem;
import model.Order;
import model.OrderItem;
import model.Organization;
import model.User;

public class EcoSystem {
    private static EcoSystem instance;
    private final UserDAO userDAO;
    private final EnterpriseDAO enterpriseDAO;
    private final OrganizationDAO organizationDAO;
    private final OrderDAO orderDAO;
    private final MenuItemDAO menuItemDAO;
    private final OrderItemDAO orderItemDAO;
    private final WorkRequestDAO workRequestDAO;

    private User currentUser;

    private EcoSystem() {
        userDAO = new UserDAO();
        enterpriseDAO = new EnterpriseDAO();
        organizationDAO = new OrganizationDAO();
        orderDAO = new OrderDAO();
        menuItemDAO = new MenuItemDAO();
        orderItemDAO = new OrderItemDAO();
        workRequestDAO = new WorkRequestDAO();
        // Ensure DB has at least the basic seed data for demo usage
        seedDefaultDataIfEmpty();
    }

    private void seedDefaultDataIfEmpty() {
        try {
            // Always ensure our demo users exist with expected default credentials (for local demo environments)
            List<model.User> existingUsers = userDAO.getAllUsers();
            if (existingUsers.isEmpty()) {
                // Create default enterprises
                enterpriseDAO.createEnterprise(new Enterprise(0, "Boston Food Delivery", "Restaurant"));
                enterpriseDAO.createEnterprise(new Enterprise(0, "New York Eats", "Restaurant"));
                enterpriseDAO.createEnterprise(new Enterprise(0, "Quick Delivery Service", "Delivery"));

                // Lookup enterprise ids
                java.util.List<Enterprise> ents = enterpriseDAO.getAllEnterprises();
                int bostonEntId = -1;
                int nyEntId = -1;
                int quickEntId = -1;
                for (Enterprise e : ents) {
                    switch (e.getName()) {
                        case "Boston Food Delivery" -> bostonEntId = e.getId();
                        case "New York Eats" -> nyEntId = e.getId();
                        case "Quick Delivery Service" -> quickEntId = e.getId();
                    }
                }

                // Create organizations for those enterprises
                if (bostonEntId > 0) {
                    organizationDAO.createOrganization(new Organization(0, "Kitchen Staff", "Kitchen", bostonEntId));
                    organizationDAO.createOrganization(new Organization(0, "Admin Team", "Admin", bostonEntId));
                }
                if (nyEntId > 0) {
                    organizationDAO.createOrganization(new Organization(0, "Delivery Fleet", "DeliveryTeam", nyEntId));
                }
                if (quickEntId > 0) {
                    organizationDAO.createOrganization(new Organization(0, "Dispatch Team", "DeliveryTeam", quickEntId));
                }

                // Lookup org ids
                java.util.List<Organization> orgs = organizationDAO.getAllOrganizations();
                int adminTeamId = -1;
                int kitchenId = -1;
                int deliveryFleetId = -1;
                int dispatchId = -1;
                for (Organization o : orgs) {
                    switch (o.getName()) {
                        case "Admin Team" -> adminTeamId = o.getId();
                        case "Kitchen Staff" -> kitchenId = o.getId();
                        case "Delivery Fleet" -> deliveryFleetId = o.getId();
                        case "Dispatch Team" -> dispatchId = o.getId();
                    }
                }

                // Create default users
                // Use direct DAO to avoid permission checks as system is not yet initialized
                userDAO.createUser(new User(0, "sysadmin", "sysadmin", "SystemAdmin", "System Administrator", null, null, 0));
                if (adminTeamId > 0) userDAO.createUser(new User(0, "manager1", "manager1", "Manager", "Jane Smith", "555-1111", "jane@example.com", adminTeamId));
                // Customer with no organization
                userDAO.createUser(new User(0, "customer1", "customer1", "Customer", "John Doe", "555-2222", "john@example.com", 0));
                if (deliveryFleetId > 0) userDAO.createUser(new User(0, "delivery1", "delivery1", "DeliveryMan", "Bob Driver", "555-3333", "bob@example.com", deliveryFleetId));
                if (adminTeamId > 0) userDAO.createUser(new User(0, "entadmin1", "entadmin1", "EnterpriseAdmin", "Boston Admin", "555-5555", "admin@bostonfd.com", adminTeamId));

                // Menu Items for the 'Boston' restaurant (use kitchenId as restaurant id)
                if (kitchenId > 0) {
                    menuItemDAO.createMenuItem(new MenuItem(0, kitchenId, "Burger", new java.math.BigDecimal("9.99"), "Beef burger with fries"));
                    menuItemDAO.createMenuItem(new MenuItem(0, kitchenId, "Pizza", new java.math.BigDecimal("12.99"), "Cheese pizza (Large)"));
                }
                if (dispatchId > 0) {
                    menuItemDAO.createMenuItem(new MenuItem(0, dispatchId, "Sushi Platter", new java.math.BigDecimal("24.99"), "Fresh assorted sushi"));
                }

                // Create a couple of demo orders and order items linked to the created users
                // Find the customer id for customer1 and restaurant id
                java.util.List<User> created = userDAO.getAllUsers();
                int customer1Id = -1;
                for (User u : created) {
                    if (u.getUsername() != null && "customer1".equalsIgnoreCase(u.getUsername().trim())) { customer1Id = u.getId(); }
                }

                if (customer1Id > 0 && kitchenId > 0) {
                    Order o1 = new Order();
                    o1.setCustomerId(customer1Id);
                    o1.setRestaurantId(kitchenId);
                    o1.setStatus("Placed");
                    o1.setDeliveryAddress("123 Main St");
                    o1.setComment("No onions");
                    orderDAO.createOrder(o1);
                    OrderItem oi1 = new OrderItem();
                    oi1.setOrderId(o1.getId());
                    oi1.setMenuItemName("Burger");
                    oi1.setPrice(new java.math.BigDecimal("9.99"));
                    oi1.setQuantity(1);
                    orderItemDAO.createOrderItem(oi1);
                }
            }

            // Optionally, ensure seeded demo users exist with expected (demo) passwords and roles.
            // This will update the password if it differs — only enabled when DEMO_RESET env var or system property is set.
            boolean demoReset = false;
            String demoResetEnv = System.getenv("DEMO_RESET");
            if (demoResetEnv != null && (demoResetEnv.equals("1") || demoResetEnv.equalsIgnoreCase("true"))) demoReset = true;
            String demoResetProp = System.getProperty("DEMO_RESET");
            if (demoResetProp != null && (demoResetProp.equals("1") || demoResetProp.equalsIgnoreCase("true"))) demoReset = true;
            if (demoReset) {
            java.util.Map<String, User> demoUsers = new java.util.HashMap<>();
            int computedAdminTeamId = -1;
            int computedDeliveryFleetId = -1;
            java.util.List<Organization> orgs = organizationDAO.getAllOrganizations();
            for (Organization o : orgs) {
                if ("Admin Team".equals(o.getName())) computedAdminTeamId = o.getId();
                if ("Delivery Fleet".equals(o.getName())) computedDeliveryFleetId = o.getId();
            }
            demoUsers.put("sysadmin", new User(0, "sysadmin", "sysadmin", "SystemAdmin", "System Administrator", null, null, 0));
            demoUsers.put("manager1", new User(0, "manager1", "manager1", "Manager", "Jane Smith", "555-1111", "jane@example.com", computedAdminTeamId > 0 ? computedAdminTeamId : 0));
            demoUsers.put("customer1", new User(0, "customer1", "customer1", "Customer", "John Doe", "555-2222", "john@example.com", 0));
            demoUsers.put("delivery1", new User(0, "delivery1", "delivery1", "DeliveryMan", "Bob Driver", "555-3333", "bob@example.com", computedDeliveryFleetId > 0 ? computedDeliveryFleetId : 0));
            demoUsers.put("entadmin1", new User(0, "entadmin1", "entadmin1", "EnterpriseAdmin", "Boston Admin", "555-5555", "admin@bostonfd.com", computedAdminTeamId > 0 ? computedAdminTeamId : 0));

            List<User> allUsers = userDAO.getAllUsers();
            for (java.util.Map.Entry<String, User> e : demoUsers.entrySet()) {
                String uname = e.getKey();
                User expectedUser = e.getValue();
                User found = null;
                for (User u : allUsers) {
                    if (u.getUsername() != null && u.getUsername().trim().equalsIgnoreCase(uname)) {
                        found = u;
                        break;
                    }
                }
                if (found == null) {
                    // Create missing demo user
                    userDAO.createUser(expectedUser);
                } else {
                    // If user exists but password doesn't match expected, update password to expected (demo convenience)
                    String stored = found.getPassword() == null ? "" : found.getPassword();
                    String expectedPw = expectedUser.getPassword() == null ? "" : expectedUser.getPassword();
                    if (!stored.equals(expectedPw)) {
                        // Update user with expected password
                        found.setPassword(expectedPw);
                        found.setUsername(found.getUsername().trim());
                        found.setName(expectedUser.getName());
                        found.setRole(expectedUser.getRole());
                        // Use DAO update directly as system might not yet be set up for permission checks
                        userDAO.updateUser(found);
                    }
                }
            }
            }
        } catch (RuntimeException re) {
            // Any runtime exception during bootstrapping should not crash the application; log for debugging
            System.err.println("Failed to seed default data: " + re.getMessage());
        }
    }

    public static EcoSystem getInstance() {
        if (instance == null) {
            instance = new EcoSystem();
        }
        return instance;
    }

    // User Management
    public User login(String username, String password) {
        if (username == null || password == null) return null;
        String trimmedUsername = username.trim();
        String trimmedPassword = password.trim();
        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) return null;
        User user = userDAO.authenticate(trimmedUsername, trimmedPassword);
        if (user != null) {
            currentUser = user;
        }
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void createUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        if (user.getUsername() == null || user.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }

        List<User> existingUsers = getAllUsers();
        boolean userExists = existingUsers.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(user.getUsername()));
        if (userExists) throw new IllegalArgumentException("Username already exists");

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }

        Integer currentEntId = getCurrentUserEnterpriseId();
        if (isSystemAdmin() ||
            (isEnterpriseAdmin() &&
             currentEntId != null &&
             currentEntId.equals(getOrganizationEnterpriseId(user.getOrganizationId())))) {
            userDAO.createUser(user);
        } else {
            throw new IllegalStateException("Unauthorized to create user for this organization");
        }
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    public void updateUser(User user) {
        Integer currentEntId = getCurrentUserEnterpriseId();
        if (isSystemAdmin() || (isEnterpriseAdmin() && currentEntId != null && currentEntId.equals(getOrganizationEnterpriseId(user.getOrganizationId())))) {
            userDAO.updateUser(user);
        } else {
            throw new IllegalStateException("Unauthorized to update user for this organization");
        }
    }

    public void deleteUser(int userId) {
        // Check user's organization enterprise
        User user = userDAO.getUserById(userId);
        Integer currentEntId = getCurrentUserEnterpriseId();
        if (user == null) return;
        if (isSystemAdmin() || (isEnterpriseAdmin() && currentEntId != null && currentEntId.equals(getOrganizationEnterpriseId(user.getOrganizationId())))) {
            userDAO.deleteUser(userId);
        } else {
            throw new IllegalStateException("Unauthorized to delete user from this organization");
        }
    }

    // Enterprise Management
    public void createEnterprise(String name, String type) {
        enterpriseDAO.createEnterprise(new Enterprise(0, name, type));
    }

    public void deleteEnterprise(int enterpriseId) {
        enterpriseDAO.deleteEnterprise(enterpriseId);
    }

    public List<Enterprise> getAllEnterprises() {
        return enterpriseDAO.getAllEnterprises();
    }
    public void updateEnterprise(Enterprise enterprise) {
        enterpriseDAO.updateEnterprise(enterprise);
    }

    // Organization Management
    public void createOrganization(String name, String type, int enterpriseId) {
        if (isSystemAdmin() || (isEnterpriseAdmin() && getCurrentUserEnterpriseId() != null && getCurrentUserEnterpriseId().equals(enterpriseId))) {
            organizationDAO.createOrganization(new Organization(0, name, type, enterpriseId));
        } else {
            throw new IllegalStateException("Unauthorized to create organization for this enterprise");
        }
    }

    public List<Organization> getOrganizations(int enterpriseId) {
        return organizationDAO.getOrganizationsByEnterpriseId(enterpriseId);
    }

    public List<Organization> getAllOrganizations() {
        return organizationDAO.getAllOrganizations();
    }

    public void deleteOrganization(int organizationId) {
        Organization org = organizationDAO.getOrganizationById(organizationId);
        if (org == null) return;
        if (isSystemAdmin() || (isEnterpriseAdmin() && getCurrentUserEnterpriseId() != null && getCurrentUserEnterpriseId().equals(org.getEnterpriseId()))) {
            organizationDAO.deleteOrganization(organizationId);
        } else {
            throw new IllegalStateException("Unauthorized to delete organization for this enterprise");
        }
    }
    public void updateOrganization(Organization org) {
        if (organizationDAO.getOrganizationById(org.getId()) == null) return;
        if (isSystemAdmin() || (isEnterpriseAdmin() && getCurrentUserEnterpriseId() != null && getCurrentUserEnterpriseId().equals(org.getEnterpriseId()))) {
            organizationDAO.updateOrganization(org);
        } else {
            throw new IllegalStateException("Unauthorized to update organization for this enterprise");
        }
    }

    // Order Management
    public void placeOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getCustomerId() <= 0) throw new IllegalArgumentException("Invalid customer ID");
        if (order.getRestaurantId() <= 0) throw new IllegalArgumentException("Invalid restaurant ID");

        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required");
        }

        if (order.getDeliveryAddress().length() < 10) {
            throw new IllegalArgumentException("Please provide a complete delivery address");
        }

        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus("Placed");
        }

        if (order.getOrderDate() == null) {
            order.setOrderDate(new java.util.Date());
        }

        orderDAO.createOrder(order);
    }

    public List<Order> getOrdersForCustomer(int customerId) {
        return orderDAO.getOrdersByCustomer(customerId);
    }

    public List<Order> getOrdersForRestaurant(int restaurantId) {
        return orderDAO.getOrdersByRestaurant(restaurantId);
    }

    public List<Order> getOrdersForDeliveryMan(int deliveryManId) {
        return orderDAO.getOrdersByDeliveryMan(deliveryManId);
    }

    public void updateOrderStatus(int orderId, String status, int deliveryManId) {
        orderDAO.updateOrderStatus(orderId, status, deliveryManId);
    }

    // Menu Management
    public void addMenuItem(MenuItem item) {
        if (item == null) throw new IllegalArgumentException("Menu item cannot be null");

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }

        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (item.getPrice().compareTo(new BigDecimal("1000")) > 0) {
            throw new IllegalArgumentException("Price cannot exceed $1000");
        }

        if (item.getRestaurantId() <= 0) {
            throw new IllegalArgumentException("Invalid restaurant ID");
        }

        menuItemDAO.createMenuItem(item);
    }

    public void deleteMenuItem(int menuItemId) {
        menuItemDAO.deleteMenuItem(menuItemId);
    }
    public void updateMenuItem(MenuItem item) {
        menuItemDAO.updateMenuItem(item);
    }

    public void createOrderItem(model.OrderItem item) {
        orderItemDAO.createOrderItem(item);
    }

    public void deleteOrderItem(int id) {
        orderItemDAO.deleteOrderItem(id);
    }

    public void updateOrderItem(model.OrderItem item) {
        orderItemDAO.updateOrderItem(item);
    }

    public java.util.List<model.OrderItem> getOrderItems(int orderId) {
        return orderItemDAO.getOrderItemsByOrderId(orderId);
    }

    public List<MenuItem> getMenu(int restaurantId) {
        return menuItemDAO.getMenuItemsByRestaurant(restaurantId);
    }

    // Work Requests
    public void createWorkRequest(model.WorkRequest wr) {
        // Sender must be the enterprise of the current user or SystemAdmin
        Integer currentEntId = getCurrentUserEnterpriseId();
        if (isSystemAdmin() || (isEnterpriseAdmin() && currentEntId != null && currentEntId.equals(wr.getSenderEnterpriseId()))) {
            workRequestDAO.createWorkRequest(wr);
        } else {
            throw new IllegalStateException("Unauthorized to create work request for this enterprise");
        }
    }

    public java.util.List<model.WorkRequest> getWorkRequestsForEnterprise(int enterpriseId) {
        // Show both sent and received? For simplicity show received requests for enterprise
        if (isSystemAdmin() || (isEnterpriseAdmin() && getCurrentUserEnterpriseId() != null && getCurrentUserEnterpriseId().equals(enterpriseId))) {
            return workRequestDAO.getWorkRequestsByReceiver(enterpriseId);
        } else {
            throw new IllegalStateException("Unauthorized to view work requests for this enterprise");
        }
    }

    public java.util.List<model.WorkRequest> getWorkRequestsSentByEnterprise(int enterpriseId) {
        return workRequestDAO.getWorkRequestsBySender(enterpriseId);
    }

    public void updateWorkRequestStatus(int id, String status) {
        model.WorkRequest wr = workRequestDAO.getWorkRequest(id);
        if (wr == null) return;
        Integer currentEntId = getCurrentUserEnterpriseId();
        // Only SystemAdmin or receiver enterprise may update status
        if (isSystemAdmin() || (isEnterpriseAdmin() && currentEntId != null && currentEntId.equals(wr.getReceiverEnterpriseId()))) {
            workRequestDAO.updateWorkRequestStatus(id, status);
        } else {
            throw new IllegalStateException("Unauthorized to update this work request");
        }
    }

    public boolean isSystemAdmin() { return currentUser != null && "SystemAdmin".equals(currentUser.getRole()); }

    public boolean isEnterpriseAdmin() { return currentUser != null && "EnterpriseAdmin".equals(currentUser.getRole()); }

    public Integer getCurrentUserEnterpriseId() {
        if (currentUser == null) return null;
        int orgId = currentUser.getOrganizationId();
        if (orgId <= 0) return null;
        Organization org = organizationDAO.getOrganizationById(orgId);
        return org == null ? null : org.getEnterpriseId();
    }

    private Integer getOrganizationEnterpriseId(int orgId) {
        Organization org = organizationDAO.getOrganizationById(orgId);
        return org == null ? null : org.getEnterpriseId();
    }
}

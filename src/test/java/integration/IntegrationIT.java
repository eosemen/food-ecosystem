package integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import dao.EnterpriseDAO;
import dao.MenuItemDAO;
import model.Enterprise;
import model.MenuItem;

public class IntegrationIT {
    // Use a class-level Testcontainers MySQL container - it will start/stop automatically
    @SuppressWarnings({"resource", "unused"})
    @org.junit.ClassRule
    public static org.testcontainers.containers.MySQLContainer<?> mysql = new org.testcontainers.containers.MySQLContainer<>(org.testcontainers.utility.DockerImageName.parse("mysql:8.0")).withDatabaseName("testdb").withUsername("test").withPassword("test");

    private void runSqlScript(String jdbcUrl, String user, String pass, File script) throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass)) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(script))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            // Replace DB name placeholder if schema contains 'food_delivery_db'
            String scriptContent = sb.toString().replaceAll("food_delivery_db", mysql.getDatabaseName());
            String[] statements = scriptContent.split(";\\s*\\n");
            for (String stmt : statements) {
                if (stmt.trim().isEmpty()) continue;
                try (Statement s = conn.createStatement()) {
                    s.execute(stmt);
                }
            }
        }
    }

    @Test
    public void testEnterpriseAndMenuItemCrud() throws Exception {
        mysql.start();
        String jdbcUrl = mysql.getJdbcUrl();
        String user = mysql.getUsername();
        String pass = mysql.getPassword();

        // Apply schema
        File script = new File("db/schema.sql");
        runSqlScript(jdbcUrl, user, pass, script);

        // Point MySQLConnection to test container
        System.setProperty("db.url", jdbcUrl);
        System.setProperty("db.user", user);
        System.setProperty("db.password", pass);

        // Test Enterprise CRUD
        EnterpriseDAO enterpriseDAO = new EnterpriseDAO();
        List<Enterprise> before = enterpriseDAO.getAllEnterprises();
        Enterprise ent = new Enterprise(0, "Test Enterprise", "Restaurant");
        enterpriseDAO.createEnterprise(ent);
        List<Enterprise> afterCreate = enterpriseDAO.getAllEnterprises();
        assertTrue(afterCreate.size() > before.size());

        // Update the enterprise (fetch last created)
        Enterprise created = afterCreate.get(afterCreate.size()-1);
        created.setName("Test Enterprise Updated");
        enterpriseDAO.updateEnterprise(created);
        List<Enterprise> afterUpdate = enterpriseDAO.getAllEnterprises();
        boolean foundUpdated = afterUpdate.stream().anyMatch(e -> "Test Enterprise Updated".equals(e.getName()));
        assertTrue(foundUpdated);

        // Menu item CRUD
        MenuItemDAO menuItemDAO = new MenuItemDAO();
        MenuItem mi = new MenuItem(0, created.getId(), "Integration Test Item", new java.math.BigDecimal("1.23"), "Desc");
        menuItemDAO.createMenuItem(mi);
        List<MenuItem> menu = menuItemDAO.getMenuItemsByRestaurant(created.getId());
        assertTrue(menu.stream().anyMatch(m -> "Integration Test Item".equals(m.getName())));
        MenuItem inserted = menu.stream().filter(m -> "Integration Test Item".equals(m.getName())).findFirst().get();
        inserted.setName("Integration Test Item Updated");
        menuItemDAO.updateMenuItem(inserted);
        List<MenuItem> menuAfterUpdate = menuItemDAO.getMenuItemsByRestaurant(created.getId());
        assertTrue(menuAfterUpdate.stream().anyMatch(m -> "Integration Test Item Updated".equals(m.getName())));
        menuItemDAO.deleteMenuItem(inserted.getId());
        List<MenuItem> menuAfterDelete = menuItemDAO.getMenuItemsByRestaurant(created.getId());
        assertFalse(menuAfterDelete.stream().anyMatch(m -> m.getId() == inserted.getId()));
        
        // Test OrderItem CRUD
        dao.OrderItemDAO orderItemDAO = new dao.OrderItemDAO();
        model.OrderItem oi = new model.OrderItem(0, created.getId(), "IntegrationTestItem", new java.math.BigDecimal("5.00"), 2);
        orderItemDAO.createOrderItem(oi);
        java.util.List<model.OrderItem> oit = orderItemDAO.getOrderItemsByOrderId(created.getId());
        assertTrue(oit.size() >= 1);
        model.OrderItem insertedOi = oit.get(0);
        insertedOi.setMenuItemName("IntegrationTestItemUpdated");
        orderItemDAO.updateOrderItem(insertedOi);
        java.util.List<model.OrderItem> oit2 = orderItemDAO.getOrderItemsByOrderId(created.getId());
        assertTrue(oit2.stream().anyMatch(o -> "IntegrationTestItemUpdated".equals(o.getMenuItemName())));
        orderItemDAO.deleteOrderItem(insertedOi.getId());
        
        // WorkRequest CRUD
        dao.WorkRequestDAO wrDao = new dao.WorkRequestDAO();
        // Use existing enterprises: find two enterprises
        EnterpriseDAO enterpriseDAO2 = new EnterpriseDAO();
        List<Enterprise> enterprises = enterpriseDAO2.getAllEnterprises();
        int sender = enterprises.get(0).getId();
        int receiver = enterprises.size() > 1 ? enterprises.get(1).getId() : sender;
        model.WorkRequest wr = new model.WorkRequest();
        wr.setType("DeliveryAssignment");
        wr.setSenderEnterpriseId(sender);
        wr.setReceiverEnterpriseId(receiver);
        wr.setRelatedOrderId(1);
        wr.setStatus("New");
        wr.setMessage("Please deliver this order");
        wrDao.createWorkRequest(wr);

        List<model.WorkRequest> received = wrDao.getWorkRequestsByReceiver(receiver);
        assertTrue(received.stream().anyMatch(r -> "Please deliver this order".equals(r.getMessage())));
        model.WorkRequest createdWr = received.stream().filter(r -> "Please deliver this order".equals(r.getMessage())).findFirst().get();
        wrDao.updateWorkRequestStatus(createdWr.getId(), "InProgress");
        model.WorkRequest afterUpdateWr = wrDao.getWorkRequest(createdWr.getId());
        assertTrue("InProgress".equals(afterUpdateWr.getStatus()));
        wrDao.deleteWorkRequest(createdWr.getId());
        List<model.WorkRequest> afterDelete = wrDao.getWorkRequestsByReceiver(receiver);
        assertFalse(afterDelete.stream().anyMatch(r -> r.getId() == createdWr.getId()));
    }
}

package dao;

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

public class WorkRequestDAOTest {
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
    public void testWorkRequestDaoCrud() throws Exception {
        String jdbcUrl = mysql.getJdbcUrl();
        String user = mysql.getUsername();
        String pass = mysql.getPassword();
        File script = new File("db/schema.sql");
        runSqlScript(jdbcUrl, user, pass, script);
        System.setProperty("db.url", jdbcUrl);
        System.setProperty("db.user", user);
        System.setProperty("db.password", pass);

        WorkRequestDAO wrDao = new WorkRequestDAO();
        dao.EnterpriseDAO enterpriseDAO = new dao.EnterpriseDAO();
        List<model.Enterprise> enterprises = enterpriseDAO.getAllEnterprises();
        int sender = enterprises.get(0).getId();
        int receiver = enterprises.size() > 1 ? enterprises.get(1).getId() : sender;
        model.WorkRequest wr = new model.WorkRequest();
        wr.setType("SupplyRequest");
        wr.setSenderEnterpriseId(sender);
        wr.setReceiverEnterpriseId(receiver);
        wr.setStatus("New");
        wr.setMessage("Supply needed");
        wrDao.createWorkRequest(wr);
        List<model.WorkRequest> received = wrDao.getWorkRequestsByReceiver(receiver);
        assertTrue(received.stream().anyMatch(r -> "Supply needed".equals(r.getMessage())));
        model.WorkRequest created = received.stream().filter(r -> "Supply needed".equals(r.getMessage())).findFirst().get();
        wrDao.updateWorkRequestStatus(created.getId(), "Completed");
        model.WorkRequest afterUpd = wrDao.getWorkRequest(created.getId());
        assertTrue("Completed".equals(afterUpd.getStatus()));
        wrDao.deleteWorkRequest(created.getId());
        List<model.WorkRequest> afterDelete = wrDao.getWorkRequestsByReceiver(receiver);
        assertFalse(afterDelete.stream().anyMatch(r -> r.getId() == created.getId()));
        assertFalse(afterDelete.stream().anyMatch(r -> r.getId() == created.getId()));
    }

    @org.junit.After
    public void cleanup() {
        if (mysql.isRunning()) {
            mysql.stop();
        }
    }
}

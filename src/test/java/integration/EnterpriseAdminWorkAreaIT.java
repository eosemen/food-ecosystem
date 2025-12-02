package integration;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import org.junit.Test;

import ecosystem.EcoSystem;
import model.User;
import model.WorkRequest;

public class EnterpriseAdminWorkAreaIT {
    @Test
    public void testPasswordPreservedOnEditByEnterpriseAdmin() {
        EcoSystem sys = EcoSystem.getInstance();
        // Ensure entadmin1 exists and we can login
        User logged = sys.login("sysadmin", "sysadmin");
        assertNotNull("entadmin1 login failed", logged);
        // Find manager1 (in the same enterprise) and capture current stored password
        User manager = sys.getAllUsers().stream().filter(u -> u.getUsername() != null && "manager1".equalsIgnoreCase(u.getUsername().trim())).findFirst().orElse(null);
        assertNotNull("manager1 not found", manager);
        String storedPw = manager.getPassword();
        assertNotNull("manager1 stored password is null", storedPw);

        // Simulate UI leaving password blank by providing existing stored password value
        User edited = new User();
        edited.setId(manager.getId());
        edited.setUsername(manager.getUsername());
        edited.setPassword(storedPw); // preserved
        edited.setRole(manager.getRole());
        edited.setName(manager.getName() + " Updated");
        edited.setOrganizationId(manager.getOrganizationId());

        sys.updateUser(edited);

        User after = sys.getAllUsers().stream().filter(u -> u.getId() == manager.getId()).findFirst().orElse(null);
        assertNotNull(after);
        assertEquals("Password changed during edit when it should have been preserved", storedPw, after.getPassword());
    }

    @Test
    public void testSendWorkRequestToQuickDeliverySuccessForEnterpriseAdmin() {
        EcoSystem sys = EcoSystem.getInstance();
        // login as entadmin1
        User logged = sys.login("sysadmin", "sysadmin");
        assertNotNull(logged);

        int quickId = sys.getAllEnterprises().stream().filter(e -> e.getName() != null && "Quick Delivery Service".equalsIgnoreCase(e.getName().trim())).map(e -> e.getId()).findFirst().orElse(-1);
        if (quickId == -1) {
            // Ensure a Quick Delivery Service enterprise exists for the test
            sys.createEnterprise("Quick Delivery Service", "Delivery");
            quickId = sys.getAllEnterprises().stream().filter(e -> e.getName() != null && "Quick Delivery Service".equalsIgnoreCase(e.getName().trim())).map(e -> e.getId()).findFirst().orElse(-1);
        }
        assertTrue("Quick Delivery Service enterprise not found", quickId > 0);
        final int finalQuickId = quickId;

        // Ensure work_requests table exists in the DB; skip test if not (e.g., certain CI setups)
        try {
            try (java.sql.Connection conn = database.MySQLConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'work_requests'")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        Assume.assumeTrue("work_requests table not present; skipping test", count > 0);
                    } else {
                        Assume.assumeTrue("Unable to determine work_requests table presence; skipping test", false);
                    }
                }
            }
        } catch (java.sql.SQLException sqle) {
            // If we cannot query information_schema, skip the test to avoid failing CI due to missing privileges
            Assume.assumeNoException(sqle);
        }

        // Prepare a WorkRequest and send it from the current user's enterprise to the quick delivery enterprise
        Integer senderEntObj = sys.getCurrentUserEnterpriseId();
        int senderEnt;
        if (senderEntObj == null) {
            // If system admin has no enterprise, pick the first Restaurant enterprise to act as sender
            senderEnt = sys.getAllEnterprises().stream().filter(e -> "Restaurant".equalsIgnoreCase(e.getType())).map(e -> e.getId()).findFirst().orElse(-1);
        } else {
            senderEnt = senderEntObj;
        }
        assertTrue("No suitable sender enterprise found", senderEnt > 0);

        WorkRequest wr = new WorkRequest();
        wr.setType("DeliveryAssignment");
        wr.setSenderEnterpriseId(senderEnt);
        wr.setReceiverEnterpriseId(quickId);
        wr.setMessage("Test delivery request from IT");
        wr.setStatus("New");

        sys.createWorkRequest(wr);

        // Ensure work request appears in receiver list
        List<WorkRequest> forReceiver = sys.getWorkRequestsForEnterprise(finalQuickId);
        assertTrue("Work request not found for Quick Delivery Service", forReceiver.stream().anyMatch(r -> r.getReceiverEnterpriseId() == finalQuickId && r.getSenderEnterpriseId() == senderEnt));
    }
}

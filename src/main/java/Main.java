public class Main {
    public static void main(String[] args) {
        // This line tells the system to fix passwords on startup
        System.setProperty("DEMO_RESET", "true");
        
        // Run UI in Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ui.MainJFrame().setVisible(true);
        });
    }
}
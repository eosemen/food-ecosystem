package util;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    public static void handle(Component parent, Exception e, String userMessage) {
        LOGGER.log(Level.SEVERE, userMessage, e);

        String displayMessage = userMessage;
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            displayMessage += "\n\nDetails: " + e.getMessage();
        }

        JOptionPane.showMessageDialog(parent,
                displayMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void handleValidation(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmAction(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent,
                message,
                "Confirm Action",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

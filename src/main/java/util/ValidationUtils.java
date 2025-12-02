package util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[\\d\\s\\-\\+\\(\\)]+$");

    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;

        private ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }

    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.invalid("Username is required");
        }
        if (username.length() < 3) {
            return ValidationResult.invalid("Username must be at least 3 characters");
        }
        if (username.length() > 50) {
            return ValidationResult.invalid("Username must be less than 50 characters");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return ValidationResult.invalid("Username can only contain letters, numbers, and underscores");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("Password is required");
        }
        if (password.length() < 6) {
            return ValidationResult.invalid("Password must be at least 6 characters");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.invalid("Invalid email format");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationResult.invalid("Phone number is required");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return ValidationResult.invalid("Invalid phone number format");
        }
        String cleaned = phone.replaceAll("[^\\d]", "");
        if (cleaned.length() < 10 || cleaned.length() > 15) {
            return ValidationResult.invalid("Phone number must be 10–15 digits");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return ValidationResult.invalid("Price is required");
        }
        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                return ValidationResult.invalid("Price must be greater than zero");
            }
            if (price.compareTo(new BigDecimal("10000")) > 0) {
                return ValidationResult.invalid("Price cannot exceed $10,000");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.invalid("Invalid price format");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateDeliveryAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return ValidationResult.invalid("Delivery address is required");
        }
        if (address.length() < 10) {
            return ValidationResult.invalid("Please enter a complete address");
        }
        if (address.length() > 500) {
            return ValidationResult.invalid("Address is too long");
        }
        return ValidationResult.valid();
    }
}

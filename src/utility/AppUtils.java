package utility;

import java.util.regex.Pattern;

/**
 * Small collection of reusable helpers: ID generation and input
 * validation. Kept static/stateless so it can be used from any layer.
 */
public class AppUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Generates a human-readable, sequential complaint ID such as
     * "CMP-1001". The sequence number is supplied by the caller
     * (ComplaintService keeps track of the running counter and
     * persists it via FileManager) so IDs stay unique even across
     * restarts of the application.
     */
    public static String generateComplaintId(int sequence) {
        return "CMP-" + (1000 + sequence);
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    public static boolean isValidTitle(String title) {
        return isNotBlank(title) && title.trim().length() >= 3;
    }

    public static boolean isValidDescription(String description) {
        return isNotBlank(description) && description.trim().length() >= 10;
    }
}

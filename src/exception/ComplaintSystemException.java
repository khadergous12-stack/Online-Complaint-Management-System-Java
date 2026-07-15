package exception;

/**
 * A single custom checked exception used across the system for all
 * business-rule violations (duplicate email, invalid login, complaint
 * not found, unauthorized action, invalid input, file I/O failure, etc).
 *
 * Keeping one exception type with a clear message keeps the console
 * app's error handling simple and consistent, while still
 * demonstrating custom exception design and try/catch usage.
 */
public class ComplaintSystemException extends Exception {
    public ComplaintSystemException(String message) {
        super(message);
    }

    public ComplaintSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}

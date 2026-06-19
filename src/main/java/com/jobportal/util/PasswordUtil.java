package com.jobportal.util;

import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;

/**
 * Secure password utility using BCrypt hashing.
 * All password operations use BCrypt for production-grade security.
 * Phase 1: Legacy password migration required (see migration script).
 */
public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~]");

    /**
     * Hash a password using BCrypt.
     * 
     * @param password Plain text password
     * @return BCrypt-hashed password
     */
    public static String hashPassword(String password) {
        if (password == null)
            return null;
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a plain password against a BCrypt hash.
     * 
     * @param plainPassword  User-provided password
     * @param hashedPassword BCrypt hash from database
     * @return true if password matches
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || plainPassword == null)
            return false;

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate password strength requirements.
     * Requirements: 8+ chars, uppercase, lowercase, digit, special character.
     * 
     * @param password Plain text password to validate
     * @return true if password meets strength requirements
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        return UPPERCASE_PATTERN.matcher(password).find() &&
                LOWERCASE_PATTERN.matcher(password).find() &&
                DIGIT_PATTERN.matcher(password).find() &&
                SPECIAL_CHAR_PATTERN.matcher(password).find();
    }

    /**
     * Get password strength validation error message.
     * 
     * @param password Plain text password
     * @return Error message describing what requirements are missing, or null if
     *         valid
     */
    public static String getPasswordStrengthError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.";
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return "Password must contain at least one uppercase letter (A-Z).";
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return "Password must contain at least one lowercase letter (a-z).";
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            return "Password must contain at least one digit (0-9).";
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return "Password must contain at least one special character (!@#$%^&*...).";
        }
        return null; // Password is valid
    }
}

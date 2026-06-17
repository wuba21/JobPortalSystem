package com.jobportal.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * File-based email logger for Phase 1.
 * Logs email OTPs and notifications to files instead of sending via SMTP.
 * Enables testing without requiring external email service configuration.
 */
public class EmailFileLogger {

    private static final String LOG_DIR = "logs/emails";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        // Ensure log directory exists
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    /**
     * Log an OTP email to file.
     * 
     * @param email Recipient email address
     * @param otp   One-time password
     */
    public static void logOTPEmail(String email, String otp) {
        String message = String.format(
                "[%s] OTP Email sent to: %s\nOTP: %s\n\n",
                LocalDateTime.now().format(DATE_FORMATTER),
                email,
                otp);
        writeToLogFile("otp_emails.log", message);
        System.out.println("✉️  OTP logged: " + email);
    }

    /**
     * Log a verification email to file.
     * 
     * @param email            Recipient email address
     * @param verificationLink Verification link/token
     */
    public static void logVerificationEmail(String email, String verificationLink) {
        String message = String.format(
                "[%s] Verification Email sent to: %s\nLink: %s\n\n",
                LocalDateTime.now().format(DATE_FORMATTER),
                email,
                verificationLink);
        writeToLogFile("verification_emails.log", message);
        System.out.println("✉️  Verification email logged: " + email);
    }

    /**
     * Log a password reset email to file.
     * 
     * @param email     Recipient email address
     * @param resetLink Password reset link/token
     */
    public static void logPasswordResetEmail(String email, String resetLink) {
        String message = String.format(
                "[%s] Password Reset Email sent to: %s\nLink: %s\n\n",
                LocalDateTime.now().format(DATE_FORMATTER),
                email,
                resetLink);
        writeToLogFile("password_reset_emails.log", message);
        System.out.println("✉️  Password reset email logged: " + email);
    }

    /**
     * Log a notification email to file.
     * 
     * @param email   Recipient email address
     * @param subject Email subject
     * @param content Email content
     */
    public static void logNotificationEmail(String email, String subject, String content) {
        String message = String.format(
                "[%s] Notification Email sent to: %s\nSubject: %s\nContent: %s\n\n",
                LocalDateTime.now().format(DATE_FORMATTER),
                email,
                subject,
                content);
        writeToLogFile("notification_emails.log", message);
        System.out.println("✉️  Notification email logged: " + email);
    }

    /**
     * Write message to log file.
     * 
     * @param filename Name of the log file
     * @param message  Message to append
     */
    private static void writeToLogFile(String filename, String message) {
        try (FileWriter fw = new FileWriter(LOG_DIR + File.separator + filename, true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message);
            bw.flush();
        } catch (IOException e) {
            System.err.println("Error writing to email log: " + e.getMessage());
        }
    }
}

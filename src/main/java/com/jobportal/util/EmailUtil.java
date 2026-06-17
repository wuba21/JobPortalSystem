package com.jobportal.util;

import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * Email utility for sending OTPs and notifications.
 * Phase 1: Uses EmailFileLogger for development/testing.
 * Phase 3: Can be upgraded to use real SMTP (Gmail, SendGrid, etc.).
 */
public class EmailUtil {

    /**
     * Verifies if a given 16-character App Password is valid by attempting
     * to connect to Gmail's SMTP server.
     * 
     * @param email       The user's Gmail address
     * @param appPassword The 16-character App Password generated from Google
     * @return true if authentication succeeds, false otherwise
     */
    public static boolean verifyAppPassword(String email, String appPassword) {
        // Sanitize password (remove spaces)
        final String cleanPassword = appPassword.trim().replace(" ", "");

        if (cleanPassword.length() != 16) {
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000"); // 5 sec timeout
        props.put("mail.smtp.timeout", "5000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, cleanPassword);
            }
        });

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            return true;
        } catch (MessagingException e) {
            System.err.println("Verification Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send OTP email (Phase 1: Logged to file).
     * 
     * @param recipientEmail Recipient email address
     * @param otp            One-time password
     */
    public static void sendOTPEmail(String recipientEmail, String otp) {
        EmailFileLogger.logOTPEmail(recipientEmail, otp);
    }

    /**
     * Send notification email (Phase 1: Logged to file).
     * 
     * @param toEmail     Recipient email
     * @param subject     Email subject
     * @param messageBody Email body
     */
    public static void sendNotificationEmail(String toEmail, String subject, String messageBody) {
        EmailFileLogger.logNotificationEmail(toEmail, subject, messageBody);
    }

    /**
     * Send contact message email (Phase 1: Logged to file).
     * 
     * @param senderName  Sender's name
     * @param senderEmail Sender's email
     * @param messageBody Message content
     */
    public static void sendContactMessage(String senderName, String senderEmail, String messageBody) {
        String subject = "Contact Message from " + senderName;
        String content = "From: " + senderEmail + "\n" + messageBody;
        EmailFileLogger.logNotificationEmail("admin@jobportal.com", subject, content);
    }

    /**
     * Generate a random OTP (6 digits).
     * 
     * @return Generated OTP
     */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}

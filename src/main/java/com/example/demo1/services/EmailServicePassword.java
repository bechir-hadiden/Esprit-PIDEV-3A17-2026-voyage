package com.example.demo1.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Email service for password reset using Gmail App Passwords.
 * Uses Jakarta Mail (modular) instead of JavaMail (non-modular).
 */
public class EmailServicePassword {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "dridihamdi889@gmail.com";
    private static final String APP_PASSWORD = "ncgu lsvm payz twgn"; // Gmail App Password

    /**
     * Send password reset email with 6-digit code.
     * @param toEmail Recipient email address
     * @param code 6-digit reset code
     * @return true if email sent successfully
     */
    public boolean sendResetCode(String toEmail, String code) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, "SmartTrip"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset Code - SmartTrip");

            String htmlContent = buildResetEmailTemplate(code);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Password reset email sent to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send reset email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if email service is configured.
     * @return always true since credentials are hardcoded
     */
    public boolean isConfigured() {
        return FROM_EMAIL != null && !FROM_EMAIL.isEmpty()
            && APP_PASSWORD != null && !APP_PASSWORD.isEmpty();
    }

    /**
     * Build HTML email template for password reset.
     */
    private String buildResetEmailTemplate(String code) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>" +
            "body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}" +
            ".container{max-width:600px;margin:0 auto;background:white;border-radius:10px;overflow:hidden;box-shadow:0 4px 6px rgba(0,0,0,0.1)}" +
            ".header{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:30px;text-align:center}" +
            ".header h1{color:white;margin:0;font-size:24px}" +
            ".content{padding:40px 30px;text-align:center}" +
            ".code{font-size:48px;font-weight:bold;color:#667eea;letter-spacing:10px;margin:30px 0}" +
            ".message{color:#666;font-size:16px;line-height:1.6;margin-bottom:20px}" +
            ".warning{color:#e74c3c;font-size:14px;margin-top:20px}" +
            "</style></head><body>" +
            "<div class=\"container\"><div class=\"header\"><h1>Password Reset</h1></div>" +
            "<div class=\"content\">" +
            "<p class=\"message\">You requested a password reset for your SmartTrip account.</p>" +
            "<p class=\"message\">Use this 6-digit code:</p>" +
            "<div class=\"code\">" + code + "</div>" +
            "<p class=\"warning\">This code expires in 10 minutes.</p>" +
            "</div></div></body></html>";
    }
}

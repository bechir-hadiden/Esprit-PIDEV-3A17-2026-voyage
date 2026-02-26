package com.example.demo1.services;
import com.example.demo1.entity.User;
import com.example.demo1.controller.dao.UserDAO;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages password reset flow: generate 6-digit code, store with expiry, verify, update password.
 */
public class PasswordResetService {
    private static final int CODE_EXPIRY_MINUTES = 10;
    private static final Random RAND = new Random();

    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();

    // In-memory: email -> {code, expiryTimeMillis}
    private final Map<String, ResetEntry> codes = new ConcurrentHashMap<>();

    private static class ResetEntry {
        final String code;
        final long expiryAt;

        ResetEntry(String code, long expiryAt) {
            this.code = code;
            this.expiryAt = expiryAt;
        }
    }

    /**
     * Check if email exists in DB. If yes, generate 6-digit code, send email, store code.
     */
    public boolean requestReset(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String e = email.trim();
        User user = userDAO.getUserByEmail(e);
        if (user == null) return false;

        String code = String.format("%06d", RAND.nextInt(1_000_000));
        if (!emailService.sendResetCode(e, code)) return false;

        codes.put(e.toLowerCase(), new ResetEntry(code, System.currentTimeMillis() + CODE_EXPIRY_MINUTES * 60_000L));
        return true;
    }

    /**
     * Verify code and update password. Returns true if successful.
     */
    public boolean resetPassword(String email, String code, String newPassword) {
        if (email == null || code == null || newPassword == null) return false;
        String e = email.trim().toLowerCase();
        ResetEntry entry = codes.get(e);
        if (entry == null || !entry.code.equals(code.trim())) return false;
        if (System.currentTimeMillis() > entry.expiryAt) {
            codes.remove(e);
            return false;
        }

        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) return false;

        if (newPassword.length() < 6) return false;

        boolean ok = userDAO.updatePassword(Integer.parseInt(user.getId()), newPassword);
        if (ok) codes.remove(e);
        return ok;
    }

    public boolean isEmailConfigured() {
        return emailService.isConfigured();
    }
}


package com.example.demo1.Utils;

/**
 * Utility to generate BCrypt password hashes for database setup.
 * Run this class to generate proper password hashes.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        String password = "admin";

        // Generate hash
        String hash = BCryptWrapper.hashpw(password, BCryptWrapper.gensalt());

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println();

        // Verify it works
        boolean matches = BCryptWrapper.checkpw(password, hash);
        System.out.println("Verification test: " + (matches ? "PASS ✓" : "FAIL ✗"));
        System.out.println();

        // Test against the hash in sample_data.sql
        String sampleDataHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1J5w3xLGg7MKbqNRfKqZ5kzGzEqZ.5O";
        boolean sampleMatches = BCryptWrapper.checkpw(password, sampleDataHash);
        System.out.println("Testing sample_data.sql hash:");
        System.out.println("Hash: " + sampleDataHash);
        System.out.println("Match: " + (sampleMatches ? "PASS ✓" : "FAIL ✗"));
        System.out.println();

        if (!sampleMatches) {
            System.out.println("⚠️  WARNING: The hash in sample_data.sql does NOT match 'admin'!");
            System.out.println("Please update sample_data.sql with the following hash:");
            System.out.println(hash);
        }
    }
}

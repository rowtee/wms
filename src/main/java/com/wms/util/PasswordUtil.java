package com.wms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enterprise Password Hashing and Encryption Utility using BCrypt.
 */
public final class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int WORKLOAD = 10;

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Hashes a plain-text password using BCrypt with salt.
     *
     * @param plainTextPassword the raw user password
     * @return the BCrypt-encrypted hash string
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORKLOAD));
    }

    /**
     * Verifies a plain-text password against a stored hash.
     * Also supports fallback matching for legacy plain-text passwords so they can be migrated seamlessly.
     *
     * @param plainTextPassword  the raw input password
     * @param storedPasswordHash the hash (or legacy text) retrieved from the database
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String storedPasswordHash) {
        if (plainTextPassword == null || storedPasswordHash == null) {
            return false;
        }

        // Check if stored password is a BCrypt hash
        if (isBcryptHash(storedPasswordHash)) {
            try {
                return BCrypt.checkpw(plainTextPassword, storedPasswordHash);
            } catch (Exception e) {
                logger.error("Error verifying BCrypt password hash", e);
                return false;
            }
        }

        // Fallback: check plain text equality (legacy pre-encryption records)
        return storedPasswordHash.equals(plainTextPassword);
    }

    /**
     * Checks if a string has the standard BCrypt hash signature ($2a$, $2b$, or $2y$).
     */
    public static boolean isBcryptHash(String hash) {
        if (hash == null || hash.length() < 28) {
            return false;
        }
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}

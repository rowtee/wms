package com.wms;

import com.wms.config.DatabaseConfig;
import com.wms.dao.StaffDao;
import com.wms.model.Staff;
import com.wms.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing produces valid BCrypt hash and verifies successfully")
    void testHashAndVerify() {
        String raw = "123456";
        String hash = PasswordUtil.hashPassword(raw);
        System.out.println("BCRYPT_HASH_FOR_123456: " + hash);

        assertNotNull(hash);
        assertTrue(PasswordUtil.isBcryptHash(hash), "Hash must be a valid BCrypt hash string");
        assertNotEquals(raw, hash, "Password must not be stored in plain text");
        assertTrue(PasswordUtil.checkPassword(raw, hash), "Password verification should pass");
        assertFalse(PasswordUtil.checkPassword("wrongpass", hash), "Password verification should fail for invalid password");
    }

    @Test
    @DisplayName("Migrate all staff plaintext passwords in PostgreSQL to BCrypt encrypted hashes")
    void testMigratePlaintextPasswordsInDb() throws Exception {
        String selectSql = "SELECT staff_id, username, password_hash FROM staff";
        String updateSql = "UPDATE staff SET password_hash = ? WHERE staff_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             ResultSet rs = selectStmt.executeQuery()) {

            while (rs.next()) {
                long staffId = rs.getLong("staff_id");
                String username = rs.getString("username");
                String currentHash = rs.getString("password_hash");

                if (!PasswordUtil.isBcryptHash(currentHash)) {
                    // It's still plain text! Encrypt it with BCrypt
                    String encrypted = PasswordUtil.hashPassword(currentHash);
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, encrypted);
                        updateStmt.setLong(2, staffId);
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Encrypted password for user: " + username);
                }
            }
        }

        // Verify that all passwords in DB are valid BCrypt hashes and not empty
        StaffDao staffDao = new StaffDao();
        List<Staff> allStaff = staffDao.findAll();
        assertFalse(allStaff.isEmpty(), "Staff list should not be empty");

        // Verify that passwords in DB are no longer plain text
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             ResultSet rs = selectStmt.executeQuery()) {
            while (rs.next()) {
                String hash = rs.getString("password_hash");
                assertTrue(PasswordUtil.isBcryptHash(hash), "DB password must be BCrypt encrypted, found: " + hash);
            }
        }
    }
}

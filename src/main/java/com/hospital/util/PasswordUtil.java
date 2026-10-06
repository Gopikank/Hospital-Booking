package com.hospital.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility for hashing and verifying passwords using PBKDF2 with HMAC-SHA256.
 * Recommended standard Java cryptographic algorithm with zero external dependencies.
 */
public class PasswordUtil {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            if (testHash.length != expectedHash.length) {
                return false;
            }

            int diff = 0;
            for (int i = 0; i < testHash.length; i++) {
                diff |= testHash[i] ^ expectedHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Admin@123 -> " + hashPassword("Admin@123"));
        System.out.println("Doctor@123 -> " + hashPassword("Doctor@123"));
        System.out.println("Patient@123 -> " + hashPassword("Patient@123"));
    }
}

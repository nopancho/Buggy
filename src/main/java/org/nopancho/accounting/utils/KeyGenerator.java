package org.nopancho.accounting.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class KeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int API_KEY_LENGTH = 32;

    /**
     * Generate a random API key consisting of alphanumeric characters.
     *
     * @return a randomly generated API key.
     */
    public static String generateKey() {
        StringBuilder keyBuilder = new StringBuilder(API_KEY_LENGTH);

        for (int i = 0; i < API_KEY_LENGTH; i++) {
            if (secureRandom.nextBoolean()) {
                // Generate a random digit (0-9)
                keyBuilder.append((char) getRandomIntBetween(48, 57));
            } else {
                // Generate a random lowercase letter (a-z)
                keyBuilder.append((char) getRandomIntBetween(97, 122));
            }
        }

        return keyBuilder.toString();
    }

    /**
     * Generate an MD5 hash of a given password.
     *
     * @param password the password to hash.
     * @return the MD5 hash of the password.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes("UTF-8"));

            // Convert the byte array into a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Generate a random 6-digit registration code.
     *
     * @return a 6-digit registration code.
     */
    public static String generateRegistrationCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    /**
     * Generate a random integer between the specified min and max (inclusive).
     *
     * @param min the minimum value.
     * @param max the maximum value.
     * @return a random integer between min and max.
     */
    private static int getRandomIntBetween(int min, int max) {
        return secureRandom.nextInt((max - min) + 1) + min;
    }

    public static void main(String[] args) {
        // Test the generateKey method
        for (int i = 0; i < 10; i++) {
            System.out.println("API Key: " + generateKey());
        }

        // Test the hashPassword method
        System.out.println("Hashed Password: " + hashPassword("myPassword123"));

        // Test the generateRegistrationCode method
        System.out.println("Registration Code: " + generateRegistrationCode());
    }
}

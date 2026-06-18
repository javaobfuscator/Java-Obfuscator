package dev.hardobfuscator.runtime;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Verifies bytecode integrity using SHA-256 checksums.
 */
public final class IntegrityChecker {

    private IntegrityChecker() {
    }

    public static boolean verify(byte[] data, String expectedSha256) {
        if (expectedSha256 == null || expectedSha256.isBlank()) {
            return true;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String actual = HexFormat.of().formatHex(digest.digest(data));
            return actual.equalsIgnoreCase(expectedSha256);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    public static String checksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static boolean verifyString(String content, String expectedSha256) {
        return verify(content.getBytes(StandardCharsets.UTF_8), expectedSha256);
    }
}

package dev.hardobfuscator.runtime;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class StringDecryptor {

    private static volatile String key = "hardobfuscator-default-key";

    private StringDecryptor() {
    }

    public static void init(String encryptionKey) {
        if (encryptionKey != null && !encryptionKey.isBlank()) {
            key = encryptionKey;
        }
    }

    public static String decrypt(String encrypted, int index) {
        return decryptHeavy(encrypted, index, 0);
    }

    public static String decryptHeavy(String encrypted, int index, int salt) {
        byte[] data = Base64.getDecoder().decode(encrypted);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            for (int round = 2; round >= 0; round--) {
                b = rotr8(b, 3);
                int k = keyBytes[(i + index + round) % keyBytes.length] & 0xFF;
                b ^= k ^ ((index + i + round * 17 + salt) & 0xFF);
            }
            out[i] = (byte) b;
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    private static int rotr8(int b, int bits) {
        return ((b >>> bits) | (b << (8 - bits))) & 0xFF;
    }
}

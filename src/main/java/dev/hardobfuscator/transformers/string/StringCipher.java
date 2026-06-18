package dev.hardobfuscator.transformers.string;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class StringCipher {

    private StringCipher() {
    }

    public static String encrypt(String input, String key, int index, int salt) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            for (int round = 0; round < 3; round++) {
                int k = keyBytes[(i + index + round) % keyBytes.length] & 0xFF;
                b ^= k ^ ((index + i + round * 17 + salt) & 0xFF);
                b = rotl8(b, 3);
            }
            out[i] = (byte) b;
        }
        return Base64.getEncoder().encodeToString(out);
    }

    public static int saltFor(String input, int index) {
        int hash = index * 31 + input.length();
        for (int i = 0; i < input.length(); i++) {
            hash = hash * 31 + input.charAt(i);
        }
        return hash & 0x7FFF;
    }

    private static int rotl8(int b, int bits) {
        return ((b << bits) | (b >>> (8 - bits))) & 0xFF;
    }
}

package dev.hardobfuscator.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and decrypts encrypted resources at runtime.
 */
public final class ResourceLoader {

    private static final Map<String, byte[]> cache = new ConcurrentHashMap<>();
    private static volatile String key = "hardobfuscator-resource-key";

    private ResourceLoader() {
    }

    public static void init(String encryptionKey) {
        if (encryptionKey != null && !encryptionKey.isBlank()) {
            key = encryptionKey;
        }
    }

    public static byte[] load(String path, InputStream rawStream) throws IOException {
        return cache.computeIfAbsent(path, p -> {
            try {
                byte[] data = rawStream.readAllBytes();
                if (data.length > 6 && new String(data, 0, 6, StandardCharsets.UTF_8).equals("AGENC:")) {
                    String payload = new String(data, 6, data.length - 6, StandardCharsets.UTF_8);
                    String decrypted = StringDecryptor.decrypt(payload, 0);
                    return Base64.getDecoder().decode(decrypted);
                }
                return data;
            } catch (IOException e) {
                throw new RuntimeException("Failed to load resource: " + path, e);
            }
        });
    }

    public static InputStream loadStream(String path, InputStream rawStream) throws IOException {
        return new ByteArrayInputStream(load(path, rawStream));
    }

    public static void clearCache() {
        cache.clear();
    }
}

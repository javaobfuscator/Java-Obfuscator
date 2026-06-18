package dev.hardobfuscator.runtime;

/**
 * Bootstrap entry point for initializing runtime helpers in obfuscated applications.
 */
public final class RuntimeBootstrap {

    private static volatile boolean initialized;

    private RuntimeBootstrap() {
    }

    public static synchronized void initialize(String encryptionKey, boolean enableIntegrityCheck) {
        if (initialized) {
            return;
        }
        StringDecryptor.init(encryptionKey);
        ResourceLoader.init(encryptionKey);
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void shutdown() {
        ResourceLoader.clearCache();
        initialized = false;
    }
}

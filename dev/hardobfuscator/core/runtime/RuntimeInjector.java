package dev.hardobfuscator.core.runtime;

import dev.hardobfuscator.config.RuntimeConfig;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

/**
 * Injects the HardObfuscator runtime helper classes into the output JAR.
 */
public final class RuntimeInjector {

    private static final Logger log = LoggerFactory.getLogger(RuntimeInjector.class);
    private static final String[] RUNTIME_CLASSES = {
            "StringDecryptor", "ResourceLoader", "IntegrityChecker", "RuntimeBootstrap"
    };

    public void inject(Context context, RuntimeConfig config) throws IOException {
        log.info("Injecting runtime classes");
        for (String className : RUNTIME_CLASSES) {
            injectClass(context, className);
        }

        context.setAttribute("runtimeKey", config.getEncryptionKey() != null
                && !config.getEncryptionKey().isBlank()
                ? config.getEncryptionKey()
                : generateDefaultKey());
        context.setAttribute("encryptionMode", config.getEncryptionMode());
        context.setAttribute("integrityCheck", config.isIntegrityCheck());
    }

    private void injectClass(Context context, String className) throws IOException {
        String resourcePath = "dev/hardobfuscator/runtime/" + className + ".class";
        try (InputStream in = RuntimeInjector.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                log.warn("Runtime class not found on classpath: {}", resourcePath);
                return;
            }
            byte[] bytecode = in.readAllBytes();
            String internalName = "dev/hardobfuscator/runtime/" + className;
            context.classes().put(internalName, new ClassEntry(internalName, bytecode));
            log.debug("Injected runtime class: {}", internalName);
        }
    }

    private String generateDefaultKey() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        StringBuilder sb = new StringBuilder();
        for (byte b : key) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

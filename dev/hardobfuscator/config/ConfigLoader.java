package dev.hardobfuscator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and serializes obfuscation configuration from JSON files.
 */
public final class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ConfigLoader() {
    }

    public static ObfuscationConfig load(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return MAPPER.readValue(in, ObfuscationConfig.class);
        }
    }

    public static ObfuscationConfig load(String json) throws IOException {
        return MAPPER.readValue(json, ObfuscationConfig.class);
    }

    public static void save(ObfuscationConfig config, Path path) throws IOException {
        MAPPER.writeValue(path.toFile(), config);
    }

    public static String toJson(ObfuscationConfig config) throws IOException {
        return MAPPER.writeValueAsString(config);
    }

    public static ObfuscationConfig defaultConfig() {
        ObfuscationConfig config = new ObfuscationConfig();
        config.setInput("app.jar");
        config.setOutput("app-obf.jar");
        config.getTransformers().put("classRename", true);
        config.getTransformers().put("methodRename", true);
        config.getTransformers().put("fieldRename", true);
        config.getTransformers().put("packageRename", false);
        config.getTransformers().put("stringEncryption", true);
        config.getTransformers().put("controlFlow", true);
        config.getTransformers().put("numberHiding", true);
        config.getTransformers().put("debugRemoval", true);
        config.getTransformers().put("resourceEncryption", false);
        return config;
    }
}

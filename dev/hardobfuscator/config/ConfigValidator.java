package dev.hardobfuscator.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates configuration before an obfuscation run begins.
 */
public final class ConfigValidator {

    private ConfigValidator() {
    }

    public static List<String> validate(ObfuscationConfig config) {
        List<String> errors = new ArrayList<>();

        if (config.getInput() == null || config.getInput().isBlank()) {
            errors.add("Input JAR path is required");
        }
        if (config.getOutput() == null || config.getOutput().isBlank()) {
            errors.add("Output JAR path is required");
        }
        if (config.getInput() != null && config.getOutput() != null
                && config.getInput().equals(config.getOutput())) {
            errors.add("Input and output paths must differ");
        }
        if (config.getAdvanced().getThreads() < 1) {
            errors.add("Thread count must be at least 1");
        }
        if (config.getAdvanced().getBatchSize() < 1) {
            errors.add("Batch size must be at least 1");
        }

        boolean anyEnabled = config.getTransformers().values().stream().anyMatch(Boolean::booleanValue);
        if (!anyEnabled) {
            errors.add("At least one transformer must be enabled");
        }

        return errors;
    }
}

package dev.hardobfuscator.plugins;

import dev.hardobfuscator.plugins.api.Context;

/**
 * Core contract for all bytecode transformers in the HardObfuscator pipeline.
 * Each transformer receives a shared {@link Context} and mutates class entries in-place.
 */
public interface Transformer {

    /** Unique transformer identifier used in configuration. */
    String name();

    /** Human-readable description shown in GUI and CLI help. */
    String description();

    /** Category for grouping in the transformer selection UI. */
    TransformerCategory category();

    /** Whether this transformer is enabled by default. */
    default boolean enabledByDefault() {
        return false;
    }

    /**
     * Executes the transformation against all applicable classes in the context.
     *
     * @param context shared obfuscation context containing classes, resources, and configuration
     */
    void transform(Context context);
}

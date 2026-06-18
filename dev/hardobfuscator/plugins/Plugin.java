package dev.hardobfuscator.plugins;

import java.util.List;

/**
 * Extension point for third-party transformer bundles.
 * Plugins are discovered via {@link java.util.ServiceLoader}.
 */
public interface Plugin {

    /** Plugin display name. */
    String name();

    /** Plugin version string. */
    String version();

    /** Called once when the plugin is loaded into the engine. */
    void initialize();

    /** Returns all transformers contributed by this plugin. */
    List<Transformer> transformers();
}

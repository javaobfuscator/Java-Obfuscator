package dev.hardobfuscator.core.plugin;

import dev.hardobfuscator.core.pipeline.TransformerRegistry;
import dev.hardobfuscator.plugins.Plugin;
import dev.hardobfuscator.plugins.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Discovers and loads plugins via the Java ServiceLoader mechanism.
 */
public final class PluginLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);
    private final List<Plugin> loadedPlugins = new ArrayList<>();

    public List<Plugin> loadPlugins(TransformerRegistry registry) {
        ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);
        for (Plugin plugin : loader) {
            try {
                log.info("Loading plugin: {} v{}", plugin.name(), plugin.version());
                plugin.initialize();
                List<Transformer> transformers = plugin.transformers();
                registry.registerAll(transformers);
                loadedPlugins.add(plugin);
                log.info("Plugin {} contributed {} transformers", plugin.name(), transformers.size());
            } catch (Exception e) {
                log.error("Failed to load plugin {}: {}", plugin.name(), e.getMessage(), e);
            }
        }
        return List.copyOf(loadedPlugins);
    }

    public List<Plugin> loadedPlugins() {
        return List.copyOf(loadedPlugins);
    }
}

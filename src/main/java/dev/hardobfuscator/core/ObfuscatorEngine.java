package dev.hardobfuscator.core;

import dev.hardobfuscator.config.ExclusionPolicyFactory;
import dev.hardobfuscator.config.ObfuscationConfig;
import dev.hardobfuscator.core.dependency.DependencyManager;
import dev.hardobfuscator.core.di.Bootstrap;
import dev.hardobfuscator.core.di.ServiceRegistry;
import dev.hardobfuscator.core.pipeline.ObfuscationPipeline;
import dev.hardobfuscator.core.pipeline.TransformerRegistry;
import dev.hardobfuscator.core.plugin.PluginLoader;
import dev.hardobfuscator.plugins.Transformer;
import dev.hardobfuscator.plugins.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Main entry point for programmatic obfuscation via the HardObfuscator engine.
 */
public final class ObfuscatorEngine {

    private static final Logger log = LoggerFactory.getLogger(ObfuscatorEngine.class);

    private final ObfuscationPipeline pipeline;
    private final TransformerRegistry registry;
    private final DependencyManager dependencyManager;

    public ObfuscatorEngine() {
        Bootstrap.initialize();
        ServiceRegistry services = ServiceRegistry.getInstance();
        this.pipeline = new ObfuscationPipeline();
        this.registry = services.resolve(TransformerRegistry.class);
        this.dependencyManager = services.resolve(DependencyManager.class);
    }

    /**
     * Bootstraps dependencies and registers all built-in and plugin transformers.
     */
    public void initialize(List<Transformer> builtInTransformers) throws Exception {
        log.info("Initializing HardObfuscator v1.0.0");
        dependencyManager.ensureDependencies();
        registry.registerAll(builtInTransformers);
        new PluginLoader().loadPlugins(registry);
        log.info("Engine ready with {} transformers", registry.size());
    }

    public void obfuscate(ObfuscationConfig config) throws Exception {
        Path input = Path.of(config.getInput());
        Path output = Path.of(config.getOutput());

        Context context = new Context(
                input,
                output,
                ExclusionPolicyFactory.fromConfig(config)
        );

        pipeline.execute(config, context);
    }

    public TransformerRegistry registry() {
        return registry;
    }

    public DependencyManager dependencyManager() {
        return dependencyManager;
    }
}

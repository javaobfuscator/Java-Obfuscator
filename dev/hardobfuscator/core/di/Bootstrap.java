package dev.hardobfuscator.core.di;

/**
 * Bootstraps core services into the {@link ServiceRegistry}.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void initialize() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        registry.registerSingleton(dev.hardobfuscator.core.event.EventBus.class, new dev.hardobfuscator.core.event.EventBus());
        registry.registerSingleton(dev.hardobfuscator.core.scheduler.TaskScheduler.class,
                new dev.hardobfuscator.core.scheduler.TaskScheduler());
        registry.registerSingleton(dev.hardobfuscator.core.pipeline.TransformerRegistry.class,
                new dev.hardobfuscator.core.pipeline.TransformerRegistry());
        registry.registerSingleton(dev.hardobfuscator.core.profiler.TransformerProfiler.class,
                new dev.hardobfuscator.core.profiler.TransformerProfiler());
        registry.registerFactory(dev.hardobfuscator.core.dependency.DependencyManager.class,
                dev.hardobfuscator.core.dependency.DependencyManager::new);
    }
}

package dev.hardobfuscator.core.pipeline;

import dev.hardobfuscator.config.ObfuscationConfig;
import dev.hardobfuscator.core.di.ServiceRegistry;
import dev.hardobfuscator.core.event.*;
import dev.hardobfuscator.core.io.JarReader;
import dev.hardobfuscator.core.io.JarWriter;
import dev.hardobfuscator.core.io.MetaInfHandler;
import dev.hardobfuscator.core.profiler.TransformerProfiler;
import dev.hardobfuscator.core.runtime.RuntimeInjector;
import dev.hardobfuscator.core.scheduler.TaskScheduler;
import dev.hardobfuscator.plugins.Transformer;
import dev.hardobfuscator.plugins.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Orchestrates the full obfuscation pipeline: read → transform → inject runtime → write.
 */
public final class ObfuscationPipeline {

    private static final Logger log = LoggerFactory.getLogger(ObfuscationPipeline.class);

    private final JarReader jarReader = new JarReader();
    private final JarWriter jarWriter = new JarWriter();
    private final TransformerRegistry registry;
    private final EventBus eventBus;
    private final TaskScheduler scheduler;
    private final TransformerProfiler profiler;

    public ObfuscationPipeline() {
        ServiceRegistry services = ServiceRegistry.getInstance();
        this.registry = services.resolve(TransformerRegistry.class);
        this.eventBus = services.resolve(EventBus.class);
        this.scheduler = services.resolve(TaskScheduler.class);
        this.profiler = services.resolve(TransformerProfiler.class);
    }

    public void execute(ObfuscationConfig config, Context context) throws Exception {
        long startTime = System.currentTimeMillis();
        eventBus.publish(new PipelineStartEvent(config.getInput()));

        scheduler.initialize(config.getAdvanced().getThreads());

        jarReader.read(Path.of(config.getInput()), context);
        context.setAttribute("config", config);

        long scopedClasses = context.mutableClassList().stream()
                .filter(e -> !context.isExcluded(e.internalName()))
                .count();
        context.setAttribute("scopedClassCount", scopedClasses);

        if (config.getTargetPackage() != null) {
            log.info("Package scope: only {}.* will be obfuscated", config.getTargetPackage());
        }

        List<Transformer> transformers = registry.resolveEnabled(config.getTransformers());
        log.info("Executing {} transformers on {} classes", transformers.size(), context.classes().size());

        int step = 0;
        for (Transformer transformer : transformers) {
            step++;
            eventBus.publish(TransformerEvent.start(transformer.name()));
            eventBus.publish(new ProgressEvent(step, transformers.size(), "Running: " + transformer.name()));

            long t0 = System.currentTimeMillis();
            try {
                transformer.transform(context);
                long elapsed = System.currentTimeMillis() - t0;
                profiler.record(transformer.name(), elapsed);
                context.statistics().recordTransformerTime(transformer.name(), elapsed);
                eventBus.publish(TransformerEvent.complete(transformer.name(), elapsed));

                ObfuscationConfig cfg = context.getAttribute("config");
                if (cfg != null && cfg.getAdvanced().isMemoryOptimization()) {
                    System.gc();
                }
            } catch (Exception e) {
                String detail = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                log.error("Transformer {} failed: {}", transformer.name(), detail, e);
                eventBus.publish(TransformerEvent.error(transformer.name(), detail));
                throw new RuntimeException("Transformer failed: " + transformer.name() + " — " + detail, e);
            }
        }

        if (config.getRuntime().isInjectRuntime()) {
            new RuntimeInjector().inject(context, config.getRuntime());
        }

        new MetaInfHandler().repair(context);

        jarWriter.write(context);

        long totalMs = System.currentTimeMillis() - startTime;
        eventBus.publish(new PipelineCompleteEvent(config.getOutput(), totalMs));
        log.info("Obfuscation complete in {} ms → {}", totalMs, config.getOutput());
    }
}

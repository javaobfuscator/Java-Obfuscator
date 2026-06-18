package dev.hardobfuscator.transformers.base;

import dev.hardobfuscator.config.ObfuscationConfig;
import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.core.bytecode.ContextClassWriter;
import dev.hardobfuscator.core.di.ServiceRegistry;
import dev.hardobfuscator.core.event.EventBus;
import dev.hardobfuscator.core.event.ProgressEvent;
import dev.hardobfuscator.core.scheduler.TaskScheduler;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Helper for applying ASM ClassVisitor transformations across all non-excluded classes.
 */
public final class ClassTransformerHelper {

    private static final Logger log = LoggerFactory.getLogger(ClassTransformerHelper.class);

    public enum FrameMode {
        PRESERVE,
        COMPUTE_MAXS,
        RECOMPUTE
    }

    private ClassTransformerHelper() {
    }

    /** Applies a transform to every class in the JAR, including package-scope exclusions. */
    public static void transformEntireJar(Context context,
                                          BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                          String progressLabel) {
        List<ClassEntry> entries = context.mutableClassList();
        if (entries.isEmpty()) {
            return;
        }
        AtomicInteger processed = new AtomicInteger();
        int total = entries.size();
        ObfuscationConfig config = context.getAttribute("config");
        boolean parallel = shouldUseParallel(context, FrameMode.COMPUTE_MAXS) && entries.size() > 1;
        int batchSize = effectiveBatchSize(config, FrameMode.COMPUTE_MAXS, false);

        Consumer<ClassEntry> processor = entry -> {
            try {
                applyTransform(context, entry, visitorFactory, FrameMode.COMPUTE_MAXS, new ConcurrentHashMap<>());
                int done = processed.incrementAndGet();
                if (done % 100 == 0 || done == total) {
                    log.info("{}: {}/{} classes", progressLabel, done, total);
                    publishProgress(done, total, progressLabel + " (" + done + "/" + total + ")");
                }
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to transform class: " + entry.internalName() + " — " + e.getMessage(), e);
            }
        };

        if (parallel) {
            TaskScheduler scheduler = ServiceRegistry.getInstance().resolve(TaskScheduler.class);
            scheduler.executeBatch(new ArrayList<>(entries), batchSize, batch -> batch.forEach(processor));
        } else {
            entries.forEach(processor);
        }
    }

    public static void transformAll(Context context,
                                    BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory) {
        transformAllParallel(context, visitorFactory, FrameMode.COMPUTE_MAXS, "Transforming");
    }

    public static void transformAll(Context context,
                                    BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                    FrameMode frameMode) {
        transformAllParallel(context, visitorFactory, frameMode, "Transforming");
    }

    /** Sequential, low-memory path for control-flow / tree-API transformers. */
    public static void transformAllHeavy(Context context,
                                         BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                         String progressLabel) {
        transformAllParallel(context, visitorFactory, FrameMode.RECOMPUTE, progressLabel, true);
    }

    public static void transformAllParallel(Context context,
                                            BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                            FrameMode frameMode,
                                            String progressLabel) {
        transformAllParallel(context, visitorFactory, frameMode, progressLabel, false);
    }

    private static void transformAllParallel(Context context,
                                             BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                             FrameMode frameMode,
                                             String progressLabel,
                                             boolean forceSequential) {
        List<ClassEntry> entries = collectEntries(context);
        if (entries.isEmpty()) {
            return;
        }

        ObfuscationConfig config = context.getAttribute("config");
        int batchSize = effectiveBatchSize(config, frameMode, forceSequential);
        boolean parallel = !forceSequential && shouldUseParallel(context, frameMode) && entries.size() > 1;

        AtomicInteger processed = new AtomicInteger();
        int total = entries.size();

        Consumer<ClassEntry> processor = entry -> {
            Map<String, ClassReader> readerCache = parallel ? null : new ConcurrentHashMap<>();
            try {
                long t0 = forceSequential ? System.currentTimeMillis() : 0;
                applyTransform(context, entry, visitorFactory, frameMode,
                        parallel ? new ConcurrentHashMap<>() : readerCache);
                context.statistics().incrementClassesProcessed();
                int done = processed.incrementAndGet();
                if (forceSequential) {
                    long elapsed = System.currentTimeMillis() - t0;
                    if (elapsed > 3_000) {
                        log.info("{}: {}/{} classes (slow: {} took {} ms)",
                                progressLabel, done, total, entry.internalName(), elapsed);
                    } else {
                        log.info("{}: {}/{} classes ({})", progressLabel, done, total, entry.internalName());
                    }
                    publishProgress(done, total, progressLabel + " (" + done + "/" + total + ")");
                } else if (done % 25 == 0 || done == total) {
                    log.info("{}: {}/{} classes", progressLabel, done, total);
                    publishProgress(done, total, progressLabel + " (" + done + "/" + total + ")");
                }
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to transform class: " + entry.internalName() + " — " + e.getMessage(), e);
            }
        };

        if (parallel) {
            TaskScheduler scheduler = ServiceRegistry.getInstance().resolve(TaskScheduler.class);
            scheduler.executeBatch(new ArrayList<>(entries), batchSize, batch -> {
                Map<String, ClassReader> batchCache = new ConcurrentHashMap<>();
                batch.forEach(entry -> {
                    try {
                        applyTransform(context, entry, visitorFactory, frameMode, batchCache);
                        context.statistics().incrementClassesProcessed();
                        int done = processed.incrementAndGet();
                        if (done % 50 == 0 || done == total) {
                            log.info("{}: {}/{} classes", progressLabel, done, total);
                            publishProgress(done, total, progressLabel + " (" + done + "/" + total + ")");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Failed to transform class: " + entry.internalName() + " — " + e.getMessage(), e);
                    }
                });
            });
        } else {
            entries.forEach(processor);
        }
    }

    public static void transformClass(Context context,
                                      ClassEntry entry,
                                      BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory) {
        transformClass(context, entry, visitorFactory, FrameMode.COMPUTE_MAXS);
    }

    public static void transformClass(Context context,
                                      ClassEntry entry,
                                      BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                      FrameMode frameMode) {
        try {
            applyTransform(context, entry, visitorFactory, frameMode, new ConcurrentHashMap<>());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to transform class: " + entry.internalName() + " — " + e.getMessage(), e);
        }
    }

    public static void stripDebugInfo(Context context) {
        List<ClassEntry> entries = collectEntries(context);
        if (entries.isEmpty()) {
            return;
        }

        ObfuscationConfig config = context.getAttribute("config");
        AtomicInteger processed = new AtomicInteger();
        int total = entries.size();

        Consumer<ClassEntry> stripper = entry -> {
            try {
                ClassReader reader = BytecodeUtil.read(entry.bytecode());
                ClassWriter writer = new ContextClassWriter(reader, 0, context.classes(), new ConcurrentHashMap<>());
                reader.accept(writer, ClassReader.SKIP_DEBUG);
                entry.setBytecode(writer.toByteArray());
                context.statistics().incrementClassesProcessed();
                int done = processed.incrementAndGet();
                if (done % 100 == 0 || done == total) {
                    publishProgress(done, total, "Stripping debug (" + done + "/" + total + ")");
                }
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to strip debug info: " + entry.internalName() + " — " + e.getMessage(), e);
            }
        };

        if (shouldUseParallel(context, FrameMode.PRESERVE) && entries.size() > 1) {
            TaskScheduler scheduler = ServiceRegistry.getInstance().resolve(TaskScheduler.class);
            int batchSize = effectiveBatchSize(config, FrameMode.PRESERVE, false);
            scheduler.executeBatch(new ArrayList<>(entries), batchSize, chunk -> chunk.forEach(stripper));
        } else {
            entries.forEach(stripper);
        }
    }

    private static boolean shouldUseParallel(Context context, FrameMode frameMode) {
        if (frameMode == FrameMode.RECOMPUTE) {
            return false;
        }
        ObfuscationConfig config = context.getAttribute("config");
        if (config != null && config.getAdvanced().isMemoryOptimization()) {
            return false;
        }
        return config == null || config.getAdvanced().isParallelProcessing();
    }

    private static int effectiveBatchSize(ObfuscationConfig config, FrameMode frameMode, boolean forceSequential) {
        if (forceSequential || frameMode == FrameMode.RECOMPUTE) {
            return 1;
        }
        int configured = config != null ? config.getAdvanced().getBatchSize() : 32;
        if (config != null && config.getAdvanced().isMemoryOptimization()) {
            return Math.min(configured, 8);
        }
        return configured;
    }

    private static List<ClassEntry> collectEntries(Context context) {
        List<ClassEntry> entries = new ArrayList<>();
        for (ClassEntry entry : context.mutableClassList()) {
            if (!context.isExcluded(entry.internalName())) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private static void applyTransform(Context context,
                                       ClassEntry entry,
                                       BiFunction<ClassReader, ClassVisitor, ClassVisitor> visitorFactory,
                                       FrameMode frameMode,
                                       Map<String, ClassReader> readerCache) throws Exception {
        ClassReader reader = BytecodeUtil.read(entry.bytecode());
        int readerFlags = frameMode == FrameMode.RECOMPUTE ? ClassReader.EXPAND_FRAMES : 0;
        int writerFlags = switch (frameMode) {
            case PRESERVE -> 0;
            case COMPUTE_MAXS -> ClassWriter.COMPUTE_MAXS;
            case RECOMPUTE -> ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;
        };
        ClassWriter writer = new ContextClassWriter(reader, writerFlags, context.classes(), readerCache);
        ClassVisitor visitor = visitorFactory.apply(reader, writer);
        reader.accept(visitor, readerFlags);
        entry.setBytecode(writer.toByteArray());
    }

    private static void publishProgress(int current, int total, String message) {
        try {
            ServiceRegistry.getInstance()
                    .resolve(EventBus.class)
                    .publish(new ProgressEvent(current, total, message));
        } catch (Exception ignored) {
        }
    }
}

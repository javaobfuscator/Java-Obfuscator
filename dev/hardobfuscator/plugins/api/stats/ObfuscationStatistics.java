package dev.hardobfuscator.plugins.api.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects aggregate metrics during an obfuscation run.
 */
public class ObfuscationStatistics {

    private final AtomicInteger classesProcessed = new AtomicInteger();
    private final AtomicInteger stringsEncrypted = new AtomicInteger();
    private final AtomicInteger methodsRenamed = new AtomicInteger();
    private final AtomicInteger fieldsRenamed = new AtomicInteger();
    private final AtomicInteger classesRenamed = new AtomicInteger();
    private final AtomicInteger resourcesEncrypted = new AtomicInteger();
    private final AtomicLong totalProcessingTimeMs = new AtomicLong();
    private final Map<String, Long> transformerTimings = new ConcurrentHashMap<>();

    public void incrementClassesProcessed() {
        classesProcessed.incrementAndGet();
    }

    public void addStringsEncrypted(int count) {
        stringsEncrypted.addAndGet(count);
    }

    public void incrementMethodsRenamed() {
        methodsRenamed.incrementAndGet();
    }

    public void addMethodsRenamed(int count) {
        methodsRenamed.addAndGet(count);
    }

    public void incrementFieldsRenamed() {
        fieldsRenamed.incrementAndGet();
    }

    public void incrementClassesRenamed() {
        classesRenamed.incrementAndGet();
    }

    public void incrementResourcesEncrypted() {
        resourcesEncrypted.incrementAndGet();
    }

    public void recordTransformerTime(String name, long millis) {
        transformerTimings.merge(name, millis, Long::sum);
        totalProcessingTimeMs.addAndGet(millis);
    }

    public int classesProcessed() {
        return classesProcessed.get();
    }

    public int stringsEncrypted() {
        return stringsEncrypted.get();
    }

    public int methodsRenamed() {
        return methodsRenamed.get();
    }

    public int fieldsRenamed() {
        return fieldsRenamed.get();
    }

    public int classesRenamed() {
        return classesRenamed.get();
    }

    public int resourcesEncrypted() {
        return resourcesEncrypted.get();
    }

    public long totalProcessingTimeMs() {
        return totalProcessingTimeMs.get();
    }

    public Map<String, Long> transformerTimings() {
        return Map.copyOf(transformerTimings);
    }
}

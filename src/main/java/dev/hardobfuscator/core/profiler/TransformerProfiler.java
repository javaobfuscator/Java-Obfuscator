package dev.hardobfuscator.core.profiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Records per-transformer execution timings for the profiler UI.
 */
public final class TransformerProfiler {

    private final Map<String, AtomicLong> totalTimeMs = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> invocationCount = new ConcurrentHashMap<>();

    public void record(String transformerName, long durationMs) {
        totalTimeMs.computeIfAbsent(transformerName, k -> new AtomicLong()).addAndGet(durationMs);
        invocationCount.computeIfAbsent(transformerName, k -> new AtomicLong()).incrementAndGet();
    }

    public long totalTime(String transformerName) {
        AtomicLong val = totalTimeMs.get(transformerName);
        return val != null ? val.get() : 0;
    }

    public long invocations(String transformerName) {
        AtomicLong val = invocationCount.get(transformerName);
        return val != null ? val.get() : 0;
    }

    public Map<String, Long> allTimings() {
        return totalTimeMs.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    public void reset() {
        totalTimeMs.clear();
        invocationCount.clear();
    }
}

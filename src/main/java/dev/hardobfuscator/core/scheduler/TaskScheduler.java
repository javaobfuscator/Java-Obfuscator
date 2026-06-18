package dev.hardobfuscator.core.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Manages thread pools and batch execution for parallel class processing.
 */
public final class TaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskScheduler.class);
    private ExecutorService executor;

    public void initialize(int threadCount) {
        shutdown();
        executor = Executors.newFixedThreadPool(
                Math.max(1, threadCount),
                r -> {
                    Thread t = new Thread(r, "hardobfuscator-worker");
                    t.setDaemon(true);
                    return t;
                }
        );
        log.debug("Task scheduler initialized with {} threads", threadCount);
    }

    public <T> void executeBatch(List<T> items, int batchSize, Consumer<List<T>> batchProcessor) {
        if (executor == null) {
            initialize(Runtime.getRuntime().availableProcessors());
        }
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            List<T> batch = items.subList(i, Math.min(i + batchSize, items.size()));
            futures.add(executor.submit(() -> batchProcessor.accept(batch)));
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Batch processing interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Batch processing failed", e.getCause());
            }
        }
    }

    public <T> List<T> mapParallel(List<T> items, java.util.function.Function<T, T> mapper) {
        if (executor == null) {
            initialize(Runtime.getRuntime().availableProcessors());
        }
        try {
            return executor.invokeAll(
                    items.stream().map(item -> (Callable<T>) () -> mapper.apply(item)).toList()
            ).stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel mapping interrupted", e);
        }
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
    }
}

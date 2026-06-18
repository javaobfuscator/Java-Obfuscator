package dev.hardobfuscator.core.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Orchestrates dependency resolution: checks libs/, downloads missing artifacts in parallel.
 */
public final class DependencyManager {

    private static final Logger log = LoggerFactory.getLogger(DependencyManager.class);

    private final Path libsDir;
    private final DependencyCache cache;
    private final MavenDownloader downloader;

    public DependencyManager() {
        this(Path.of("libs"));
    }

    public DependencyManager(Path libsDir) {
        this.libsDir = libsDir.toAbsolutePath().normalize();
        this.cache = new DependencyCache(this.libsDir);
        this.downloader = new MavenDownloader();
    }

    /**
     * Ensures all required libraries are present in the libs directory.
     *
     * @return paths to all resolved JAR files
     */
    public List<Path> ensureDependencies() throws IOException {
        cache.ensureCacheDir();
        List<DependencySpec> required = DependencyCatalog.REQUIRED;
        List<DependencySpec> missing = new ArrayList<>();

        for (DependencySpec spec : required) {
            Path target = cache.cacheFile(spec);
            if (!cache.isCached(spec)) {
                missing.add(spec);
            } else {
                log.debug("Dependency already present: {}", spec.fileName());
            }
        }

        if (!missing.isEmpty()) {
            log.info("Downloading {} missing dependencies to {}", missing.size(), libsDir);
            downloadParallel(missing);
        } else {
            log.info("All {} dependencies are up to date", required.size());
        }

        return required.stream().map(cache::cacheFile).toList();
    }

    private void downloadParallel(List<DependencySpec> specs) throws IOException {
        int threads = Math.min(specs.size(), Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (DependencySpec spec : specs) {
            futures.add(pool.submit(() -> {
                try {
                    Path target = cache.cacheFile(spec);
                    downloader.download(spec, target);
                    cache.markDownloaded(spec);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to download " + spec.fileName(), e);
                }
            }));
        }

        pool.shutdown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                pool.shutdownNow();
                throw new IOException("Parallel download failed", e);
            }
        }
    }

    public Path libsDirectory() {
        return libsDir;
    }

    public boolean verifyAll() throws IOException {
        for (DependencySpec spec : DependencyCatalog.REQUIRED) {
            Path file = cache.cacheFile(spec);
            if (!Files.exists(file)) {
                return false;
            }
        }
        return true;
    }
}

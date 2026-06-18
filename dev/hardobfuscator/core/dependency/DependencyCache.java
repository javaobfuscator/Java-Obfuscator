package dev.hardobfuscator.core.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent cache tracking downloaded dependency versions and checksums.
 */
public final class DependencyCache {

    private static final Logger log = LoggerFactory.getLogger(DependencyCache.class);
    private final Path cacheDir;
    private final Map<String, String> versionIndex = new ConcurrentHashMap<>();

    public DependencyCache(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    public Path cacheFile(DependencySpec spec) {
        return cacheDir.resolve(spec.fileName());
    }

    public boolean isCached(DependencySpec spec) {
        Path file = cacheFile(spec);
        return Files.exists(file) && Files.isRegularFile(file);
    }

    public void markDownloaded(DependencySpec spec) {
        versionIndex.put(spec.cacheKey(), spec.version());
        log.debug("Cached dependency: {}", spec.cacheKey());
    }

    public String cachedVersion(String cacheKey) {
        return versionIndex.get(cacheKey);
    }

    public void ensureCacheDir() throws IOException {
        Files.createDirectories(cacheDir);
    }
}

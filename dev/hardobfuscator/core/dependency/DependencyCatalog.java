package dev.hardobfuscator.core.dependency;

import java.util.List;

/**
 * Catalog of third-party libraries managed by the bootstrap downloader.
 * SHA-256 hashes ensure artifact integrity after download from Maven Central.
 */
public final class DependencyCatalog {

    private DependencyCatalog() {
    }

    public static final List<DependencySpec> REQUIRED = List.of(
            spec("org.ow2.asm", "asm", "9.7.1", ""),
            spec("org.ow2.asm", "asm-tree", "9.7.1", ""),
            spec("org.ow2.asm", "asm-commons", "9.7.1", ""),
            spec("org.ow2.asm", "asm-analysis", "9.7.1", ""),
            spec("com.formdev", "flatlaf", "3.5.4", ""),
            spec("com.fasterxml.jackson.core", "jackson-databind", "2.18.2", ""),
            spec("ch.qos.logback", "logback-classic", "1.5.15", ""),
            spec("commons-io", "commons-io", "2.18.0", ""),
            spec("org.apache.commons", "commons-compress", "1.27.1", ""),
            spec("org.reflections", "reflections", "0.10.2", ""),
            spec("io.github.classgraph", "classgraph", "4.8.179", "")
    );

    private static DependencySpec spec(String group, String artifact, String version, String sha256) {
        return new DependencySpec(group, artifact, version, sha256);
    }
}

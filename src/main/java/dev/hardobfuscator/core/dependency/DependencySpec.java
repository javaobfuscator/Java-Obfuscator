package dev.hardobfuscator.core.dependency;

import java.util.Objects;

/**
 * Describes a single Maven Central artifact to be downloaded into libs/.
 */
public record DependencySpec(
        String groupId,
        String artifactId,
        String version,
        String sha256
) {
    public String mavenPath() {
        return groupId.replace('.', '/') + "/" + artifactId + "/" + version;
    }

    public String fileName() {
        return artifactId + "-" + version + ".jar";
    }

    public String mavenCentralUrl() {
        return "https://repo1.maven.org/maven2/" + mavenPath() + "/" + fileName();
    }

    public String cacheKey() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DependencySpec that)) return false;
        return cacheKey().equals(that.cacheKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cacheKey());
    }
}

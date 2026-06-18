package dev.hardobfuscator.plugins.api;

import dev.hardobfuscator.plugins.api.model.ClassEntry;
import dev.hardobfuscator.plugins.api.model.ResourceEntry;
import dev.hardobfuscator.plugins.api.naming.RenameMap;
import dev.hardobfuscator.plugins.api.stats.ObfuscationStatistics;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared mutable state passed through the entire obfuscation pipeline.
 */
public class Context {

    private final Path inputJar;
    private final Path outputJar;
    private final Map<String, ClassEntry> classes = new ConcurrentHashMap<>();
    private final Map<String, ResourceEntry> resources = new ConcurrentHashMap<>();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final RenameMap renameMap = new RenameMap();
    private final ObfuscationStatistics statistics = new ObfuscationStatistics();
    private final ExclusionPolicy exclusions;

    public Context(Path inputJar, Path outputJar, ExclusionPolicy exclusions) {
        this.inputJar = inputJar;
        this.outputJar = outputJar;
        this.exclusions = exclusions;
    }

    public Path inputJar() {
        return inputJar;
    }

    public Path outputJar() {
        return outputJar;
    }

    public Map<String, ClassEntry> classes() {
        return classes;
    }

    public Map<String, ResourceEntry> resources() {
        return resources;
    }

    public RenameMap renameMap() {
        return renameMap;
    }

    public ObfuscationStatistics statistics() {
        return statistics;
    }

    public ExclusionPolicy exclusions() {
        return exclusions;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public List<ClassEntry> mutableClassList() {
        return List.copyOf(classes.values());
    }

    public boolean isExcluded(String internalName) {
        return exclusions.isClassExcluded(internalName);
    }

    public boolean isMethodExcluded(String owner, String name, String desc) {
        return exclusions.isMethodExcluded(owner, name, desc);
    }

    public boolean isFieldExcluded(String owner, String name) {
        return exclusions.isFieldExcluded(owner, name);
    }
}

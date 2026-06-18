package dev.hardobfuscator.core.pipeline;

import dev.hardobfuscator.plugins.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry of all available transformers, including plugin contributions.
 */
public final class TransformerRegistry {

    private static final Logger log = LoggerFactory.getLogger(TransformerRegistry.class);
    private final Map<String, Transformer> transformers = new ConcurrentHashMap<>();

    public void register(Transformer transformer) {
        String name = transformer.name();
        if (transformers.containsKey(name)) {
            log.warn("Overwriting transformer registration: {}", name);
        }
        transformers.put(name, transformer);
        log.debug("Registered transformer: {}", name);
    }

    public void registerAll(Collection<Transformer> collection) {
        collection.forEach(this::register);
    }

    public Optional<Transformer> get(String name) {
        return Optional.ofNullable(transformers.get(name));
    }

    public List<Transformer> getAll() {
        return transformers.values().stream()
                .sorted(Comparator.comparing(Transformer::name))
                .collect(Collectors.toList());
    }

    public List<Transformer> resolveEnabled(Map<String, Boolean> config) {
        List<Transformer> enabled = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : config.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                Transformer t = transformers.get(entry.getKey());
                if (t != null) {
                    enabled.add(t);
                } else {
                    log.warn("Unknown transformer in config: {}", entry.getKey());
                }
            }
        }
        return enabled;
    }

    public int size() {
        return transformers.size();
    }
}

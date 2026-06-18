package dev.hardobfuscator.core.io;

import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.naming.RenameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Repairs META-INF after renaming: MANIFEST.MF main class, service loader files, etc.
 */
public final class MetaInfHandler {

    private static final Logger log = LoggerFactory.getLogger(MetaInfHandler.class);

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final List<String> CLASS_ATTRIBUTES = List.of(
            "Main-Class", "Start-Class", "Premain-Class", "Agent-Class", "Launcher-Agent-Class"
    );

    public void repair(Context context) {
        RenameMap renameMap = context.renameMap();
        repairManifest(context, renameMap);
        repairServiceLoaderFiles(context, renameMap);
    }

    private void repairManifest(Context context, RenameMap renameMap) {
        var resource = context.resources().get(MANIFEST_PATH);
        if (resource == null) {
            log.debug("No MANIFEST.MF found, skipping manifest repair");
            return;
        }

        try {
            Manifest manifest = new Manifest(new java.io.ByteArrayInputStream(resource.data()));
            Attributes main = manifest.getMainAttributes();
            boolean changed = false;

            for (String attr : CLASS_ATTRIBUTES) {
                String value = main.getValue(attr);
                if (value != null && !value.isBlank()) {
                    String remapped = renameMap.remapDottedClassName(value.trim());
                    if (!remapped.equals(value.trim())) {
                        main.putValue(attr, remapped);
                        log.info("Manifest {}: {} → {}", attr, value.trim(), remapped);
                        changed = true;
                    }
                }
            }

            for (Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
                Attributes attrs = entry.getValue();
                for (String attr : CLASS_ATTRIBUTES) {
                    String value = attrs.getValue(attr);
                    if (value != null && !value.isBlank()) {
                        String remapped = renameMap.remapDottedClassName(value.trim());
                        if (!remapped.equals(value.trim())) {
                            attrs.putValue(attr, remapped);
                            changed = true;
                        }
                    }
                }
            }

            if (changed) {
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                manifest.write(out);
                resource.setData(out.toByteArray());
                log.info("MANIFEST.MF repaired successfully");
            }
        } catch (Exception e) {
            log.warn("Failed to repair MANIFEST.MF: {}", e.getMessage());
            repairManifestFallback(resource, renameMap);
        }
    }

    private void repairManifestFallback(dev.hardobfuscator.plugins.api.model.ResourceEntry resource, RenameMap renameMap) {
        String text = new String(resource.data(), StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>();
        boolean changed = false;

        for (String line : text.split("\r?\n")) {
            String updated = line;
            for (String attr : CLASS_ATTRIBUTES) {
                String prefix = attr + ": ";
                if (line.startsWith(prefix)) {
                    String className = line.substring(prefix.length()).trim();
                    String remapped = renameMap.remapDottedClassName(className);
                    if (!remapped.equals(className)) {
                        updated = prefix + remapped;
                        log.info("Manifest {}: {} → {}", attr, className, remapped);
                        changed = true;
                    }
                }
            }
            lines.add(updated);
        }

        if (changed) {
            resource.setData(String.join("\r\n", lines).getBytes(StandardCharsets.UTF_8));
        }
    }

    private void repairServiceLoaderFiles(Context context, RenameMap renameMap) {
        for (var entry : context.resources().entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith("META-INF/services/")) {
                continue;
            }
            String content = new String(entry.getValue().data(), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            boolean changed = false;

            for (String line : content.split("\r?\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    sb.append(line).append('\n');
                    continue;
                }
                String remapped = renameMap.remapDottedClassName(trimmed);
                if (!remapped.equals(trimmed)) {
                    changed = true;
                }
                sb.append(remapped).append('\n');
            }

            if (changed) {
                entry.getValue().setData(sb.toString().getBytes(StandardCharsets.UTF_8));
                log.debug("Repaired service loader: {}", path);
            }
        }
    }
}

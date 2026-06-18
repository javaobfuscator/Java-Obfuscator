package dev.hardobfuscator.plugins.api.naming;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe mapping of original names to obfuscated names.
 */
public class RenameMap {

    private final Map<String, String> classes = new ConcurrentHashMap<>();
    private final Map<String, String> methods = new ConcurrentHashMap<>();
    private final Map<String, String> fields = new ConcurrentHashMap<>();
    private final Map<String, String> packages = new ConcurrentHashMap<>();

    public void mapClass(String original, String obfuscated) {
        classes.put(original, obfuscated);
    }

    public void mapMethod(String owner, String name, String desc, String newName) {
        methods.put(owner + "#" + name + desc, newName);
    }

    public void mapField(String owner, String name, String newName) {
        fields.put(owner + "." + name, newName);
    }

    public void mapPackage(String original, String obfuscated) {
        packages.put(original, obfuscated);
    }

    public String getClassMapping(String original) {
        return classes.getOrDefault(original, original);
    }

    /**
     * Remaps a dotted class name (e.g. pack.Main) using class and package rename mappings.
     */
    public String remapDottedClassName(String dottedName) {
        if (dottedName == null || dottedName.isBlank()) {
            return dottedName;
        }
        String internal = dottedName.replace('.', '/');
        String mapped = remapInternalName(internal);
        return mapped.replace('/', '.');
    }

    /**
     * Remaps an internal JVM class name (slash-separated).
     */
    public String remapInternalName(String internal) {
        if (classes.containsKey(internal)) {
            return classes.get(internal);
        }
        for (Map.Entry<String, String> pkg : packages.entrySet()) {
            String prefix = pkg.getKey() + "/";
            if (internal.startsWith(prefix)) {
                String suffix = internal.substring(prefix.length());
                return pkg.getValue() + "/" + suffix;
            }
        }
        int lastSlash = internal.lastIndexOf('/');
        if (lastSlash > 0) {
            String pkg = internal.substring(0, lastSlash);
            String simple = internal.substring(lastSlash + 1);
            String remappedPkg = packages.getOrDefault(pkg, pkg);
            String full = remappedPkg + "/" + simple;
            return classes.getOrDefault(full, full);
        }
        return classes.getOrDefault(internal, internal);
    }

    public String getMethodMapping(String owner, String name, String desc) {
        return methods.getOrDefault(owner + "#" + name + desc, name);
    }

    public String getFieldMapping(String owner, String name) {
        return fields.getOrDefault(owner + "." + name, name);
    }

    public String getPackageMapping(String original) {
        return packages.getOrDefault(original, original);
    }

    public Map<String, String> classMappings() {
        return Map.copyOf(classes);
    }

    public Map<String, String> methodMappings() {
        return Map.copyOf(methods);
    }

    public Map<String, String> fieldMappings() {
        return Map.copyOf(fields);
    }

    public int totalMappings() {
        return classes.size() + methods.size() + fields.size() + packages.size();
    }
}

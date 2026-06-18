package dev.hardobfuscator.plugins.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Glob-style exclusion rules for classes, methods, and fields.
 */
public class ExclusionPolicy {

    private final List<Pattern> classPatterns = new ArrayList<>();
    private final List<Pattern> methodPatterns = new ArrayList<>();
    private final List<Pattern> fieldPatterns = new ArrayList<>();
    private final List<String> preservedAnnotations = new ArrayList<>();
    private String targetPackagePrefix;

    public void addClassExclusion(String glob) {
        classPatterns.add(globToPattern(glob));
    }

    public void addMethodExclusion(String glob) {
        methodPatterns.add(globToPattern(glob));
    }

    public void addFieldExclusion(String glob) {
        fieldPatterns.add(globToPattern(glob));
    }

    public void addPreservedAnnotation(String descriptor) {
        preservedAnnotations.add(descriptor);
    }

    /**
     * Limits obfuscation to a single package tree. Empty value disables the limit.
     */
    public void setTargetPackage(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            targetPackagePrefix = null;
            return;
        }
        targetPackagePrefix = packageName.trim()
                .replace('.', '/')
                .replaceAll("^/+|/+$", "");
    }

    public String targetPackagePrefix() {
        return targetPackagePrefix;
    }

    public boolean isClassExcluded(String internalName) {
        if (targetPackagePrefix != null && !isInsideTargetPackage(internalName)) {
            return true;
        }
        String dotted = internalName.replace('/', '.');
        return classPatterns.stream().anyMatch(p -> p.matcher(dotted).matches());
    }

    private boolean isInsideTargetPackage(String internalName) {
        return internalName.equals(targetPackagePrefix)
                || internalName.startsWith(targetPackagePrefix + "/");
    }

    public boolean isMethodExcluded(String owner, String name, String desc) {
        String key = owner.replace('/', '.') + "#" + name + desc;
        return methodPatterns.stream().anyMatch(p -> p.matcher(key).matches());
    }

    public boolean isFieldExcluded(String owner, String name) {
        String key = owner.replace('/', '.') + "." + name;
        return fieldPatterns.stream().anyMatch(p -> p.matcher(key).matches());
    }

    public List<String> preservedAnnotations() {
        return List.copyOf(preservedAnnotations);
    }

    private static Pattern globToPattern(String glob) {
        String regex = glob
                .replace(".", "\\.")
                .replace("**", "§§")
                .replace("*", "[^.]*")
                .replace("§§", ".*");
        return Pattern.compile(regex);
    }
}

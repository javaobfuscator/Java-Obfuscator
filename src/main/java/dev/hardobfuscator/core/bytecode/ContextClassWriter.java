package dev.hardobfuscator.core.bytecode;

import dev.hardobfuscator.plugins.api.model.ClassEntry;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassWriter that resolves type hierarchies from the in-memory JAR instead of the JVM classpath.
 */
public final class ContextClassWriter extends ClassWriter {

    private final Map<String, ClassEntry> classes;
    private final Map<String, ClassReader> readerCache;
    private final Map<String, String> hierarchyCache = new ConcurrentHashMap<>();

    public ContextClassWriter(ClassReader reader, int flags, Map<String, ClassEntry> classes) {
        this(reader, flags, classes, new ConcurrentHashMap<>());
    }

    public ContextClassWriter(ClassReader reader, int flags,
                              Map<String, ClassEntry> classes,
                              Map<String, ClassReader> readerCache) {
        super(reader, flags);
        this.classes = classes;
        this.readerCache = readerCache;
    }

    public ContextClassWriter(int flags, Map<String, ClassEntry> classes) {
        super(flags);
        this.classes = classes;
        this.readerCache = new ConcurrentHashMap<>();
    }

    private static final int MAX_HIERARCHY_CACHE = 4_096;
    private static final int MAX_SUPER_CHAIN_DEPTH = 48;

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (hierarchyCache.size() > MAX_HIERARCHY_CACHE) {
            hierarchyCache.clear();
        }
        String cacheKey = type1 + "<:" + type2;
        return hierarchyCache.computeIfAbsent(cacheKey, k -> computeCommonSuperClass(type1, type2));
    }

    private String computeCommonSuperClass(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        if (isAssignableFrom(type1, type2)) {
            return type1;
        }
        if (isAssignableFrom(type2, type1)) {
            return type2;
        }
        if (isInterface(type1) || isInterface(type2)) {
            return "java/lang/Object";
        }
        String current = type1;
        int depth = 0;
        while (current != null && !"java/lang/Object".equals(current) && depth++ < MAX_SUPER_CHAIN_DEPTH) {
            if (isAssignableFrom(current, type2)) {
                return current;
            }
            current = superClassName(current);
        }
        return "java/lang/Object";
    }

    private boolean isInterface(String type) {
        ClassReader reader = readerFor(type);
        return reader != null && (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
    }

    private boolean isAssignableFrom(String superType, String subType) {
        if (superType.equals(subType)) {
            return true;
        }
        String current = subType;
        int depth = 0;
        while (current != null && depth++ < MAX_SUPER_CHAIN_DEPTH) {
            if (superType.equals(current)) {
                return true;
            }
            current = superClassName(current);
        }
        return "java/lang/Object".equals(superType);
    }

    private String superClassName(String type) {
        ClassReader reader = readerFor(type);
        if (reader == null) {
            return "java/lang/Object";
        }
        String superName = reader.getSuperName();
        return superName != null ? superName : "java/lang/Object";
    }

    private ClassReader readerFor(String internalName) {
        ClassReader cached = readerCache.get(internalName);
        if (cached != null) {
            return cached;
        }
        ClassEntry entry = classes.get(internalName);
        if (entry != null) {
            ClassReader reader = new ClassReader(entry.bytecode());
            readerCache.put(internalName, reader);
            return reader;
        }
        try {
            ClassReader reader = new ClassReader(internalName);
            readerCache.put(internalName, reader);
            return reader;
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }
}

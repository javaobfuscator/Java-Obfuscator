package dev.hardobfuscator.core.naming;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates collision-free obfuscated identifiers using mixed alphabets.
 */
public final class NameGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALL = LOWER + UPPER + DIGITS;

    private final SecureRandom random = new SecureRandom();
    private final Set<String> usedNames = new HashSet<>();
    private final String prefix;

    public NameGenerator() {
        this("");
    }

    public NameGenerator(String prefix) {
        this.prefix = prefix;
    }

    public String nextClassName() {
        return next("C");
    }

    public String nextMethodName() {
        return next("m");
    }

    public String nextFieldName() {
        return next("f");
    }

    public String nextPackageSegment() {
        return next("p").toLowerCase();
    }

    private String next(String typePrefix) {
        String name;
        do {
            int length = 8 + random.nextInt(8);
            StringBuilder sb = new StringBuilder(prefix).append(typePrefix);
            for (int i = 0; i < length; i++) {
                sb.append(ALL.charAt(random.nextInt(ALL.length())));
            }
            name = sb.toString();
        } while (!usedNames.add(name));
        return name;
    }

    public void reserve(String name) {
        usedNames.add(name);
    }

    public void clear() {
        usedNames.clear();
    }
}

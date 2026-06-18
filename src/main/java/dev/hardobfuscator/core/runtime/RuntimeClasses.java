package dev.hardobfuscator.core.runtime;

public final class RuntimeClasses {

    public static final String PREFIX = "dev/hardobfuscator/runtime/";

    private RuntimeClasses() {
    }

    public static boolean isRuntime(String internalName) {
        return internalName != null && internalName.startsWith(PREFIX);
    }
}

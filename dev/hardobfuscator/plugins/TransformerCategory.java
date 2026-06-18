package dev.hardobfuscator.plugins;

/**
 * Logical grouping for transformers in the selection UI and documentation.
 */
public enum TransformerCategory {
    RENAMING("Renaming"),
    STRING_PROTECTION("String Protection"),
    CONTROL_FLOW("Control Flow"),
    CONSTANTS("Constants"),
    METADATA("Metadata"),
    RESOURCES("Resources"),
    BYTECODE("Bytecode");

    private final String displayName;

    TransformerCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

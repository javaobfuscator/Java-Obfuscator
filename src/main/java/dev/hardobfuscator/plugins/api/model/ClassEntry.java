package dev.hardobfuscator.plugins.api.model;

/**
 * Represents a single .class file within the JAR being obfuscated.
 */
public class ClassEntry {

    private String internalName;
    private byte[] bytecode;
    private boolean modified;

    public ClassEntry(String internalName, byte[] bytecode) {
        this.internalName = internalName;
        this.bytecode = bytecode;
    }

    public String internalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
        this.modified = true;
    }

    public byte[] bytecode() {
        return bytecode;
    }

    public void setBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public void markModified() {
        this.modified = true;
    }
}

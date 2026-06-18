package dev.hardobfuscator.plugins.api.model;

/**
 * Represents a non-class resource entry inside the target JAR.
 */
public class ResourceEntry {

    private final String path;
    private byte[] data;
    private boolean encrypted;

    public ResourceEntry(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public String path() {
        return path;
    }

    public byte[] data() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
}

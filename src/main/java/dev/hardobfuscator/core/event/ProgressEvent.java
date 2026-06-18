package dev.hardobfuscator.core.event;

/**
 * Published to report incremental progress during obfuscation.
 */
public final class ProgressEvent extends ObfuscationEvent {

    private final int current;
    private final int total;
    private final String message;

    public ProgressEvent(int current, int total, String message) {
        this.current = current;
        this.total = total;
        this.message = message;
    }

    public int current() {
        return current;
    }

    public int total() {
        return total;
    }

    public String message() {
        return message;
    }

    public double percentage() {
        return total == 0 ? 0.0 : (current * 100.0) / total;
    }
}

package dev.hardobfuscator.core.event;

/**
 * Published before and after each transformer executes.
 */
public final class TransformerEvent extends ObfuscationEvent {

    public enum Phase { START, COMPLETE, ERROR }

    private final String transformerName;
    private final Phase phase;
    private final long durationMs;
    private final String errorMessage;

    private TransformerEvent(String transformerName, Phase phase, long durationMs, String errorMessage) {
        this.transformerName = transformerName;
        this.phase = phase;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
    }

    public static TransformerEvent start(String name) {
        return new TransformerEvent(name, Phase.START, 0, null);
    }

    public static TransformerEvent complete(String name, long durationMs) {
        return new TransformerEvent(name, Phase.COMPLETE, durationMs, null);
    }

    public static TransformerEvent error(String name, String message) {
        return new TransformerEvent(name, Phase.ERROR, 0, message);
    }

    public String transformerName() {
        return transformerName;
    }

    public Phase phase() {
        return phase;
    }

    public long durationMs() {
        return durationMs;
    }

    public String errorMessage() {
        return errorMessage;
    }
}

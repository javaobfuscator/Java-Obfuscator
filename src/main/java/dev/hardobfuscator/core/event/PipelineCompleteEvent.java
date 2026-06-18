package dev.hardobfuscator.core.event;

/**
 * Published when the obfuscation pipeline completes successfully.
 */
public final class PipelineCompleteEvent extends ObfuscationEvent {

    private final String outputPath;
    private final long durationMs;

    public PipelineCompleteEvent(String outputPath, long durationMs) {
        this.outputPath = outputPath;
        this.durationMs = durationMs;
    }

    public String outputPath() {
        return outputPath;
    }

    public long durationMs() {
        return durationMs;
    }
}

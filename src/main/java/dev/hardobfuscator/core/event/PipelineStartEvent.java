package dev.hardobfuscator.core.event;

/**
 * Published when the obfuscation pipeline starts.
 */
public final class PipelineStartEvent extends ObfuscationEvent {

    private final String inputPath;

    public PipelineStartEvent(String inputPath) {
        this.inputPath = inputPath;
    }

    public String inputPath() {
        return inputPath;
    }
}

package dev.hardobfuscator.core.event;

import java.time.Instant;

/**
 * Base type for all obfuscation lifecycle events.
 */
public abstract class ObfuscationEvent {

    private final Instant timestamp = Instant.now();

    public Instant timestamp() {
        return timestamp;
    }
}

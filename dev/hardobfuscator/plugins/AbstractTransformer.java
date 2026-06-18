package dev.hardobfuscator.plugins;

import dev.hardobfuscator.plugins.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class providing common logging and exclusion-check utilities for transformers.
 */
public abstract class AbstractTransformer implements Transformer {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void transform(Context context) {
        log.info("Starting transformer: {}", name());
        long start = System.nanoTime();
        doTransform(context);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        log.info("Finished transformer: {} ({} ms)", name(), elapsed);
    }

    protected abstract void doTransform(Context context);
}

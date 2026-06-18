package dev.hardobfuscator.examples.plugin;

import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.Plugin;
import dev.hardobfuscator.plugins.Transformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;

import java.util.List;

/**
 * Example third-party plugin demonstrating the HardObfuscator Plugin API.
 */
public final class ExamplePlugin implements Plugin {

    @Override
    public String name() {
        return "Example Plugin";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void initialize() {
        // Plugin initialization logic
    }

    @Override
    public List<Transformer> transformers() {
        return List.of(new ExampleMarkerTransformer());
    }

  /**
   * Adds a synthetic marker interface to demonstrate plugin transformer registration.
   */
    static final class ExampleMarkerTransformer extends AbstractTransformer {

        @Override
        public String name() {
            return "exampleMarker";
        }

        @Override
        public String description() {
            return "Example plugin transformer (no-op marker)";
        }

        @Override
        public TransformerCategory category() {
            return TransformerCategory.METADATA;
        }

        @Override
        protected void doTransform(Context context) {
            log.info("Example plugin transformer executed on {} classes", context.classes().size());
        }
    }
}

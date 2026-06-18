package dev.hardobfuscator.transformers.metadata;

import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;

/**
 * Strips debug information: source file names, line numbers, and local variable tables.
 */
public final class DebugRemovalTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "debugRemoval";
    }

    @Override
    public String description() {
        return "Removes debug metadata (source, lines, local variables)";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.METADATA;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.stripDebugInfo(context);
    }
}

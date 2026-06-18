package dev.hardobfuscator.transformers;

import dev.hardobfuscator.plugins.Transformer;
import dev.hardobfuscator.transformers.bytecode.InstructionMutationTransformer;
import dev.hardobfuscator.transformers.bytecode.StackMutationTransformer;
import dev.hardobfuscator.transformers.constant.ConstantMutationTransformer;
import dev.hardobfuscator.transformers.constant.NumberHidingTransformer;
import dev.hardobfuscator.transformers.flow.BogusJumpTransformer;
import dev.hardobfuscator.transformers.flow.ControlFlowTransformer;
import dev.hardobfuscator.transformers.flow.DeadCodeTransformer;
import dev.hardobfuscator.transformers.metadata.AnnotationTransformer;
import dev.hardobfuscator.transformers.metadata.DebugRemovalTransformer;
import dev.hardobfuscator.transformers.rename.ClassRenameTransformer;
import dev.hardobfuscator.transformers.rename.FieldRenameTransformer;
import dev.hardobfuscator.transformers.rename.MethodRenameTransformer;
import dev.hardobfuscator.transformers.rename.PackageRenameTransformer;
import dev.hardobfuscator.transformers.resource.ResourceEncryptionTransformer;
import dev.hardobfuscator.transformers.string.StringEncryptionTransformer;

import java.util.List;

/**
 * Provides the complete set of built-in transformers shipped with HardObfuscator.
 */
public final class BuiltinTransformers {

    private BuiltinTransformers() {
    }

    public static List<Transformer> all() {
        return List.of(
                new ClassRenameTransformer(),
                new MethodRenameTransformer(),
                new FieldRenameTransformer(),
                new PackageRenameTransformer(),
                new StringEncryptionTransformer(),
                new ControlFlowTransformer(),
                new BogusJumpTransformer(),
                new DeadCodeTransformer(),
                new NumberHidingTransformer(),
                new ConstantMutationTransformer(),
                new DebugRemovalTransformer(),
                new AnnotationTransformer(),
                new ResourceEncryptionTransformer(),
                new StackMutationTransformer(),
                new InstructionMutationTransformer()
        );
    }
}

package dev.hardobfuscator.transformers.flow;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

/**
 * Injects harmless dead code at method entry (memory-light, no MethodNode).
 */
public final class DeadCodeTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "deadCode";
    }

    @Override
    public String description() {
        return "Injects unreachable dead code blocks into methods";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.CONTROL_FLOW;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.transformAllParallel(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ("<clinit>".equals(name) || (access & Opcodes.ACC_ABSTRACT) != 0) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    private boolean injected;

                    @Override
                    public void visitCode() {
                        if (!injected) {
                            super.visitInsn(Opcodes.ICONST_0);
                            super.visitInsn(Opcodes.ICONST_0);
                            super.visitInsn(Opcodes.IMUL);
                            super.visitInsn(Opcodes.POP);
                            injected = true;
                        }
                    }
                };
            }
        }, ClassTransformerHelper.FrameMode.COMPUTE_MAXS, "Dead code");
    }
}

package dev.hardobfuscator.transformers.bytecode;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

/**
 * Inserts harmless stack manipulation instructions (DUP/POP pairs).
 */
public final class StackMutationTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "stackMutation";
    }

    @Override
    public String description() {
        return "Inserts harmless stack manipulation instructions";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.BYTECODE;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.transformAll(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    private int insnCount;

                    @Override
                    public void visitInsn(int opcode) {
                        super.visitInsn(opcode);
                        if (++insnCount % 7 == 0 && opcode != Opcodes.RETURN) {
                            super.visitInsn(Opcodes.ICONST_0);
                            super.visitInsn(Opcodes.POP);
                        }
                    }
                };
            }
        });
    }
}

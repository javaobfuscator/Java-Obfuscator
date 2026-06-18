package dev.hardobfuscator.transformers.constant;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

/**
 * Mutates long and float constants using reversible bitwise operations.
 */
public final class ConstantMutationTransformer extends AbstractTransformer {

    private static final int XOR_MASK = 0x5A5A5A5A;

    @Override
    public String name() {
        return "constantMutation";
    }

    @Override
    public String description() {
        return "Mutates constant pool values using reversible transformations";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.CONSTANTS;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.transformAll(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof Long longVal) {
                            long mutated = longVal ^ XOR_MASK;
                            super.visitLdcInsn(mutated);
                            super.visitLdcInsn((long) XOR_MASK);
                            super.visitInsn(Opcodes.LXOR);
                        } else if (value instanceof Float floatVal) {
                            int bits = Float.floatToIntBits(floatVal);
                            int mutated = bits ^ XOR_MASK;
                            super.visitLdcInsn(mutated);
                            super.visitLdcInsn(XOR_MASK);
                            super.visitInsn(Opcodes.IXOR);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "intBitsToFloat",
                                    "(I)F", false);
                        } else {
                            super.visitLdcInsn(value);
                        }
                    }
                };
            }
        });
    }
}

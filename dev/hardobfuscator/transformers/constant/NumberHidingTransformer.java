package dev.hardobfuscator.transformers.constant;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Replaces integer constants with equivalent arithmetic expressions.
 */
public final class NumberHidingTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "numberHiding";
    }

    @Override
    public String description() {
        return "Hides numeric constants behind arithmetic expressions";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.CONSTANTS;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.transformAllParallel(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    @Override
                    public void visitIntInsn(int opcode, int operand) {
                        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
                            hideInt(operand);
                        } else {
                            super.visitIntInsn(opcode, operand);
                        }
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof Integer intVal) {
                            hideInt(intVal);
                        } else {
                            super.visitLdcInsn(value);
                        }
                    }

                    private void hideInt(int value) {
                        int a = ThreadLocalRandom.current().nextInt(1000) + 1;
                        int b = value - a;
                        super.visitLdcInsn(a);
                        super.visitLdcInsn(b);
                        super.visitInsn(Opcodes.IADD);
                    }
                };
            }
        }, ClassTransformerHelper.FrameMode.COMPUTE_MAXS, "Number hiding");
    }
}

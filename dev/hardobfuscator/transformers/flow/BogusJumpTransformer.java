package dev.hardobfuscator.transformers.flow;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * Inserts bogus conditional jumps at method entry (skips very large methods).
 */
public final class BogusJumpTransformer extends AbstractTransformer {

    private static final int MAX_METHOD_INSNS = 2_500;

    @Override
    public String name() {
        return "bogusJumps";
    }

    @Override
    public String description() {
        return "Inserts fake conditional jumps to confuse decompilers";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.CONTROL_FLOW;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.transformAllHeavy(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ("<clinit>".equals(name) || (access & Opcodes.ACC_ABSTRACT) != 0) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                return new BogusMethodNode(access, name, descriptor, signature, exceptions,
                        super.visitMethod(access, name, descriptor, signature, exceptions));
            }
        }, "Bogus jumps");
    }

    private static final class BogusMethodNode extends MethodNode {

        private final MethodVisitor downstream;

        BogusMethodNode(int access, String name, String descriptor, String signature,
                        String[] exceptions, MethodVisitor downstream) {
            super(BytecodeUtil.ASM_VERSION, access, name, descriptor, signature, exceptions);
            this.downstream = downstream;
        }

        @Override
        public void visitEnd() {
            if (downstream == null) {
                return;
            }
            if (instructions.size() > 0 && instructions.size() <= MAX_METHOD_INSNS) {
                instructions.insert(buildBogusChain());
            }
            accept(downstream);
        }
    }

    private static InsnList buildBogusChain() {
        InsnList list = new InsnList();
        LabelNode real = new LabelNode();
        LabelNode fake = new LabelNode();

        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPNE, fake));
        list.add(new JumpInsnNode(Opcodes.GOTO, real));
        list.add(fake);
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        list.add(new InsnNode(Opcodes.ATHROW));
        list.add(real);
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new JumpInsnNode(Opcodes.IFNE, real));
        return list;
    }
}

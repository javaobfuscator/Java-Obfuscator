package dev.hardobfuscator.transformers.flow;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Control-flow obfuscation at method entry. Large methods are skipped to avoid hangs.
 */
public final class ControlFlowTransformer extends AbstractTransformer {

    /** Methods larger than this are left untouched (frame recompute becomes too slow). */
    private static final int MAX_METHOD_INSNS = 2_500;
    private static final int MAX_CLASS_BYTES = 120_000;

    @Override
    public String name() {
        return "controlFlow";
    }

    @Override
    public String description() {
        return "Opaque predicates and bogus branches at method entry";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.CONTROL_FLOW;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    protected void doTransform(Context context) {
        ClassTransformerHelper.transformAllHeavy(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            private boolean classTooLarge;

            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                classTooLarge = reader.getClassName() != null
                        && entrySize(reader) > MAX_CLASS_BYTES;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if (classTooLarge || shouldSkip(access, name)) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                return new FlowMethodNode(access, name, descriptor, signature, exceptions,
                        super.visitMethod(access, name, descriptor, signature, exceptions));
            }
        }, "Control flow");
    }

    private static int entrySize(ClassReader reader) {
        return reader.b.length;
    }

    private static boolean shouldSkip(int access, String name) {
        return "<init>".equals(name) || "<clinit>".equals(name)
                || (access & Opcodes.ACC_ABSTRACT) != 0
                || (access & Opcodes.ACC_NATIVE) != 0;
    }

    private static final class FlowMethodNode extends MethodNode {

        private final MethodVisitor downstream;

        FlowMethodNode(int access, String name, String descriptor, String signature,
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
                instructions.insert(buildEntryObfuscation());
            }
            accept(downstream);
        }
    }

    private static InsnList buildEntryObfuscation() {
        InsnList list = new InsnList();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int x = r.nextInt(50, 200);
        int y = r.nextInt(50, 200);

        LabelNode real = new LabelNode();
        list.add(new LdcInsnNode(x));
        list.add(new LdcInsnNode(y));
        list.add(new InsnNode(Opcodes.IMUL));
        list.add(new LdcInsnNode(x * y));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, real));
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        list.add(new InsnNode(Opcodes.ATHROW));
        list.add(real);

        LabelNode real2 = new LabelNode();
        LabelNode fake = new LabelNode();
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPNE, fake));
        list.add(new JumpInsnNode(Opcodes.GOTO, real2));
        list.add(fake);
        list.add(new InsnNode(Opcodes.NOP));
        list.add(new JumpInsnNode(Opcodes.GOTO, real2));
        list.add(real2);

        LabelNode real3 = new LabelNode();
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new InsnNode(Opcodes.IXOR));
        list.add(new JumpInsnNode(Opcodes.IFEQ, real3));
        list.add(new InsnNode(Opcodes.NOP));
        list.add(real3);

        return list;
    }
}

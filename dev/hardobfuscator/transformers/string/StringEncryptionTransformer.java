package dev.hardobfuscator.transformers.string;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aggressive multi-layer string encryption with junk bytecode before each decrypt call.
 */
public final class StringEncryptionTransformer extends AbstractTransformer {

    private static final String DECRYPTOR = "dev/hardobfuscator/runtime/StringDecryptor";

    @Override
    public String name() {
        return "stringEncryption";
    }

    @Override
    public String description() {
        return "Multi-layer string encryption with junk bytecode and runtime decryption";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.STRING_PROTECTION;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    protected void doTransform(Context context) {
        String key = resolveKey(context);
        AtomicInteger count = new AtomicInteger();

        ClassTransformerHelper.transformAllParallel(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ((access & Opcodes.ACC_ABSTRACT) != 0 || (access & Opcodes.ACC_NATIVE) != 0) {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String str && !str.isEmpty()) {
                            int index = count.getAndIncrement();
                            int salt = StringCipher.saltFor(str, index);
                            String encrypted = StringCipher.encrypt(str, key, index, salt);
                            emitJunkBytecode();
                            super.visitLdcInsn(encrypted);
                            super.visitLdcInsn(index);
                            super.visitLdcInsn(salt);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, DECRYPTOR, "decryptHeavy",
                                    "(Ljava/lang/String;II)Ljava/lang/String;", false);
                        } else {
                            super.visitLdcInsn(value);
                        }
                    }

                    private void emitJunkBytecode() {
                        ThreadLocalRandom r = ThreadLocalRandom.current();
                        int a = r.nextInt(100, 500);
                        int b = r.nextInt(100, 500);
                        super.visitLdcInsn(a);
                        super.visitLdcInsn(b);
                        super.visitInsn(Opcodes.IMUL);
                        super.visitLdcInsn(a + b);
                        super.visitInsn(Opcodes.IADD);
                        super.visitInsn(Opcodes.POP);
                    }
                };
            }
        }, ClassTransformerHelper.FrameMode.COMPUTE_MAXS, "String encryption");

        context.statistics().addStringsEncrypted(count.get());
        context.setAttribute("stringEncryptionKey", key);
        log.info("Encrypted {} strings (multi-layer)", count.get());
    }

    private String resolveKey(Context context) {
        Object key = context.getAttribute("runtimeKey");
        if (key instanceof String s && !s.isBlank()) {
            return s;
        }
        return "hardobfuscator-default-key";
    }

    public static String encrypt(String input, String key) {
        return StringCipher.encrypt(input, key, 0, 0);
    }
}

package dev.hardobfuscator.core.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Utility methods for ASM bytecode read/write operations.
 */
public final class BytecodeUtil {

    public static final int ASM_VERSION = Opcodes.ASM9;

    private BytecodeUtil() {
    }

    public static ClassWriter createWriter(int flags) {
        return new ClassWriter(flags);
    }

    public static ClassWriter createWriter(ClassReader reader, int flags) {
        return new ClassWriter(reader, flags);
    }

    public static byte[] toBytecode(ClassVisitor visitor, ClassWriter writer) {
        return writer.toByteArray();
    }

    public static ClassReader read(byte[] bytecode) {
        return new ClassReader(bytecode);
    }

    public static boolean isInterface(int access) {
        return (access & Opcodes.ACC_INTERFACE) != 0;
    }

    public static boolean isEnum(int access) {
        return (access & Opcodes.ACC_ENUM) != 0;
    }

    public static boolean isSynthetic(int access) {
        return (access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    public static boolean isBridge(int access) {
        return (access & Opcodes.ACC_BRIDGE) != 0;
    }
}

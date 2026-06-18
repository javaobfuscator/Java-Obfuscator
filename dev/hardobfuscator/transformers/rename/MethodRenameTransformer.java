package dev.hardobfuscator.transformers.rename;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.core.runtime.RuntimeClasses;
import dev.hardobfuscator.core.naming.NameGenerator;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

/**
 * Renames method names in scoped classes and updates all invoke instructions across the JAR.
 */
public final class MethodRenameTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "methodRename";
    }

    @Override
    public String description() {
        return "Renames method names and updates all call sites";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.RENAMING;
    }

    @Override
    public boolean enabledByDefault() {
        return true;
    }

    @Override
    protected void doTransform(Context context) {
        NameGenerator names = new NameGenerator();
        int mapped = collectMethodMappings(context, names);
        if (mapped == 0) {
            log.info("No methods to rename in current scope");
            return;
        }

        ClassTransformerHelper.transformEntireJar(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            private String className;
            private boolean inScope;

            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                this.className = name;
                this.inScope = !context.isExcluded(name);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                String declaredName = inScope && !RuntimeClasses.isRuntime(className)
                        ? context.renameMap().getMethodMapping(className, name, descriptor)
                        : name;
                MethodVisitor mv = super.visitMethod(access, declaredName, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName,
                                                String methodDesc, boolean isInterface) {
                        String mappedOwner = context.renameMap().remapInternalName(owner);
                        String mappedMethod = RuntimeClasses.isRuntime(mappedOwner)
                                ? methodName
                                : context.renameMap().getMethodMapping(mappedOwner, methodName, methodDesc);
                        super.visitMethodInsn(opcode, mappedOwner, mappedMethod, methodDesc, isInterface);
                    }
                };
            }
        }, "Method rename");

        log.info("Renamed {} methods", mapped);
    }

    private int collectMethodMappings(Context context, NameGenerator names) {
        int count = 0;
        for (ClassEntry entry : context.mutableClassList()) {
            if (context.isExcluded(entry.internalName()) || RuntimeClasses.isRuntime(entry.internalName())) {
                continue;
            }
            String owner = entry.internalName();
            ClassReader reader = BytecodeUtil.read(entry.bytecode());
            final int[] local = {0};
            reader.accept(new ClassVisitor(BytecodeUtil.ASM_VERSION) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (shouldRename(context, owner, access, name, descriptor)) {
                        String newName = names.nextMethodName();
                        context.renameMap().mapMethod(owner, name, descriptor, newName);
                        local[0]++;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            count += local[0];
        }
        context.statistics().addMethodsRenamed(count);
        return count;
    }

    private static boolean shouldRename(Context context, String owner, int access,
                                        String name, String descriptor) {
        if ("<init>".equals(name) || "<clinit>".equals(name) || "main".equals(name)) {
            return false;
        }
        if ((access & Opcodes.ACC_NATIVE) != 0) {
            return false;
        }
        if ((access & Opcodes.ACC_BRIDGE) != 0) {
            return false;
        }
        if (context.isMethodExcluded(owner, name, descriptor)) {
            return false;
        }
        return true;
    }
}

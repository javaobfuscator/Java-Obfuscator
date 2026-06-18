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
 * Renames non-public instance field names and updates all field access instructions.
 */
public final class FieldRenameTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "fieldRename";
    }

    @Override
    public String description() {
        return "Renames field names to meaningless identifiers";
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
        collectFieldMappings(context, names);

        ClassTransformerHelper.transformAll(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            String className;

            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                this.className = name;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor,
                                           String signature, Object value) {
                String mapped = context.renameMap().getFieldMapping(className, name);
                return super.visitField(access, mapped, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDesc) {
                        if (RuntimeClasses.isRuntime(owner)) {
                            super.visitFieldInsn(opcode, owner, fieldName, fieldDesc);
                            return;
                        }
                        super.visitFieldInsn(opcode, owner,
                                context.renameMap().getFieldMapping(owner, fieldName), fieldDesc);
                    }
                };
            }
        });
    }

    private void collectFieldMappings(Context context, NameGenerator names) {
        for (ClassEntry entry : context.mutableClassList()) {
            if (context.isExcluded(entry.internalName()) || RuntimeClasses.isRuntime(entry.internalName())) {
                continue;
            }
            ClassReader reader = BytecodeUtil.read(entry.bytecode());
            reader.accept(new ClassVisitor(BytecodeUtil.ASM_VERSION) {
                private String className;

                @Override
                public void visit(int version, int access, String name, String signature,
                                  String superName, String[] interfaces) {
                    this.className = name;
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor,
                                               String signature, Object value) {
                    if (shouldRename(context, className, access, name)) {
                        String newName = names.nextFieldName();
                        context.renameMap().mapField(className, name, newName);
                        context.statistics().incrementFieldsRenamed();
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }

    private static boolean shouldRename(Context context, String owner, int access, String name) {
        if (context.isFieldExcluded(owner, name)) {
            return false;
        }
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            return false;
        }
        // Keep static fields (MAPPER, loggers, constants) — bytecode references stay valid.
        if ((access & Opcodes.ACC_STATIC) != 0) {
            return false;
        }
        return true;
    }
}

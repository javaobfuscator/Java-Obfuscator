package dev.hardobfuscator.transformers.metadata;

import dev.hardobfuscator.core.bytecode.BytecodeUtil;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.*;

import java.util.List;

/**
 * Removes non-essential annotations while preserving configured annotation types.
 */
public final class AnnotationTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "annotationManipulation";
    }

    @Override
    public String description() {
        return "Strips non-essential annotations from classes and members";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.METADATA;
    }

    @Override
    protected void doTransform(Context context) {
        List<String> preserved = context.exclusions().preservedAnnotations();

        ClassTransformerHelper.transformAll(context, (reader, writer) -> new ClassVisitor(BytecodeUtil.ASM_VERSION, writer) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (shouldPreserve(descriptor, preserved)) {
                    return super.visitAnnotation(descriptor, visible);
                }
                return null;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(BytecodeUtil.ASM_VERSION, mv) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        if (shouldPreserve(desc, preserved)) {
                            return super.visitAnnotation(desc, visible);
                        }
                        return null;
                    }
                };
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor,
                                           String signature, Object value) {
                FieldVisitor fv = super.visitField(access, name, descriptor, signature, value);
                return new FieldVisitor(BytecodeUtil.ASM_VERSION, fv) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        if (shouldPreserve(desc, preserved)) {
                            return super.visitAnnotation(desc, visible);
                        }
                        return null;
                    }
                };
            }
        }, ClassTransformerHelper.FrameMode.PRESERVE);
    }

    private boolean shouldPreserve(String descriptor, List<String> preserved) {
        return preserved.stream().anyMatch(descriptor::contains);
    }
}

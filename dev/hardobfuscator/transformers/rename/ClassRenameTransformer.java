package dev.hardobfuscator.transformers.rename;

import dev.hardobfuscator.core.runtime.RuntimeClasses;
import dev.hardobfuscator.core.naming.NameGenerator;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Renames class identifiers while preserving inheritance relationships.
 */
public final class ClassRenameTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "classRename";
    }

    @Override
    public String description() {
        return "Renames class names to meaningless identifiers";
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
        Map<String, String> mapping = new HashMap<>();

        for (ClassEntry entry : context.mutableClassList()) {
            String original = entry.internalName();
            if (context.isExcluded(original)) {
                continue;
            }
            if (original.startsWith(RuntimeClasses.PREFIX)) {
                continue;
            }
            String obfuscated = names.nextClassName();
            mapping.put(original, obfuscated);
            context.renameMap().mapClass(original, obfuscated);
            context.statistics().incrementClassesRenamed();
        }

        if (mapping.isEmpty()) {
            return;
        }

        SimpleRemapper remapper = new SimpleRemapper(mapping);
        Map<String, ClassEntry> updated = new HashMap<>();

        for (ClassEntry entry : context.mutableClassList()) {
            String internalName = entry.internalName();
            if (context.isExcluded(internalName)) {
                updated.put(internalName, entry);
                continue;
            }
            if (internalName.startsWith(RuntimeClasses.PREFIX)) {
                updated.put(internalName, entry);
                continue;
            }
            if (!mapping.containsKey(internalName)) {
                updated.put(internalName, entry);
                continue;
            }
            ClassTransformerHelper.transformClass(context, entry, (reader, writer) ->
                    new ClassRemapper(writer, remapper));
            String newName = mapping.get(internalName);
            if (!newName.equals(internalName)) {
                entry.setInternalName(newName);
            }
            updated.put(entry.internalName(), entry);
        }

        context.classes().clear();
        context.classes().putAll(updated);
        log.info("Class rename complete: {} renamed, {} preserved",
                mapping.size(), updated.size() - mapping.size());
    }
}

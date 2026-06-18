package dev.hardobfuscator.transformers.rename;

import dev.hardobfuscator.core.naming.NameGenerator;
import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import dev.hardobfuscator.transformers.base.ClassTransformerHelper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Flatten package hierarchy by remapping package segments.
 */
public final class PackageRenameTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "packageRename";
    }

    @Override
    public String description() {
        return "Collapses package structure into a flat obfuscated namespace";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.RENAMING;
    }

    @Override
    protected void doTransform(Context context) {
        NameGenerator names = new NameGenerator();
        Map<String, String> packageMapping = new HashMap<>();

        for (ClassEntry entry : context.mutableClassList()) {
            String pkg = extractPackage(entry.internalName());
            if (pkg.isEmpty() || context.isExcluded(entry.internalName())) {
                continue;
            }
            packageMapping.computeIfAbsent(pkg, k -> {
                String obf = names.nextPackageSegment();
                context.renameMap().mapPackage(k, obf);
                return obf;
            });
        }

        Map<String, String> classMapping = new HashMap<>();
        for (ClassEntry entry : context.mutableClassList()) {
            String original = entry.internalName();
            String pkg = extractPackage(original);
            if (packageMapping.containsKey(pkg)) {
                String simpleName = original.substring(pkg.length() + 1);
                String newName = packageMapping.get(pkg) + "/" + simpleName;
                classMapping.put(original, newName);
            }
        }

        if (classMapping.isEmpty()) {
            return;
        }

        SimpleRemapper remapper = new SimpleRemapper(classMapping);
        Map<String, ClassEntry> updated = new HashMap<>();

        for (ClassEntry entry : context.mutableClassList()) {
            if (context.isExcluded(entry.internalName())) {
                updated.put(entry.internalName(), entry);
                continue;
            }
            String original = entry.internalName();
            if (!classMapping.containsKey(original)) {
                updated.put(original, entry);
                continue;
            }
            ClassTransformerHelper.transformClass(context, entry, (reader, writer) ->
                    new ClassRemapper(writer, remapper));
            String newName = classMapping.get(original);
            if (!newName.equals(original)) {
                entry.setInternalName(newName);
            }
            updated.put(entry.internalName(), entry);
        }

        context.classes().clear();
        context.classes().putAll(updated);
    }

    private String extractPackage(String internalName) {
        int idx = internalName.lastIndexOf('/');
        return idx < 0 ? "" : internalName.substring(0, idx);
    }
}

package dev.hardobfuscator.transformers.resource;

import dev.hardobfuscator.plugins.AbstractTransformer;
import dev.hardobfuscator.plugins.TransformerCategory;
import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ResourceEntry;
import dev.hardobfuscator.transformers.string.StringEncryptionTransformer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encrypts non-class resources and marks them for runtime decryption.
 */
public final class ResourceEncryptionTransformer extends AbstractTransformer {

    @Override
    public String name() {
        return "resourceEncryption";
    }

    @Override
    public String description() {
        return "Encrypts JAR resources with XOR-based encoding";
    }

    @Override
    public TransformerCategory category() {
        return TransformerCategory.RESOURCES;
    }

    @Override
    protected void doTransform(Context context) {
        String key = "hardobfuscator-resource-key";
        Object runtimeKey = context.getAttribute("runtimeKey");
        if (runtimeKey instanceof String s && !s.isBlank()) {
            key = s;
        }

        for (ResourceEntry resource : context.resources().values()) {
            if (resource.path().endsWith(".class") || resource.path().startsWith("META-INF/")) {
                continue;
            }
            String encoded = StringEncryptionTransformer.encrypt(
                    Base64.getEncoder().encodeToString(resource.data()), key);
            resource.setData(("AGENC:" + encoded).getBytes(StandardCharsets.UTF_8));
            resource.setEncrypted(true);
            context.statistics().incrementResourcesEncrypted();
        }
    }
}

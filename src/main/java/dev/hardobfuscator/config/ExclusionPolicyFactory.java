package dev.hardobfuscator.config;

import dev.hardobfuscator.plugins.api.ExclusionPolicy;

/**
 * Builds an {@link ExclusionPolicy} from configuration exclusions.
 */
public final class ExclusionPolicyFactory {

    private ExclusionPolicyFactory() {
    }

    public static ExclusionPolicy fromConfig(ObfuscationConfig config) {
        ExclusionPolicy policy = new ExclusionPolicy();
        ExclusionConfig exclusions = config.getExclusions();
        exclusions.getClasses().forEach(policy::addClassExclusion);
        exclusions.getMethods().forEach(policy::addMethodExclusion);
        exclusions.getFields().forEach(policy::addFieldExclusion);
        exclusions.getAnnotations().forEach(policy::addPreservedAnnotation);
        policy.setTargetPackage(config.getTargetPackage());
        return policy;
    }
}

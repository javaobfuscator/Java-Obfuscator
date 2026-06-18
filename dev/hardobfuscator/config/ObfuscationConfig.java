package dev.hardobfuscator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root configuration model deserialized from JSON configuration files.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObfuscationConfig {

    private String input;
    private String output;

    /** When set, only classes in this package (and subpackages) are obfuscated. */
    private String targetPackage;

    @JsonProperty("transformers")
    private Map<String, Boolean> transformers = new HashMap<>();

    private ExclusionConfig exclusions = new ExclusionConfig();
    private RuntimeConfig runtime = new RuntimeConfig();
    private AdvancedConfig advanced = new AdvancedConfig();

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = (targetPackage == null || targetPackage.isBlank()) ? null : targetPackage.trim();
    }

    public Map<String, Boolean> getTransformers() {
        return transformers;
    }

    public void setTransformers(Map<String, Boolean> transformers) {
        this.transformers = transformers;
    }

    public ExclusionConfig getExclusions() {
        return exclusions;
    }

    public void setExclusions(ExclusionConfig exclusions) {
        this.exclusions = exclusions;
    }

    public RuntimeConfig getRuntime() {
        return runtime;
    }

    public void setRuntime(RuntimeConfig runtime) {
        this.runtime = runtime;
    }

    public AdvancedConfig getAdvanced() {
        return advanced;
    }

    public void setAdvanced(AdvancedConfig advanced) {
        this.advanced = advanced;
    }

    public boolean isTransformerEnabled(String name) {
        return transformers.getOrDefault(name, false);
    }
}

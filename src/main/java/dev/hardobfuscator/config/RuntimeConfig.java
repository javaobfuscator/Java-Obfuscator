package dev.hardobfuscator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeConfig {

    private boolean injectRuntime = true;
    private String encryptionKey;
    private boolean integrityCheck = false;
    private String encryptionMode = "XOR_ROTATE";

    public boolean isInjectRuntime() {
        return injectRuntime;
    }

    public void setInjectRuntime(boolean injectRuntime) {
        this.injectRuntime = injectRuntime;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isIntegrityCheck() {
        return integrityCheck;
    }

    public void setIntegrityCheck(boolean integrityCheck) {
        this.integrityCheck = integrityCheck;
    }

    public String getEncryptionMode() {
        return encryptionMode;
    }

    public void setEncryptionMode(String encryptionMode) {
        this.encryptionMode = encryptionMode;
    }
}

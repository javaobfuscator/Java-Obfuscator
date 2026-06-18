package dev.hardobfuscator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvancedConfig {

    private int threads = Runtime.getRuntime().availableProcessors();
    private int batchSize = 16;
    private String logLevel = "INFO";
    private boolean parallelProcessing = true;
    private boolean memoryOptimization = true;

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isParallelProcessing() {
        return parallelProcessing;
    }

    public void setParallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing;
    }

    public boolean isMemoryOptimization() {
        return memoryOptimization;
    }

    public void setMemoryOptimization(boolean memoryOptimization) {
        this.memoryOptimization = memoryOptimization;
    }
}

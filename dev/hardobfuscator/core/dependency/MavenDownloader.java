package dev.hardobfuscator.core.dependency;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

/**
 * Downloads artifacts from Maven Central with retry and SHA-256 verification.
 */
public final class MavenDownloader {

    private static final Logger log = LoggerFactory.getLogger(MavenDownloader.class);
    private static final int MAX_RETRIES = 3;
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 60_000;

    public void download(DependencySpec spec, Path target) throws IOException {
        IOException lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                doDownload(spec, target);
                verifyChecksum(target, spec.sha256());
                log.info("Downloaded {} (attempt {})", spec.fileName(), attempt);
                return;
            } catch (IOException e) {
                lastError = e;
                log.warn("Download attempt {}/{} failed for {}: {}", attempt, MAX_RETRIES, spec.fileName(), e.getMessage());
                Files.deleteIfExists(target);
                if (attempt < MAX_RETRIES) {
                    sleep(attempt * 1000L);
                }
            }
        }
        throw new IOException("Failed to download " + spec.fileName() + " after " + MAX_RETRIES + " attempts", lastError);
    }

    private void doDownload(DependencySpec spec, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(target.getParent(), "ag-download-", ".jar");

        HttpURLConnection connection = (HttpURLConnection) URI.create(spec.mavenCentralUrl()).toURL().openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "hardobfuscator/1.0");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + responseCode + " for " + spec.mavenCentralUrl());
        }

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void verifyChecksum(Path file, String expectedSha256) throws IOException {
        if (expectedSha256 == null || expectedSha256.isBlank()) {
            log.debug("No SHA-256 specified for {}, skipping verification", file.getFileName());
            return;
        }
        byte[] data = Files.readAllBytes(file);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String actual = HexFormat.of().formatHex(digest.digest(data));
            if (!actual.equalsIgnoreCase(expectedSha256)) {
                throw new IOException("SHA-256 mismatch for " + file.getFileName()
                        + ": expected " + expectedSha256 + ", got " + actual);
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm unavailable", e);
        }
    }

    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

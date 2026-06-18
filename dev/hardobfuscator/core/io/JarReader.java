package dev.hardobfuscator.core.io;

import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import dev.hardobfuscator.plugins.api.model.ResourceEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;

/**
 * Reads JAR/ZIP archives into the obfuscation context.
 */
public final class JarReader {

    private static final Logger log = LoggerFactory.getLogger(JarReader.class);

    public void read(Path jarPath, Context context) throws IOException {
        log.info("Reading input JAR: {}", jarPath);
        int classCount = 0;
        int resourceCount = 0;

        try (ZipFile zipFile = ZipFile.builder().setPath(jarPath).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                byte[] data = readEntry(zipFile, entry);

                if (name.endsWith(".class")) {
                    String internalName = name.substring(0, name.length() - 6);
                    context.classes().put(internalName, new ClassEntry(internalName, data));
                    classCount++;
                } else {
                    context.resources().put(name, new ResourceEntry(name, data));
                    resourceCount++;
                }
            }
        }

        log.info("Loaded {} classes and {} resources", classCount, resourceCount);
    }

    private byte[] readEntry(ZipFile zipFile, ZipArchiveEntry entry) throws IOException {
        try (InputStream in = zipFile.getInputStream(entry);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        }
    }
}

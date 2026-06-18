package dev.hardobfuscator.core.io;

import dev.hardobfuscator.plugins.api.Context;
import dev.hardobfuscator.plugins.api.model.ClassEntry;
import dev.hardobfuscator.plugins.api.model.ResourceEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the obfuscated context back to a JAR file.
 */
public final class JarWriter {

    private static final Logger log = LoggerFactory.getLogger(JarWriter.class);

    public void write(Context context) throws IOException {
        Path output = context.outputJar();
        Files.createDirectories(output.getParent() != null ? output.getParent() : Path.of("."));

        log.info("Writing output JAR: {}", output);
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(output.toFile())) {
            zos.setLevel(9);
            zos.setUseZip64(org.apache.commons.compress.archivers.zip.Zip64Mode.AsNeeded);

            for (ClassEntry entry : context.classes().values()) {
                String path = entry.internalName() + ".class";
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(path);
                zipEntry.setSize(entry.bytecode().length);
                zos.putArchiveEntry(zipEntry);
                zos.write(entry.bytecode());
                zos.closeArchiveEntry();
            }

            for (ResourceEntry resource : context.resources().values()) {
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(resource.path());
                zipEntry.setSize(resource.data().length);
                zos.putArchiveEntry(zipEntry);
                zos.write(resource.data());
                zos.closeArchiveEntry();
            }
        }

        Object scopedAttr = context.getAttribute("scopedClassCount");
        int inScope = scopedAttr instanceof Number n ? n.intValue() : context.classes().size();
        int preserved = Math.max(0, context.classes().size() - inScope);
        log.info("Output JAR written: {} ({} classes: {} obfuscated, {} preserved; {} resources)",
                output, context.classes().size(), inScope, preserved, context.resources().size());
    }
}

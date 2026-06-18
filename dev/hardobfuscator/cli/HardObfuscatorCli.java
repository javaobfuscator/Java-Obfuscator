package dev.hardobfuscator.cli;

import dev.hardobfuscator.config.ConfigLoader;
import dev.hardobfuscator.config.ConfigValidator;
import dev.hardobfuscator.config.ObfuscationConfig;
import dev.hardobfuscator.core.ObfuscatorEngine;
import dev.hardobfuscator.transformers.BuiltinTransformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command-line interface for HardObfuscator.
 *
 * Usage:
 *   hardobfuscator --config obfuscation.json
 *   hardobfuscator --input app.jar --output app-obf.jar
 */
public final class HardObfuscatorCli {

    private static final Logger log = LoggerFactory.getLogger(HardObfuscatorCli.class);

    public static void main(String[] args) {
        try {
            CliArguments cli = CliArguments.parse(args);
            if (cli.help()) {
                printHelp();
                return;
            }

            ObfuscationConfig config = loadConfig(cli);
            List<String> errors = ConfigValidator.validate(config);
            if (!errors.isEmpty()) {
                errors.forEach(e -> log.error("Config error: {}", e));
                System.exit(1);
            }

            if (!Files.exists(Path.of(config.getInput()))) {
                log.error("Input JAR not found: {}", config.getInput());
                System.exit(1);
            }

            ObfuscatorEngine engine = new ObfuscatorEngine();
            engine.initialize(BuiltinTransformers.all());
            engine.obfuscate(config);

            log.info("Obfuscation successful: {}", config.getOutput());
        } catch (Exception e) {
            log.error("Obfuscation failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static ObfuscationConfig loadConfig(CliArguments cli) throws Exception {
        if (cli.configPath() != null) {
            return ConfigLoader.load(java.nio.file.Path.of(cli.configPath()));
        }
        ObfuscationConfig config = ConfigLoader.defaultConfig();
        if (cli.input() != null) {
            config.setInput(cli.input());
        }
        if (cli.output() != null) {
            config.setOutput(cli.output());
        }
        return config;
    }

    private static void printHelp() {
        System.out.println("""
                HardObfuscator CLI v1.0.0

                Usage:
                  --config <file>     JSON configuration file
                  --input <jar>       Input JAR path
                  --output <jar>      Output JAR path
                  --help              Show this help

                Example:
                  hardobfuscator --config examples/obfuscation.json
                """);
    }
}

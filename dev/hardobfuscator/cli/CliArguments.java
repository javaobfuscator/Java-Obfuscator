package dev.hardobfuscator.cli;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses command-line arguments for the CLI entry point.
 */
public record CliArguments(
        String configPath,
        String input,
        String output,
        boolean help
) {
    public static CliArguments parse(String[] args) {
        Map<String, String> flags = new HashMap<>();
        boolean help = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help", "-h" -> help = true;
                case "--config", "-c" -> flags.put("config", nextArg(args, ++i));
                case "--input", "-i" -> flags.put("input", nextArg(args, ++i));
                case "--output", "-o" -> flags.put("output", nextArg(args, ++i));
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        return new CliArguments(
                flags.get("config"),
                flags.get("input"),
                flags.get("output"),
                help
        );
    }

    private static String nextArg(String[] args, int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for argument");
        }
        return args[index];
    }
}

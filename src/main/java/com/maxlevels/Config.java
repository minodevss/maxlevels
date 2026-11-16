package com.maxlevels;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

record Config(Map<Key, Integer> levels, OptionalInt anvilMaxCost) {

    static final Config NONE = new Config(Map.of(), OptionalInt.empty());

    private static final String FILE_NAME = "config.yml";
    private static final String ANVIL_MAX_COST = "anvil-max-cost";
    private static final int LOWEST_LEVEL = 1;
    private static final int HIGHEST_LEVEL = 255;
    private static final int LOWEST_COST = 1;
    private static final int HIGHEST_COST = 32766;

    static Config load(Path dataDirectory, Consumer<String> warn) {
        Path file = dataDirectory.resolve(FILE_NAME);
        try {
            if (Files.notExists(file)) {
                writeDefault(file);
            }
            try (Reader reader = Files.newBufferedReader(file)) {
                return parse(new Yaml().load(reader), warn);
            }
        } catch (IOException | YAMLException exception) {
            warn.accept("Ignoring " + FILE_NAME + ": " + exception.getMessage());
            return NONE;
        }
    }

    private static void writeDefault(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (InputStream defaults = Config.class.getResourceAsStream("/" + FILE_NAME)) {
            Files.copy(defaults, file);
        }
    }

    private static Config parse(Object document, Consumer<String> warn) {
        if (document == null) {
            return NONE;
        }
        if (!(document instanceof Map<?, ?> entries)) {
            warn.accept("Ignoring " + FILE_NAME + ": expected lines like 'sharpness: 10'");
            return NONE;
        }
        Map<Key, Integer> levels = new LinkedHashMap<>();
        OptionalInt anvilMaxCost = OptionalInt.empty();
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            String name = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            String line = "'" + name + ": " + value + "'";
            if (name.equals(ANVIL_MAX_COST)) {
                if (isWithin(value, LOWEST_COST, HIGHEST_COST)) {
                    anvilMaxCost = OptionalInt.of((Integer) value);
                } else {
                    warn.accept("Ignoring " + line + ": cost must be a whole number from " + LOWEST_COST + " to " + HIGHEST_COST);
                }
                continue;
            }
            Key key;
            try {
                key = Key.key(name);
            } catch (InvalidKeyException exception) {
                warn.accept("Ignoring " + line + ": not a valid enchantment key");
                continue;
            }
            if (!isWithin(value, LOWEST_LEVEL, HIGHEST_LEVEL)) {
                warn.accept("Ignoring " + line + ": level must be a whole number from " + LOWEST_LEVEL + " to " + HIGHEST_LEVEL);
                continue;
            }
            if (levels.putIfAbsent(key, (Integer) value) != null) {
                warn.accept("Ignoring " + line + ": " + key.asString() + " is already set");
            }
        }
        return new Config(Collections.unmodifiableMap(levels), anvilMaxCost);
    }

    private static boolean isWithin(Object value, int lowest, int highest) {
        return value instanceof Integer number && number >= lowest && number <= highest;
    }
}

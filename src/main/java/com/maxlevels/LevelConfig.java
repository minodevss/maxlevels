package com.maxlevels;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

record LevelConfig(Map<Key, Integer> levels) {

    static final LevelConfig NONE = new LevelConfig(Map.of());

    private static final String FILE_NAME = "config.yml";
    private static final int LOWEST = 1;
    private static final int HIGHEST = 255;

    static LevelConfig load(Path dataDirectory, Consumer<String> warn) {
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
        try (InputStream defaults = LevelConfig.class.getResourceAsStream("/" + FILE_NAME)) {
            Files.copy(defaults, file);
        }
    }

    private static LevelConfig parse(Object document, Consumer<String> warn) {
        if (document == null) {
            return NONE;
        }
        if (!(document instanceof Map<?, ?> entries)) {
            warn.accept("Ignoring " + FILE_NAME + ": expected lines like 'sharpness: 10'");
            return NONE;
        }
        Map<Key, Integer> levels = new LinkedHashMap<>();
        entries.forEach((name, value) -> {
            String entry = "'" + name + ": " + value + "'";
            Key key;
            try {
                key = Key.key(String.valueOf(name));
            } catch (InvalidKeyException exception) {
                warn.accept("Ignoring " + entry + ": not a valid enchantment key");
                return;
            }
            if (!(value instanceof Integer level) || level < LOWEST || level > HIGHEST) {
                warn.accept("Ignoring " + entry + ": level must be a whole number from " + LOWEST + " to " + HIGHEST);
                return;
            }
            if (levels.putIfAbsent(key, level) != null) {
                warn.accept("Ignoring " + entry + ": " + key.asString() + " is already set");
            }
        });
        return new LevelConfig(Collections.unmodifiableMap(levels));
    }
}

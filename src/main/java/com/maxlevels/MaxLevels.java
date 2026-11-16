package com.maxlevels;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

public final class MaxLevels extends JavaPlugin {

    private final Config config;
    private final Map<Key, Integer> originalLevels;

    MaxLevels(Config config, Map<Key, Integer> originalLevels) {
        this.config = config;
        this.originalLevels = originalLevels;
    }

    @Override
    public void onEnable() {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        List<String> applied = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        config.levels().keySet().forEach(key -> {
            Integer original = originalLevels.get(key);
            Enchantment enchantment = registry.get(key);
            if (original == null || enchantment == null) {
                unknown.add(key.asMinimalString());
            } else {
                applied.add(key.asMinimalString() + " " + original + " -> " + enchantment.getMaxLevel());
            }
        });
        if (!applied.isEmpty()) {
            getSLF4JLogger().info("Applied {}", String.join(", ", applied));
        }
        if (!unknown.isEmpty()) {
            getSLF4JLogger().warn("Unknown enchantments: {}", String.join(", ", unknown));
        }
        config.anvilMaxCost().ifPresent(maxCost -> {
            getServer().getPluginManager().registerEvents(new AnvilCostLimit(maxCost), this);
            getSLF4JLogger().info("Anvil max cost 39 -> {}", maxCost);
        });
    }
}

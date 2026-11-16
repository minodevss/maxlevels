package com.enchantcap;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantCap extends JavaPlugin {

    private final MaxLevels maxLevels;
    private final Map<Key, Integer> originalLevels;

    EnchantCap(MaxLevels maxLevels, Map<Key, Integer> originalLevels) {
        this.maxLevels = maxLevels;
        this.originalLevels = originalLevels;
    }

    @Override
    public void onEnable() {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        List<String> applied = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        maxLevels.levels().keySet().forEach(key -> {
            Integer original = originalLevels.get(key);
            Enchantment enchantment = registry.get(key);
            if (original == null || enchantment == null) {
                unknown.add(key.asMinimalString());
            } else {
                applied.add(key.asMinimalString() + " " + original + " -> " + enchantment.getMaxLevel());
            }
        });
        if (!applied.isEmpty()) {
            getSLF4JLogger().info("Max levels: {}", String.join(", ", applied));
        }
        if (!unknown.isEmpty()) {
            getSLF4JLogger().warn("Unknown enchantments: {}", String.join(", ", unknown));
        }
    }
}

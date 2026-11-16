package com.maxlevels;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.registry.event.RegistryEvents;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;

public final class MaxLevelsBootstrap implements PluginBootstrap {

    private final Map<Key, Integer> originalLevels = new HashMap<>();
    private Config config = Config.NONE;

    @Override
    public void bootstrap(BootstrapContext context) {
        config = Config.load(context.getDataDirectory(), context.getLogger()::warn);
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            Key key = event.key().key();
            Integer level = config.levels().get(key);
            if (level != null) {
                originalLevels.put(key, event.builder().maxLevel());
                event.builder().maxLevel(level);
            }
        }));
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new MaxLevels(config, originalLevels);
    }
}

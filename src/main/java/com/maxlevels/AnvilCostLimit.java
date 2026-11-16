package com.maxlevels;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.view.AnvilView;

final class AnvilCostLimit implements Listener {

    private final int maximumRepairCost;

    AnvilCostLimit(int maxCost) {
        this.maximumRepairCost = maxCost + 1;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getView() instanceof AnvilView view) {
            view.setMaximumRepairCost(maximumRepairCost);
        }
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        event.getView().setMaximumRepairCost(maximumRepairCost);
    }
}

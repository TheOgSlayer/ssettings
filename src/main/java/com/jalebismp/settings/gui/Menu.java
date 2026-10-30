package com.jalebismp.settings.gui;

import com.jalebismp.settings.JalebiSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Menu implements InventoryHolder {
    protected final JalebiSettings plugin;
    protected Inventory inventory;

    protected Menu(JalebiSettings plugin) { this.plugin = plugin; }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public abstract void handleClick(Player player, InventoryClickEvent event);

    public void open(Player player) { player.openInventory(inventory); }

    protected ItemStack pane() {
        return Items.make(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
    }

    protected void fillEmpty() {
        for (int i = 0; i < inventory.getSize(); i++)
            if (inventory.getItem(i) == null) inventory.setItem(i, pane());
    }

    protected void click(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
    }
}

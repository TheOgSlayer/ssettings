package com.jalebismp.settings.gui;

import com.jalebismp.settings.JalebiSettings;
import com.jalebismp.settings.data.Category;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import net.kyori.adventure.text.Component;

import java.util.List;

public class MainMenu extends Menu {

    public MainMenu(JalebiSettings plugin) {
        super(plugin);
        inventory = Bukkit.createInventory(this, 36, Component.text("Jalebi SMP Settings"));
        inventory.setItem(4, Items.make(Material.PAPER,
                Items.text("Settings", NamedTextColor.GOLD),
                List.of(Items.text("Choose a category to change", NamedTextColor.GRAY),
                        Items.text("your Jalebi SMP settings", NamedTextColor.GRAY))));
        for (Category c : Category.values()) {
            inventory.setItem(c.slot(), Items.make(c.icon(),
                    Items.text(c.display(), NamedTextColor.WHITE),
                    List.of(Items.text("Click to open", NamedTextColor.DARK_GRAY))));
        }
        fillEmpty();
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent e) {
        Category c = Category.bySlot(e.getSlot());
        if (c == null) return;
        click(player);
        new CategoryMenu(plugin, player, c).open(player);
    }
}

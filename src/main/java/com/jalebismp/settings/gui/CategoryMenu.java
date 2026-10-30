package com.jalebismp.settings.gui;

import com.jalebismp.settings.JalebiSettings;
import com.jalebismp.settings.data.Category;
import com.jalebismp.settings.data.Setting;
import com.jalebismp.settings.data.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Row 1: one icon per setting. Row 2: a coloured dye under each icon showing its state.
 * Row 3: the Back button, directly under the last option.
 */
public class CategoryMenu extends Menu {
    private final Category category;
    private final List<Setting> settings;
    private final Map<Integer, Setting> slotMap = new HashMap<>();
    private final Map<Setting, Integer> iconSlots = new HashMap<>();
    private final int backSlot;
    private final Player viewer;

    public CategoryMenu(JalebiSettings plugin, Player viewer, Category category) {
        super(plugin);
        this.viewer = viewer;
        this.category = category;
        this.settings = Setting.in(category);
        inventory = Bukkit.createInventory(this, 45, Component.text(category.display() + " Settings"));

        int n = settings.size();
        int start = 13 - n / 2;
        for (int i = 0; i < n; i++) {
            Setting s = settings.get(i);
            int slot = start + i;
            iconSlots.put(s, slot);
            slotMap.put(slot, s);
            slotMap.put(slot + 9, s);
        }
        backSlot = start + n - 1 + 18;
        inventory.setItem(backSlot, Items.make(Material.ARROW,
                Items.text("Back", NamedTextColor.WHITE),
                List.of(Items.text("Return to the main settings menu", NamedTextColor.GRAY))));
        render();
        fillEmpty();
    }

    private void render() {
        for (Setting s : settings) {
            Value cur = plugin.data().get(viewer.getUniqueId(), s);
            int slot = iconSlots.get(s);
            inventory.setItem(slot, Items.make(s.icon(), Items.text(s.display(), NamedTextColor.WHITE), lore(s, cur)));
            inventory.setItem(slot + 9, Items.make(cur.dye(), Items.text(cur.display(), cur.color()),
                    List.of(Items.text(s.options().size() > 2 ? "Click to change" : "Click to toggle", NamedTextColor.DARK_GRAY))));
        }
    }

    private List<Component> lore(Setting s, Value cur) {
        List<Component> l = new ArrayList<>();
        l.add(Items.text(s.description(), NamedTextColor.GRAY));
        l.add(Component.empty());
        for (Value o : s.options()) {
            boolean sel = o == cur;
            l.add(Component.text((sel ? "» " : "   ") + o.display(), sel ? o.color() : NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, sel));
        }
        l.add(Component.empty());
        l.add(Items.text(s.options().size() > 2 ? "Left-click: next | Right-click: previous" : "Click to toggle",
                NamedTextColor.DARK_GRAY));
        return l;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent e) {
        int slot = e.getSlot();
        if (slot == backSlot) {
            click(player);
            new MainMenu(plugin).open(player);
            return;
        }
        Setting s = slotMap.get(slot);
        if (s == null) return;
        Value cur = plugin.data().get(player.getUniqueId(), s);
        Value next = s.cycle(cur, !e.isRightClick());
        plugin.data().set(player.getUniqueId(), s, next);
        plugin.onSettingChanged(player, s, next);
        click(player);
        render();
    }
}

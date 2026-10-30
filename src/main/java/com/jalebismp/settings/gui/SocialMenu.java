package com.jalebismp.settings.gui;

import com.jalebismp.settings.JalebiSettings;
import com.jalebismp.settings.data.DataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/** 3-tab GUI: Friends / Followers / Following. */
public class SocialMenu extends Menu {

    public enum Tab {
        FRIENDS("Friends", Material.GOLDEN_APPLE, 2),
        FOLLOWERS("Followers", Material.PAPER, 4),
        FOLLOWING("Following", Material.COMPASS, 6);

        final String display;
        final Material icon;
        final int slot;

        Tab(String display, Material icon, int slot) {
            this.display = display;
            this.icon = icon;
            this.slot = slot;
        }
    }

    private static final int PER_PAGE = 36; // slots 9..44

    private final Player viewer;
    private Tab tab;
    private int page;
    private List<UUID> shown = new ArrayList<>();

    public SocialMenu(JalebiSettings plugin, Player viewer, Tab tab) {
        super(plugin);
        this.viewer = viewer;
        this.tab = tab;
        inventory = Bukkit.createInventory(this, 54, Component.text("Friends & Followers"));
        render();
    }

    private Set<UUID> listFor(Tab t) {
        DataManager d = plugin.data();
        UUID me = viewer.getUniqueId();
        return switch (t) {
            case FRIENDS -> d.friends(me);
            case FOLLOWERS -> d.followers(me);
            case FOLLOWING -> d.following(me);
        };
    }

    private void render() {
        inventory.clear();
        DataManager d = plugin.data();
        UUID me = viewer.getUniqueId();

        for (int i = 0; i < 9; i++) inventory.setItem(i, pane());
        for (int i = 45; i < 54; i++) inventory.setItem(i, pane());

        for (Tab t : Tab.values()) {
            boolean sel = t == tab;
            inventory.setItem(t.slot, Items.make(t.icon,
                    Items.text(t.display, sel ? NamedTextColor.GREEN : NamedTextColor.WHITE),
                    List.of(Items.text(listFor(t).size() + " player(s)", NamedTextColor.GRAY),
                            Items.text(sel ? "Currently viewing" : "Click to view", NamedTextColor.DARK_GRAY)),
                    sel));
        }

        List<UUID> all = new ArrayList<>(listFor(tab));
        all.sort(Comparator.comparing((UUID u) -> Bukkit.getPlayer(u) == null)
                .thenComparing(u -> d.nameOf(u).toLowerCase()));

        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.min(Math.max(page, 0), pages - 1);
        int from = page * PER_PAGE;
        int to = Math.min(all.size(), from + PER_PAGE);
        shown = new ArrayList<>(all.subList(from, to));

        if (all.isEmpty()) {
            String title = switch (tab) {
                case FRIENDS -> "No friends yet";
                case FOLLOWERS -> "Nobody follows you yet";
                case FOLLOWING -> "You aren't following anyone";
            };
            inventory.setItem(22, Items.make(Material.BARRIER, Items.text(title, NamedTextColor.RED),
                    List.of(Items.text("Use /follow <player> to follow someone.", NamedTextColor.GRAY),
                            Items.text("If they follow you back, you become friends.", NamedTextColor.GRAY))));
        }
        for (int i = 0; i < shown.size(); i++) inventory.setItem(9 + i, head(shown.get(i)));

        inventory.setItem(47, Items.make(Material.COMPARATOR, Items.text("Settings", NamedTextColor.WHITE),
                List.of(Items.text("Open the settings menu", NamedTextColor.GRAY))));
        inventory.setItem(49, Items.make(Material.PAPER,
                Items.text("Page " + (page + 1) + "/" + pages, NamedTextColor.GRAY), List.of()));
        if (page > 0) inventory.setItem(45, Items.make(Material.ARROW, Items.text("Previous page", NamedTextColor.WHITE), List.of()));
        if (page < pages - 1) inventory.setItem(53, Items.make(Material.ARROW, Items.text("Next page", NamedTextColor.WHITE), List.of()));
    }

    private ItemStack head(UUID id) {
        DataManager d = plugin.data();
        UUID me = viewer.getUniqueId();
        boolean online = Bukkit.getPlayer(id) != null;
        boolean iFollow = d.isFollowing(me, id);
        boolean followsMe = d.isFollowing(id, me);

        List<Component> lore = new ArrayList<>();
        lore.add(Items.text(online ? "Online" : "Offline", online ? NamedTextColor.GREEN : NamedTextColor.RED));
        if (iFollow && followsMe) lore.add(Items.text("Friends", NamedTextColor.YELLOW));
        else if (iFollow) lore.add(Items.text("You follow them", NamedTextColor.GRAY));
        else if (followsMe) lore.add(Items.text("They follow you", NamedTextColor.GRAY));
        lore.add(Component.empty());
        if (tab == Tab.FOLLOWERS) {
            lore.add(Items.text(iFollow ? "You follow each other" : "Left-click to follow back", NamedTextColor.DARK_GRAY));
        } else {
            lore.add(Items.text("Right-click to unfollow", NamedTextColor.DARK_GRAY));
        }

        ItemStack item = Items.make(Material.PLAYER_HEAD,
                Items.text(d.nameOf(id), online ? NamedTextColor.GREEN : NamedTextColor.GRAY), lore);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent e) {
        int slot = e.getSlot();

        for (Tab t : Tab.values()) {
            if (slot == t.slot) {
                tab = t;
                page = 0;
                click(player);
                render();
                return;
            }
        }
        if (slot == 45 && inventory.getItem(45) != null && inventory.getItem(45).getType() == Material.ARROW) {
            page--; click(player); render(); return;
        }
        if (slot == 53 && inventory.getItem(53) != null && inventory.getItem(53).getType() == Material.ARROW) {
            page++; click(player); render(); return;
        }
        if (slot == 47) {
            click(player);
            new MainMenu(plugin).open(player);
            return;
        }
        if (slot >= 9 && slot < 9 + shown.size()) {
            UUID target = shown.get(slot - 9);
            DataManager d = plugin.data();
            if (tab == Tab.FOLLOWERS) {
                if (e.isLeftClick() && !d.isFollowing(player.getUniqueId(), target)) {
                    plugin.social().follow(player, target);
                }
            } else if (e.isRightClick()) {
                plugin.social().unfollow(player, target);
            }
            click(player);
            render();
        }
    }
}

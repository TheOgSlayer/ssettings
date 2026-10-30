package com.jalebismp.settings;

import com.jalebismp.settings.data.DataManager;
import com.jalebismp.settings.data.Setting;
import com.jalebismp.settings.data.Value;
import com.jalebismp.settings.feature.*;
import com.jalebismp.settings.gui.MainMenu;
import com.jalebismp.settings.gui.MenuListener;
import com.jalebismp.settings.social.SocialCommands;
import com.jalebismp.settings.social.SocialService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class JalebiSettings extends JavaPlugin {
    private static JalebiSettings instance;
    private DataManager data;
    private SocialService social;
    private GeneralTask general;
    private CombatTracker combat;

    public static JalebiSettings get() { return instance; }
    public DataManager data() { return data; }
    public SocialService social() { return social; }
    public CombatTracker combat() { return combat; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        data = new DataManager(this);
        data.load();
        social = new SocialService(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MenuListener(), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CommandGuardListener(this), this);
        pm.registerEvents(new PvPListener(this), this);

        combat = new CombatTracker(this);
        pm.registerEvents(combat, this);
        getServer().getScheduler().runTaskTimer(this, combat, 10L, 10L);

        general = new GeneralTask(this);
        pm.registerEvents(general, this);
        long interval = Math.max(20L, getConfig().getLong("general.task-interval-ticks", 100L));
        getServer().getScheduler().runTaskTimer(this, general, 100L, interval);

        getCommand("settings").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) new MainMenu(this).open(p);
            else sender.sendMessage("Players only.");
            return true;
        });
        SocialCommands sc = new SocialCommands(this);
        for (String name : new String[]{"follow", "unfollow", "friends", "followers", "following"}) {
            getCommand(name).setExecutor(sc);
            getCommand(name).setTabCompleter(sc);
        }

        if (pm.isPluginEnabled("ProtocolLib")) {
            try {
                new PacketHooks(this);
            } catch (Throwable t) {
                getLogger().warning("ProtocolLib hooks failed: " + t.getMessage());
            }
        } else {
            getLogger().info("ProtocolLib not found: hotbar / totem / explosion settings will not take effect.");
        }

        for (Player p : Bukkit.getOnlinePlayers()) data.remember(p);
        getServer().getScheduler().runTaskTimerAsynchronously(this, data::save, 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        if (data != null) data.save();
    }

    /** Called by the GUI after a player changes a setting (for settings that need an immediate effect). */
    public void onSettingChanged(Player p, Setting s, Value v) {
        if (s == Setting.NIGHT_VISION) general.applyNightVision(p, true);
    }
}

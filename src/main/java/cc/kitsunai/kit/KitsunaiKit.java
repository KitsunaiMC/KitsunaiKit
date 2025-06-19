package cc.kitsunai.kit;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xingchen.xingchenPlayerInfo.api.PlayerInfoAPI;

public final class KitsunaiKit extends JavaPlugin implements Listener {

    private static PlayerInfoAPI playerInfoAPI;
    private static KitsunaiKit instance;
    private static KitManager kitManager;

    public static KitsunaiKit getInstance() {
        return instance;
    }

    public static PlayerInfoAPI getPlayerInfoAPI() {
        return playerInfoAPI;
    }

    public KitsunaiKit() {
        super();
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        playerInfoAPI = Bukkit.getServicesManager().load(PlayerInfoAPI.class);
        kitManager = new KitManager();
        Bukkit.getPluginManager().registerEvents(this, this);
        kitManager.loadAllKits();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (playerInfoAPI.isNewComer(event.getPlayer())) {
            kitManager.newComerKit(event.getPlayer());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

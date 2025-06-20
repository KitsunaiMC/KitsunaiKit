package cc.kitsunai.kit;

import cc.kitsunai.kit.api.KitRegistrar;
import cc.kitsunai.kit.kits.NewComers;
import cc.kitsunai.kit.kits.NewComersMoney;
import cc.kitsunai.kit.kits.TestCdKey;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import xingchen.xingchenPlayerInfo.api.PlayerInfoAPI;

public final class KitsunaiKit extends JavaPlugin implements Listener {

    private static KitsunaiKit instance;
    public static KitManager kitManager;

    public static KitsunaiKit getInstance() {
        return instance;
    }

    public KitsunaiKit() {
        super();
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        kitManager = new KitManager();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getServicesManager().register(
                KitRegistrar.class,
                kitManager,
                this,
                ServicePriority.Normal
        );
        kitManager.registerKit(new NewComers());
        kitManager.registerKit(new NewComersMoney());
        kitManager.registerKit(new TestCdKey());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(KitCommand.buildCommand));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        kitManager.newComerKit(event.getPlayer());
    }

    @Override
    public void onDisable() {
        kitManager.close();
    }
}

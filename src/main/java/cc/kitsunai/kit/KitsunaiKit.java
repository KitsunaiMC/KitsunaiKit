package cc.kitsunai.kit;

import cc.kitsunai.kit.api.KitRegistrar;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitsunaiKit extends JavaPlugin implements Listener {

    private static KitsunaiKit instance;
    public static KitManager kitManager;
    private static KitReader kitReader;

    @SuppressWarnings("UnstableApiUsage")
    public static SuggestionProvider<CommandSourceStack> getKitsSuggestions() {
        return kitManager.getSuggestions();
    }

    public static KitsunaiKit getInstance() {
        return instance;
    }

    public KitsunaiKit() {
        super();
        instance = this;
    }

    @SuppressWarnings("UnstableApiUsage")
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
        kitReader = new KitReader(getDataFolder(), kitManager, getLogger());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(KitCommand.buildCommand));
        kitReader.readKits();
        instance.getLogger().info("插件启动成功！");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        kitManager.newComerKit(event.getPlayer());
    }

    @Override
    public void onDisable() {
        kitManager.close();
    }

    public static void onReload() {
        instance.getLogger().info("重载插件中...");
        kitManager.onReload();
        kitReader.readKits();
        instance.getLogger().info("重载插件成功...");
    }
}

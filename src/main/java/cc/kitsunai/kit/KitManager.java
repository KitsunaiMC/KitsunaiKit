package cc.kitsunai.kit;

import cc.kitsunai.kit.api.Kit;
import cc.kitsunai.kit.api.KitRegistrar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class KitManager implements KitRegistrar {
    private final Map<String, Kit> kits;
    private final KitDataBase kitDataBase;

    public KitManager() {
        this.kits = new HashMap<>();
        this.kitDataBase = new KitDataBase();
    }

    public void registerKit(@NotNull Kit kit) {
        kits.put(kit.getCdKey(), kit);
    }

    public void newComerKit(Player player) {
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            if (entry.getValue().isFirstJoin()) {
                sendKit(player, entry.getValue());
            }
        }
    }

    public void getKit(Player player, String cdKey) {
        if (!kits.containsKey(cdKey)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>CdKey不存在！</red>"));
            return;
        }
        Kit kit = kits.get(cdKey);
        if (kit.isFirstJoin()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>新手礼包不能手动领取</red>"));
            return;
        }
        if (kitDataBase.getRedemptionCount(kit.getDataBaseName(), player.getUniqueId()) < kit.getMaximumCollect()) {
            sendKit(player, kit);
        }
    }

    private void sendKit(Player player, Kit kit) {
        Set<String> dependencies = kit.getDependencies();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (dependencies.contains(plugin.getName())) {
                dependencies.remove(player.getName());
            }
        }
        if (!dependencies.isEmpty()) {
            KitsunaiKit.getInstance().getLogger().warning("礼包 " + kit.getSimpleName() + "  的依赖插件：" + dependencies + " 未加载");
            return;
        }
        Collection<ItemStack> itemStacks = new ArrayList<>();
        for (ItemStack itemStack : kit.getItems()) {
            itemStacks.add(itemStack.clone());
        }
        player.give(itemStacks, true);
        Component component = MiniMessage.miniMessage().deserialize("<green>成功领取礼包：</green>");
        player.sendMessage(component.append(kit.getDisplayName()));
        kitDataBase.recordRedemption(kit.getDataBaseName(), player.getUniqueId());
        kit.afterCollect(player);
    }
}

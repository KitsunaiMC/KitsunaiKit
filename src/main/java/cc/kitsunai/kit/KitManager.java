package cc.kitsunai.kit;

import cc.kitsunai.kit.api.Kit;
import cc.kitsunai.kit.api.KitRegistrar;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class KitManager implements KitRegistrar {
    private final Map<String, Kit> kits;
    private final KitDataBase kitDataBase;

    public KitManager() {
        this.kits = new HashMap<>();
        this.kitDataBase = new KitDataBase();
    }

    public void registerKit(@NotNull Kit kit) {
        kits.put(kit.getId(), kit);
        kitDataBase.createGiftTable(kit.getId());
    }

    public void newComerKit(Player player) {
        for (Kit kit : kits.values()) {
            int count = kitDataBase.getRedemptionCount(kit.getId(), player.getUniqueId());
            if (kit.isFirstJoin() && count == 0) {
                sendKit(player, kit);
            }
        }
    }

    public @Nullable Set<String> generate(int count, String id) {
        if (!kits.containsKey(id)) return null;
        return kits.get(id).generateCdkey(count);
    }

    public void getKit(Player player, String cdKey) {
        boolean get = false;
        for (Kit kit : kits.values()) {
            if (kit.match(cdKey)) {
                if (kit.isFirstJoin() && !player.isOp()) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>新手礼包不能手动领取</red>"));
                } else if (kitDataBase.getRedemptionCount(kit.getId(), player.getUniqueId()) < kit.getMaximumCollect()) {
                    sendKit(player, kit);
                    get = true;
                } else {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>你的领取次数已经达到上限！</red>"));
                    get = true;
                }
            }
        }
        if (!get) player.sendMessage(MiniMessage.miniMessage().deserialize("<red>CdKey不存在！</red>"));
    }

    private void sendKit(Player player, Kit kit) {
        Set<String> dependencies = new HashSet<>(kit.getDependencies());
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            dependencies.remove(plugin.getName());
        }
        if (!dependencies.isEmpty()) {
            KitsunaiKit.getInstance().getLogger().warning("礼包 " + kit.getId() + " 的依赖插件：" + dependencies + " 未加载");
            return;
        }
        Collection<ItemStack> itemStacks = new ArrayList<>();
        for (ItemStack itemStack : kit.getItems()) {
            itemStacks.add(itemStack.clone());
        }
        player.give(itemStacks, true);
        Component component = MiniMessage.miniMessage().deserialize("<green>成功领取礼包：</green>");
        player.sendMessage(component.append(kit.getDisplayName()));
        kitDataBase.recordRedemption(kit.getId(), player.getUniqueId());
        kit.afterCollect(player);
    }

    public void close() {
        kitDataBase.close();
    }

    public void onReload() {
        kits.clear();
    }

    @SuppressWarnings("UnstableApiUsage")
    public SuggestionProvider<CommandSourceStack> getSuggestions() {
        return (ctx, builder) -> {
            String currentInput = builder.getRemaining().toLowerCase();
            kits.values().stream()
                    .filter(kit -> kit.getId().startsWith(currentInput))
                    .forEach(kit -> builder.suggest(kit.getId()));
            return builder.buildFuture();
        };
    }

    public Set<String> getAllCdkeys(String kitId) {
        if (kits.containsKey(kitId)) return kits.get(kitId).getAllCdkeys();
        else return null;
    }
}

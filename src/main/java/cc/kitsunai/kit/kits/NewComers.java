package cc.kitsunai.kit.kits;

import cc.kitsunai.kit.api.Kit;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class NewComers implements Kit {
    @Override
    public @NotNull String getDataBaseName() {
        return "newcomers";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize("<gold>新手礼包");
    }

    @Override
    public @NotNull String getSimpleName() {
        return "新手礼包";
    }

    @Override
    public @NotNull String getCdKey() {
        return "asdfkdhgvf-24u3y467";
    }

    @Override
    public @NotNull ItemStack[] getItems() {
        ItemStack arrow = ItemStack.of(Material.ARROW);
        arrow.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<b><green>圈地工具</green></b>"));
        arrow.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(MiniMessage.miniMessage().deserialize("<yellow>可以用左右键分别点击对角线来圈地噢~")).build());
        return new ItemStack[]{
                ItemStack.of(Material.STONE_SWORD),
                ItemStack.of(Material.STONE_AXE),
                ItemStack.of(Material.STONE_PICKAXE),
                ItemStack.of(Material.STONE_HOE),
                ItemStack.of(Material.BREAD, 32),
                ItemStack.of(Material.COOKED_COD, 6),
                ItemStack.of(Material.IRON_INGOT, 6),
                arrow
        };
    }

    @Override
    public boolean isFirstJoin() {
        return true;
    }

    @Override
    public int getMaximumCollect() {
        return 1;
    }

    @Override
    public void afterCollect(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "zmenu giveopenitem zmenu:mainmenu " + player.getName());
    }

    @Override
    public @NotNull Set<String> getDependencies() {
        return Set.of("zMenu");
    }
}

package cc.kitsunai.kit.kits;

import cc.kitsunai.kit.api.Kit;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class TestCdKey implements Kit {
    @Override
    public @NotNull String getDataBaseName() {
        return "test_cdkey";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize("<gold>cdkey测试礼包");
    }

    @Override
    public @NotNull String getSimpleName() {
        return "cdkey测试礼包";
    }

    @Override
    public @NotNull String getCdKey() {
        return "tests-minas-dkfjs-djfui";
    }

    @Override
    public @NotNull ItemStack[] getItems() {
        ItemStack itemStack = ItemStack.of(Material.PAPER, 1);
        itemStack.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<red>测试"));
        return new ItemStack[] {
                itemStack
        };
    }

    @Override
    public boolean isFirstJoin() {
        return false;
    }

    @Override
    public int getMaximumCollect() {
        return 100;
    }

    @Override
    public void afterCollect(Player player) {

    }

    @Override
    public @NotNull Set<String> getDependencies() {
        return Set.of();
    }
}

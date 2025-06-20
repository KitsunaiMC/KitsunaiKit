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

public class NewComersMoney implements Kit {
    @Override
    public @NotNull String getDataBaseName() {
        return "newcomers_money";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize("<gold>新手钱包");
    }

    @Override
    public @NotNull String getSimpleName() {
        return "新手钱包";
    }

    @Override
    public @NotNull String getCdKey() {
        return "qweqweqweqweqwe-1432-2342";
    }

    @Override
    public @NotNull ItemStack[] getItems() {
        return new ItemStack[0];
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "balance give " + player.getName() + " 100");
    }

    @Override
    public @NotNull Set<String> getDependencies() {
        return Set.of("Xconomy");
    }
}

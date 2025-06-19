package cc.kitsunai.kit.api;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Kit {
    @NotNull String getDataBaseName();

    @NotNull Component getName();

    @NotNull String getCdKey();

    @NotNull ItemStack[] getItems();

    boolean isFirstJoin();

    int getMaximumCollect();
}

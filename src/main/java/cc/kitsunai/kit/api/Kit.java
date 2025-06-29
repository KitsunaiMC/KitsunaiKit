package cc.kitsunai.kit.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Kit {
    @NotNull String getId();

    @NotNull Component getDisplayName();

    boolean match(@NotNull String cdkey);

    @NotNull ItemStack[] getItems();

    boolean isFirstJoin();

    int getMaximumCollect();

    void afterCollect(@NotNull Player player);

    @NotNull Set<String> getDependencies();

    @NotNull Set<String> generateCdkey(int count);

    @NotNull Set<String> getAllCdkeys();
}

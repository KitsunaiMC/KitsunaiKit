package cc.kitsunai.kit.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Kit {
    @NotNull String getDataBaseName();

    @NotNull default Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(getSimpleName());
    }

    @NotNull String getSimpleName();

    @NotNull String getCdKey();

    @NotNull ItemStack[] getItems();

    boolean isFirstJoin();

    int getMaximumCollect();

    void afterCollect(Player player);

    @NotNull Set<String> getDependencies();
}

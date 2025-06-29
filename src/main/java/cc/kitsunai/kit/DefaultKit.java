package cc.kitsunai.kit;

import cc.kitsunai.kit.api.Kit;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class DefaultKit implements Kit {

    private final Component displayName;
    private final String databaseName;
    private final boolean isFirstJoin;
    private final Consumer<Player> executor;
    private final Set<String> dependencies;
    private final List<ItemStack> itemStacks;
    private final int maxCollect;
    private final Set<String> cdkeys;

    public DefaultKit(Component displayName, String databaseName, boolean isFirstJoin, Consumer<Player> executor, Set<String> dependencies, List<ItemStack> itemStacks) {
        this.displayName = displayName;
        this.databaseName = databaseName;
        this.isFirstJoin = isFirstJoin;
        this.maxCollect = 1;
        this.executor = executor;
        this.dependencies = new HashSet<>(dependencies);
        this.itemStacks = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            this.itemStacks.add(itemStack.clone());
        }
        this.cdkeys = new HashSet<>();
    }

    public DefaultKit(Component displayName, String databaseName, int maxCollect, Consumer<Player> executor, Set<String> dependencies, List<ItemStack> itemStacks) {
        this.displayName = displayName;
        this.databaseName = databaseName;
        this.isFirstJoin = false;
        this.maxCollect = maxCollect;
        this.executor = executor;
        this.dependencies = new HashSet<>(dependencies);
        this.itemStacks = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            this.itemStacks.add(itemStack.clone());
        }
        this.cdkeys = new HashSet<>();
    }

    @Override
    public @NotNull String getId() {
        return databaseName;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    @Override
    public boolean match(@NotNull String cdkey) {
        return cdkeys.contains(cdkey);
    }

    @Override
    public @NotNull ItemStack[] getItems() {
        ItemStack[] itemStackArray = new ItemStack[itemStacks.size()];
        for (int i = 0; i < itemStacks.size(); i++) {
            itemStackArray[i] = itemStacks.get(i).clone();
        }
        return itemStackArray;
    }

    @Override
    public boolean isFirstJoin() {
        return isFirstJoin;
    }

    @Override
    public int getMaximumCollect() {
        return maxCollect;
    }

    @Override
    public void afterCollect(@NotNull Player player) {
        executor.accept(player);
    }

    @Override
    public @NotNull Set<String> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    @Override
    public @NotNull Set<String> generateCdkey(int count) {
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String cdkey = generateSingleCdkey(5, 5);
            while (cdkeys.contains(cdkey)) {
                cdkey = generateSingleCdkey(5, 5);
            }
            generated.add(cdkey);
            cdkeys.add(cdkey);
        }
        return Collections.unmodifiableSet(generated);
    }

    @Override
    public @NotNull Set<String> getAllCdkeys() {
        return Collections.unmodifiableSet(cdkeys);
    }

    private String generateSingleCdkey(int group, int lengthPerGroup) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < group; i++) {
            for (int j = 0; j < lengthPerGroup; j++) {
                builder.append(generateSingleCharacter());
            }
            if (i != group - 1) builder.append('-');
        }
        return builder.toString();
    }

    private char generateSingleCharacter() {
        final int allCharacters = 62;
        final int random = ThreadLocalRandom.current().nextInt(0, allCharacters);
        if (random <= 9) {
            return (char) (random + (int) '0');
        }
        else if (random <= 35) {
            return (char) (random - 10 + (int) 'a');
        } else {
            return (char) (random - 36 + (int) 'A');
        }
    }
}

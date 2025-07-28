package cc.kitsunai.kit;

import cc.kitsunai.kit.api.KitRegistrar;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class KitReader {
    private final File folder;
    private final KitRegistrar kitRegistrar;
    private final Logger logger;

    public KitReader(@NotNull File pluginFolder, @NotNull KitRegistrar kitRegistrar, @NotNull Logger logger) {
        this.folder = new File(pluginFolder, "kits");
        if (!this.folder.exists()) {
            this.folder.mkdirs();
            KitsunaiKit.getInstance().saveResource("kits/example_join_kit.yml", false);
            KitsunaiKit.getInstance().saveResource("kits/example_normal_kit.yml", false);
        }
        this.kitRegistrar = kitRegistrar;
        this.logger = logger;
    }

    public void readKits() {
        File[] files = folder.listFiles(
                file -> file.isFile() && (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))
        );
        if (files == null) {
            KitsunaiKit.getInstance().saveResource("kits/example_join_kit.yml", false);
            KitsunaiKit.getInstance().saveResource("kits/example_normal_kit.yml", false);
            return;
        }
        for (File ymlFile : files) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(ymlFile);
            String _name = configuration.getString("name");
            String databaseName = configuration.getString("id");
            String _isFirstJoin = configuration.getString("give_when_first_join");
            List<String> _dependencies = configuration.getStringList("dependency");
            List<String> _itemStacks = configuration.getStringList("item");
            String _maxCollect = configuration.getString("max_can_collect");
            if (_name == null || databaseName == null || _itemStacks.isEmpty()) {
                logger.severe("error on reading yml file " + ymlFile.getName() + ", Please fill all the necessary params");
                continue;
            } else if (_isFirstJoin == null && _maxCollect == null) {
                logger.severe("error on reading yml file " + ymlFile.getName() + ", Please fill all the necessary params");
                continue;
            } else if (_isFirstJoin != null && _maxCollect == null && !_isFirstJoin.equalsIgnoreCase("true") && !_isFirstJoin.equalsIgnoreCase("false")) {
                logger.severe("error on reading yml file " + ymlFile.getName() + ", Please correctly fill give_when_first_join!");
                continue;
            } else if (_isFirstJoin == null) {
                try {
                    Integer.parseInt(_maxCollect);
                } catch (NumberFormatException e) {
                    logger.severe("error on reading yml file " + ymlFile.getName() + ", Please correctly fill max_can_collect! It must be a correct number!");
                    continue;
                }
            } else if (_maxCollect != null) {
                logger.severe("error on reading yml file " + ymlFile.getName() + ", You can't fill max_can_collect and give_when_first_join at the same time!");
                continue;
            }
            final Component displayName = MiniMessage.miniMessage().deserialize(_name);
            Boolean isFirstJoin = null;
            Integer maxCollect = null;
            if (_isFirstJoin != null) isFirstJoin = Boolean.parseBoolean(_isFirstJoin);
            else maxCollect = Integer.parseInt(_maxCollect);
            final List<String> _actions = configuration.getStringList("action");
            final Multimap<String, String> actions = ArrayListMultimap.create();
            for (String action : _actions) {
                String[] args = action.split(":");
                if (args.length != 2) {
                    logger.severe("error on reading yml file " + ymlFile.getName() + ", You should provide correct actions!");
                    continue;
                }
                while (args[1].startsWith(" ")) {
                    args[1] = args[1].substring(1);
                }
                actions.put(args[0], args[1]);
            }
            final Consumer<Player> consumer = player -> {
                actions.forEach((type, command) -> {
                    if (command.toLowerCase().contains("%player%")) command = command.toLowerCase().replace("%player%", player.getName());
                    switch (type) {
                        case "player_command":
                            Bukkit.dispatchCommand(player, command);
                            break;
                        case "console_command":
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            break;
                        case null, default:
                            logger.severe("error on reading yml file " + ymlFile.getName() + ", You should provide correct actions!");
                    }
                });
            };
            final Set<String> dependencies = new HashSet<>(_dependencies);
            final List<ItemStack> itemStacks = new ArrayList<>();
            for (String binaryItem : _itemStacks) {
                byte[] bytes = Base64.getDecoder().decode(binaryItem);
                itemStacks.add(ItemStack.deserializeBytes(bytes));
            }
            if (isFirstJoin == null) {
                kitRegistrar.registerKit(
                        new DefaultKit(
                                displayName,
                                databaseName,
                                maxCollect,
                                consumer,
                                dependencies,
                                itemStacks
                        )
                );
            } else {
                kitRegistrar.registerKit(
                        new DefaultKit(
                                displayName,
                                databaseName,
                                isFirstJoin,
                                consumer,
                                dependencies,
                                itemStacks
                        )
                );
            }
            logger.info("礼包 " + databaseName + " 注册成功！");
        }
    }
}

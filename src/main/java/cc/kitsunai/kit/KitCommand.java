package cc.kitsunai.kit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class KitCommand {
    private static final LiteralArgumentBuilder<CommandSourceStack> command =
            Commands.literal("kit")
                    .then(
                            Commands.literal("get").then(
                                    Commands.argument("cdkey", StringArgumentType.word())
                                            .executes(
                                                    ctx -> {
                                                        if (ctx.getSource().getSender() instanceof Player player) {
                                                            String cdkey = ctx.getArgument("cdkey", String.class);
                                                            KitsunaiKit.kitManager.getKit(player, cdkey);
                                                            return 1;
                                                        }
                                                        ctx.getSource().getSender().sendMessage(
                                                                Component.text("你必须是一名玩家！").color(TextColor.color(255, 0, 0))
                                                        );
                                                        return 0;
                                                    }
                                            )
                            )
                    )
                    .then(
                            Commands.literal("save")
                                    .requires(ctx -> ctx.getSender().isOp())
                                    .then(
                                            Commands.argument("filename", StringArgumentType.word()).executes(
                                                    ctx -> {
                                                        if (ctx.getSource().getSender() instanceof Player player) {
                                                            if (player.getInventory().getItemInMainHand().isEmpty()) {
                                                                player.sendMessage(
                                                                        Component.text("你不能保存空气的信息！").color(TextColor.color(255, 0, 0))
                                                                );
                                                            }
                                                            String filename = ctx.getArgument("filename", String.class);
                                                            ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
                                                            byte[] bytes = itemStack.serializeAsBytes();
                                                            File dir = new File(Bukkit.getPluginsFolder(), "KitsunaiKit/save");
                                                            if (!dir.exists()) dir.mkdirs();
                                                            File file = new File(dir, filename + ".yml");
                                                            YamlConfiguration configuration = new YamlConfiguration();
                                                            String base64 = Base64.getEncoder().encodeToString(bytes);
                                                            configuration.set("data", base64);
                                                            try {
                                                                configuration.save(file);
                                                                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>数据成功保存到: " + file.getAbsolutePath()));
                                                            } catch (IOException e) {
                                                                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>保存文件失败: " + e.getMessage()));
                                                                e.printStackTrace();
                                                            }
                                                            return 1;
                                                        }
                                                        ctx.getSource().getSender().sendMessage(
                                                                Component.text("你必须是一名玩家！").color(TextColor.color(255, 0, 0))
                                                        );
                                                        return 0;
                                                    }
                                            )
                                    )
                    )
                    .then(
                            Commands.literal("reload")
                                    .requires(ctx -> ctx.getSender().isOp())
                                    .executes(
                                    ctx -> {
                                        KitsunaiKit.onReload();
                                        return 1;
                                    }
                            )
                    )
                    .then(
                            Commands.literal("generate")
                                    .requires(ctx -> ctx.getSender().isOp())
                                    .then(
                                            Commands.argument("kitId", StringArgumentType.word())
                                                    .suggests(KitsunaiKit.getKitsSuggestions())
                                                    .then(
                                                            Commands.argument("count", IntegerArgumentType.integer(0))
                                                                    .executes(
                                                                            ctx -> {
                                                                                String kitId = ctx.getArgument("kitId", String.class);
                                                                                int count = ctx.getArgument("count", Integer.class);
                                                                                Set<String> keys = KitsunaiKit.kitManager.generate(count, kitId);
                                                                                if (keys == null) {
                                                                                    ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize("<red>礼包id不存在"));
                                                                                    return 0;
                                                                                }
                                                                                int i = 1;
                                                                                for (String key : keys) {
                                                                                    ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(i + ": " + "<gold>" + key));
                                                                                    i++;
                                                                                }
                                                                                return 1;
                                                                            }
                                                                    )
                                                    )
                                    )
                    )
                    .then(
                            Commands.literal("allcdkeys")
                                    .requires(ctx -> ctx.getSender().isOp())
                                    .then(
                                            Commands.argument("kitId", StringArgumentType.word())
                                                    .suggests(KitsunaiKit.getKitsSuggestions())
                                                    .executes(
                                                            ctx -> {
                                                                String kitId = ctx.getArgument("kitId", String.class);
                                                                Set<String> keys = KitsunaiKit.kitManager.getAllCdkeys(kitId);
                                                                if (keys == null) {
                                                                    ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize("<red>礼包id不存在"));
                                                                    return 0;
                                                                }
                                                                int i = 1;
                                                                for (String key : keys) {
                                                                    ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(i + ": " + "<gold>" + key));
                                                                    i++;
                                                                }
                                                                return 1;
                                                            }
                                                    )
                                    )
                    );

    public static final LiteralCommandNode<CommandSourceStack> buildCommand = command.build();

}

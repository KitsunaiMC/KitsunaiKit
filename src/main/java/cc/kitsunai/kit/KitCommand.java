package cc.kitsunai.kit;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class KitCommand {
    public static final LiteralArgumentBuilder<CommandSourceStack> command =
            Commands.literal("kit").then(
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
            );

    public static final LiteralCommandNode<CommandSourceStack> buildCommand = command.build();

}

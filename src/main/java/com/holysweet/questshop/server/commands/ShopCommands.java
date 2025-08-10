package com.holysweet.questshop.server.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import com.holysweet.questshop.menu.ShopMenu; // server/common menu

public final class ShopCommands {
    private ShopCommands() {}

    /** Attach under /hqs ... */
    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        return Commands.literal("shop")
                // /hqs shop
                .executes(ctx -> {
                    Player p = ctx.getSource().getPlayerOrException();
                    p.openMenu(new SimpleMenuProvider(
                            (windowId, inv, player) -> new ShopMenu(windowId, inv),
                            Component.literal("Quest Shop")
                    ));
                    return 1;
                })
                // /hqs shop open  (explicit alias)
                .then(Commands.literal("open").executes(ctx -> {
                    Player p = ctx.getSource().getPlayerOrException();
                    p.openMenu(new SimpleMenuProvider(
                            (windowId, inv, player) -> new ShopMenu(windowId, inv),
                            Component.literal("Quest Shop")
                    ));
                    return 1;
                }));
    }
}

package com.holysweet.questshop.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class HqsCommands {
    private HqsCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d, CommandBuildContext ctx) {
        var root = Commands.literal("hqs");
        root.then(CoinsCommands.subtree());
        root.then(CategoriesCommands.subtree());
        root.then(ShopCommands.subtree());
        d.register(root);
    }
}

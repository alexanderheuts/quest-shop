package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.TeamCoins;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;


public final class CoinsCommands {
    private CoinsCommands() {}

    /** Attach under /hqs ... */
    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        // /hqs coins ...
        return Commands.literal("coins")
                // /hqs coins
                .executes(c -> {
                    var src = c.getSource();
                    if (!(src.getEntity() instanceof ServerPlayer self)) {
                        src.sendFailure(Component.translatable("command.questshop.player_only"));
                        return 0;
                    }
                    showCoins(src, self);
                    return 1;
                })
                // /hqs coins get [player]
                .then(Commands.literal("get")
                        .executes(c -> {
                            var src = c.getSource();
                            if (!(src.getEntity() instanceof ServerPlayer self)) {
                                src.sendFailure(Component.translatable("command.questshop.player_only"));
                                return 0;
                            }
                            showCoins(src, self);
                            return 1;
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> src.hasPermission(2))
                                .executes(c -> {
                                    var src = c.getSource();
                                    ServerPlayer target = EntityArgument.getPlayer(c, "player");
                                    showCoins(src, target);
                                    return 1;
                                })))
                // /hqs coins add <player> <amount>
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                        .executes(c -> {
                                            var src = c.getSource();
                                            ServerPlayer target = EntityArgument.getPlayer(c, "player");
                                            int amount = IntegerArgumentType.getInteger(c, "amount");
                                            return addCoins(src, target, amount);
                                        }))))
                // /hqs coins remove <player> <amount>
                .then(Commands.literal("remove")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                        .executes(c -> {
                                            var src = c.getSource();
                                            ServerPlayer target = EntityArgument.getPlayer(c, "player");
                                            int amount = IntegerArgumentType.getInteger(c, "amount");
                                            return addCoins(src, target, -amount);
                                        }))))
                // /hqs coins set <player> <amount>
                .then(Commands.literal("set")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                        .executes(c -> {
                                            var src = c.getSource();
                                            ServerPlayer target = EntityArgument.getPlayer(c, "player");
                                            int value = IntegerArgumentType.getInteger(c, "amount");
                                            return setCoins(src, target, value);
                                        }))));
    }

    private static void showCoins(CommandSourceStack src, ServerPlayer player) {
        Optional<Team> opt = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        if (opt.isEmpty()) {
            src.sendFailure(Component.translatable("command.questshop.no_team", player.getName()));
            return;
        }
        Team team = opt.get();
        int coins = TeamCoins.get(team);
        src.sendSuccess(() ->
                Component.translatable("command.questshop.coins_show", player.getName(), coins), false);
    }

    private static int addCoins(CommandSourceStack src, ServerPlayer target, int delta) {
        Optional<Team> opt = FTBTeamsAPI.api().getManager().getTeamForPlayer(target);
        if (opt.isEmpty()) {
            src.sendFailure(Component.translatable("command.questshop.no_team", target.getName()));
            return 0;
        }
        Team team = opt.get();
        int before = TeamCoins.get(team);
        int after = Math.max(0, before + delta);
        TeamCoins.set(team, after);

        if (delta >= 0) {
            src.sendSuccess(() ->
                    Component.translatable("command.questshop.coins_added", delta, target.getName(), after), true);
        } else {
            src.sendSuccess(() ->
                    Component.translatable("command.questshop.coins_removed", -delta, target.getName(), after), true);
        }
        return 1;
    }

    private static int setCoins(CommandSourceStack src, ServerPlayer target, int value) {
        Optional<Team> opt = FTBTeamsAPI.api().getManager().getTeamForPlayer(target);
        if (opt.isEmpty()) {
            src.sendFailure(Component.translatable("command.questshop.no_team", target.getName()));
            return 0;
        }
        Team team = opt.get();
        TeamCoins.set(team, Math.max(0, value));
        src.sendSuccess(() ->
                Component.translatable("command.questshop.coins_set", target.getName(), value), true);
        return 1;
    }

}
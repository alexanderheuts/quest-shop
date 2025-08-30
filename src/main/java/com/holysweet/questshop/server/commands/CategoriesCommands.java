// src/main/java/com/holysweet/questshop/server/commands/CategoriesCommands.java
package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.server.commands.util.CategoriesCommandUtil;
import com.holysweet.questshop.service.CategoriesService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.Map;

public final class CategoriesCommands {
    private CategoriesCommands() {}

    /** Attach this subtree under your /hqs root. */
    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        return Commands.literal("categories")
                .then(Commands.literal("list")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> list(ctx.getSource())))
                .then(buildToggleCommand("unlock", true))
                .then(buildToggleCommand("lock", false));
    }

    // ---------- Builders (DRY) ----------

    private static LiteralArgumentBuilder<CommandSourceStack> buildToggleCommand(String name, boolean unlock) {
        return Commands.literal(name)
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("category", ResourceLocationArgument.id())
                        .suggests(CategoriesCommandUtil.CATEGORY_SUGGESTIONS)
                        // /hqs categories <name> <category>  (self)
                        .executes(ctx -> execToggle(ctx, ctx.getSource().getPlayerOrException(), unlock))
                        // /hqs categories <name> <category> <player>
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> execToggle(ctx, EntityArgument.getPlayer(ctx, "player"), unlock))));
    }

    // ---------- Handlers ----------

    private static int list(CommandSourceStack src) {
        Map<ResourceLocation, ShopCategory> cats = ShopCatalog.INSTANCE.categories();
        src.sendSuccess(() -> Component.literal("QuestShop: " + cats.size() + " categories"), false);

        cats.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<ResourceLocation, ShopCategory> e) -> e.getValue().order())
                        .thenComparing(e -> e.getKey().toString()))
                .forEach(e -> {
                    ShopCategory c = e.getValue();
                    String line = "- " + e.getKey()
                            + "  name='" + c.display() + "'"
                            + "  order=" + c.order()
                            + "  default=" + c.unlockedByDefault();
                    src.sendSuccess(() -> Component.literal(line), false);
                });
        return 1;
    }

    /** Shared executor for unlock/lock (self or target). */
    private static int execToggle(CommandContext<CommandSourceStack> ctx, ServerPlayer target, boolean unlock) {
        ResourceLocation raw = ResourceLocationArgument.getId(ctx, "category");
        ResourceLocation id  = CategoriesCommandUtil.resolve(raw);
        return doSet(ctx.getSource(), target, id, unlock);
    }

    /**
     * Mutates unlock state and reports result.
     * Note: CategoriesService.setUnlocked(...) triggers client snapshot sync automatically on change.
     */
    private static int doSet(CommandSourceStack src, ServerPlayer target, ResourceLocation id, boolean unlock) {
        if (!ShopCatalog.INSTANCE.categories().containsKey(id)) {
            src.sendFailure(Component.literal("Unknown category: " + id));
            return 0;
        }
        boolean changed = CategoriesService.setUnlocked(target, id, unlock);
        String state = unlock ? "unlocked" : "locked";
        if (changed) {
            src.sendSuccess(() -> Component.literal(
                    "QuestShop: " + state + " '" + id + "' for " + target.getGameProfile().getName()), true);
        } else {
            src.sendSuccess(() -> Component.literal("QuestShop: no change for '" + id + "'"), false);
        }
        return 1;
    }
}

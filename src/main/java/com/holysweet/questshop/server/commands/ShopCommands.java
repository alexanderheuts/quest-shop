// src/main/java/com/holysweet/questshop/server/commands/ShopCommands.java
package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.network.Net;
import com.holysweet.questshop.server.commands.util.CategoriesCommandUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public final class ShopCommands {
    private static final Component TITLE = Component.literal("Quest Shop");

    /** Reusable menu provider for the shop UI. */
    private static final SimpleMenuProvider SHOP_PROVIDER = new SimpleMenuProvider(
            (windowId, inv, player) -> new ShopMenu(windowId, inv),
            TITLE
    );

    private ShopCommands() {}

    /** Attach this subtree under your /hqs root. */
    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        return Commands.literal("shop")
                // /hqs shop
                .executes(ShopCommands::openCommand)
                // /hqs shop open
                .then(Commands.literal("open").executes(ShopCommands::openCommand))
                // /hqs shop list-categories  (OP-only)
                .then(Commands.literal("list-categories")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> listCategories(ctx.getSource())))
                // /hqs shop list-entries <category>  (OP-only)
                .then(Commands.literal("list-entries")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("category", ResourceLocationArgument.id())
                                .suggests(CategoriesCommandUtil.CATEGORY_SUGGESTIONS)
                                .executes(ctx -> {
                                    ResourceLocation raw = ResourceLocationArgument.getId(ctx, "category");
                                    ResourceLocation catId = CategoriesCommandUtil.resolve(raw);
                                    return listEntries(ctx.getSource(), catId);
                                })));
    }

    /** Handler for /hqs shop and /hqs shop open. */
    private static int openCommand(CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        return openFor(p);
    }

    /** Opens the shop menu and pushes balance + catalog to the client. */
    private static int openFor(ServerPlayer player) {
        player.openMenu(SHOP_PROVIDER);
        Net.syncBalance(player);
        Net.sendShopData(player);
        Net.sendCategoriesSnapshot(player);
        return 1;
    }

    // ---------------------- Listing helpers ----------------------

    private static int listCategories(CommandSourceStack src) {
        // Use the real value type to keep generics intact
        Map<ResourceLocation, ShopCategory> cats =
                com.holysweet.questshop.data.ShopCatalog.INSTANCE.categories();

        src.sendSuccess(() -> Component.literal("QuestShop: " + cats.size() + " categories"), false);

        cats.entrySet().stream()
                .sorted(
                        Comparator
                                .comparingInt((Map.Entry<ResourceLocation, ShopCategory> e) -> e.getValue().order())
                                .thenComparing(e -> e.getKey().toString())
                )
                .forEach(e -> {
                    var cat = e.getValue();
                    String line = "- " + e.getKey()
                            + "  name='" + cat.display() + "'"
                            + "  order=" + cat.order()
                            + "  default=" + cat.unlockedByDefault();
                    src.sendSuccess(() -> Component.literal(line), false);
                });

        return 1;
    }

    private static int listEntries(CommandSourceStack src, ResourceLocation categoryId) {
        if (!ShopCatalog.INSTANCE.categories().containsKey(categoryId)) {
            src.sendFailure(Component.literal("Unknown category: " + categoryId));
            return 0;
        }

        var entries = ShopCatalog.INSTANCE.allEntries().stream()
                .filter(e -> e.category().equals(categoryId))
                .sorted(Comparator
                        .comparingInt(ShopEntry::cost)
                        .thenComparing(e -> e.itemId().toString()))
                .toList();

        src.sendSuccess(() -> Component.literal(
                "QuestShop: " + entries.size() + " entries in " + categoryId), false);

        for (ShopEntry e : entries) {
            Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(e.itemId());
            String itemName = opt.map(i -> i.getDescription().getString())
                    .orElse(e.itemId().toString());
            String line = "• " + itemName
                    + "  id=" + e.itemId()
                    + "  x" + e.amount()
                    + "  cost=" + e.cost();
            src.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }
}

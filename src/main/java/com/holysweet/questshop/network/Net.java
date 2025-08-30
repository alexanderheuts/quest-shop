package com.holysweet.questshop.network;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.payload.*;
import com.holysweet.questshop.service.CategoriesService;
import com.holysweet.questshop.service.CoinsService;
import com.holysweet.questshop.client.ClientShopData;
import net.minecraft.client.Minecraft;
import com.holysweet.questshop.client.screen.ShopMenuScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

@EventBusSubscriber(modid = QuestShop.MODID)
public final class Net {
    private Net() {}

    // ---------- registration ----------

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar reg = event.registrar(QuestShop.MODID).versioned("1");

        // S2C: balance cache (used by the UI)
        reg.playToClient(CoinsBalancePayload.TYPE, CoinsBalancePayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> ClientCoins.set(payload.balance()))
        );

        // S2C: buy result feedback
        reg.playToClient(BuyResultPayload.TYPE, BuyResultPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> {
                    switch (payload.code()) {
                        case INVALID_ENTRY -> ClientFX.purchaseError(Component.translatable("questshop.buy.invalid"));
                        case NOT_ENOUGH_COINS -> ClientFX.purchaseError(Component.translatable("questshop.buy.no_coins"));
                        case NO_INVENTORY_SPACE -> ClientFX.purchaseError(Component.translatable("questshop.buy.no_space"));
                        case LOCKED_CATEGORY -> ClientFX.purchaseError(Component.translatable("questshop.buy.locked"));
                        case OK -> { /* handled by BuyOkToastPayload */ }
                    }
                })
        );

        // S2C: Category data
        reg.playToClient(CategoriesSnapshotPayload.TYPE, CategoriesSnapshotPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> {
                    if (Minecraft.getInstance().screen instanceof ShopMenuScreen s) {
                        s.refreshEntries();
                    }
                })
        );

        // S2C: shop data
        reg.playToClient(ShopDataPayload.TYPE, ShopDataPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> {
                    ClientShopData.set(payload.entries());
                    // If shop is open, refresh immediately
                    if (Minecraft.getInstance().screen instanceof ShopMenuScreen s) {
                        s.refreshEntries();
                    }
                })
        );

        // S2C: purchase OK
        reg.playToClient(BuyOkToastPayload.TYPE, BuyOkToastPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> ClientFX.purchaseOk(payload.itemId(), payload.amount(), payload.cost()))
        );

        // C2S: buy request
        reg.playToServer(BuyEntryPayload.TYPE, BuyEntryPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> handleBuy(ctx.player(), payload))
        );
    }

    // ---------- public send helpers ----------

    public static void sendBalance(ServerPlayer player, int balance) {
        PacketDistributor.sendToPlayer(player, new CoinsBalancePayload(balance));
    }

    public static void syncBalance(ServerPlayer player) {
        int balance = CoinsService.get(player.serverLevel(), player);
        sendBalance(player, balance);
    }

    public static void sendBuyResult(ServerPlayer player, BuyResultPayload.Code code) {
        PacketDistributor.sendToPlayer(player, new BuyResultPayload(code));
    }

    public static void sendShopData(ServerPlayer player) {
        // Build from authoritative server catalog
        var list = ShopCatalog.INSTANCE.allEntries();
        PacketDistributor.sendToPlayer(player, new ShopDataPayload(list));
    }

    public static void sendCategoriesSnapshot(ServerPlayer player) {
        var cats = CategoriesService.categories();
        var unlocked = CategoriesService.effectiveUnlocked(player);
        PacketDistributor.sendToPlayer(player, new CategoriesSnapshotPayload(cats, unlocked));
    }

    // ---------- server buy logic (compact + readable) ----------

    private static void handleBuy(Player player, BuyEntryPayload p) {
        // Defensive: playToServer should always give us a ServerPlayer, but guard anyway.
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        ServerLevel level = sp.serverLevel();

        // 1) Resolve item id
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(p.itemId());
        if (item.isEmpty()) {
            sendBuyResult(sp, BuyResultPayload.Code.INVALID_ENTRY);
            return;
        }

        // 2) Validate against authoritative catalog
        Optional<ShopEntry> match = ShopCatalog.INSTANCE.allEntries().stream()
                .filter(e -> e.itemId().equals(p.itemId())
                        && e.amount() == p.amount()
                        && e.cost() == p.cost()
                        && e.category().equals(p.category()))
                .findFirst();
        if (match.isEmpty()) {
            sendBuyResult(sp, BuyResultPayload.Code.INVALID_ENTRY);
            return;
        }

        // 3) Check if the category is unlocked
        if (!CategoriesService.isUnlocked(sp, p.category())) {
            sendBuyResult(sp, BuyResultPayload.Code.LOCKED_CATEGORY);
            return;
        }

        // 4) Check coins
        int balance = CoinsService.get(level, sp);
        if (balance < p.cost()) {
            sendBuyResult(sp, BuyResultPayload.Code.NOT_ENOUGH_COINS);
            return;
        }

        // 5) Try to give items
        // Give all items: fill inventory first, then drop any leftover
        ItemStack stack = new ItemStack(item.get(), Math.max(1, p.amount()));
        giveOrDrop(sp, stack);

        // 6) Deduct coins and sync
        CoinsService.add(level, sp, -p.cost());    // auto-sync via CoinsService
        PacketDistributor.sendToPlayer(sp,
                new BuyOkToastPayload(p.itemId(), Math.max(1, p.amount()), p.cost()));
        Net.sendBuyResult(sp, BuyResultPayload.Code.OK);
    }

    private static void giveOrDrop(ServerPlayer sp, ItemStack toGive) {
        // Try to insert as much as possible; 'left' becomes the remainder
        ItemStack left = toGive.copy();
        boolean fullyAdded = sp.getInventory().add(left);
        if (!fullyAdded && !left.isEmpty()) {
            // Drop the exact leftover on the ground (not random scatter)
            sp.drop(left, false);
        }
    }
}

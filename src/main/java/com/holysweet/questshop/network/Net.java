package com.holysweet.questshop.network;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import com.holysweet.questshop.network.payload.CoinsBalancePayload;
import com.holysweet.questshop.service.CoinsService;
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

        // S2C: buy result feedback (simple for now; wire a toast later)
        reg.playToClient(BuyResultPayload.TYPE, BuyResultPayload.CODEC, (payload, ctx) ->
                ctx.enqueueWork(() -> {
                    switch (payload.code()) {
                        case OK -> ctx.player().displayClientMessage(Component.translatable("questshop.buy.ok"), true);
                        case INVALID_ENTRY -> ctx.player().displayClientMessage(Component.translatable("questshop.buy.invalid"), true);
                        case NOT_ENOUGH_COINS -> ctx.player().displayClientMessage(Component.translatable("questshop.buy.no_coins"), true);
                        case NO_INVENTORY_SPACE -> ctx.player().displayClientMessage(Component.translatable("questshop.buy.no_space"), true);
                        case LOCKED_CATEGORY -> ctx.player().displayClientMessage(Component.translatable("questshop.buy.locked"), true);
                    }
                })
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

        // TODO: (Unlocks intentionally omitted for now; everything is considered unlocked.)

        // 3) Check coins
        int balance = CoinsService.get(level, sp);
        if (balance < p.cost()) {
            sendBuyResult(sp, BuyResultPayload.Code.NOT_ENOUGH_COINS);
            return;
        }

        // 4) Try to give items
        // Give all items: fill inventory first, then drop any leftover
        ItemStack stack = new ItemStack(item.get(), Math.max(1, p.amount()));
        giveOrDrop(sp, stack);

        // 5) Deduct coins and sync
        CoinsService.add(level, sp, -p.cost());    // auto-sync via CoinsService
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

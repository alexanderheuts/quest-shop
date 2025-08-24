package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ShopDataPayload(List<ShopEntry> entries) implements CustomPacketPayload {
    public static final Type<ShopDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "shop_data"));

    /** Manual codec: list of (itemId, amount, cost, category) */
    public static final StreamCodec<FriendlyByteBuf, ShopDataPayload> CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ShopDataPayload decode(final @NotNull FriendlyByteBuf buf) {
            int n = buf.readVarInt();
            List<ShopEntry> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                ResourceLocation itemId = buf.readResourceLocation();
                int amount = buf.readVarInt();
                int cost = buf.readVarInt();
                ResourceLocation category = buf.readResourceLocation();
                list.add(new ShopEntry(itemId, amount, cost, category));
            }
            return new ShopDataPayload(list);
        }

        @Override
        public void encode(final @NotNull FriendlyByteBuf buf, final @NotNull ShopDataPayload value) {
            List<ShopEntry> list = value.entries();
            buf.writeVarInt(list.size());
            for (ShopEntry e : list) {
                buf.writeResourceLocation(e.itemId());
                buf.writeVarInt(e.amount());
                buf.writeVarInt(e.cost());
                buf.writeResourceLocation(e.category());
            }
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}

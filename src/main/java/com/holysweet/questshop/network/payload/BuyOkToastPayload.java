
package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BuyOkToastPayload(ResourceLocation itemId, int amount, int cost) implements CustomPacketPayload {
    public static final Type<BuyOkToastPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "buy_ok_toast"));

    public static final StreamCodec<FriendlyByteBuf, BuyOkToastPayload> CODEC = new StreamCodec<>() {
        @Override
        public @NotNull BuyOkToastPayload decode(@NotNull FriendlyByteBuf buf) {
            ResourceLocation id = buf.readResourceLocation();
            int amt = buf.readVarInt();
            int cst = buf.readVarInt();
            return new BuyOkToastPayload(id, amt, cst);
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buf, @NotNull BuyOkToastPayload v) {
            buf.writeResourceLocation(v.itemId());
            buf.writeVarInt(v.amount());
            buf.writeVarInt(v.cost());
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}

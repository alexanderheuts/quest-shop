package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BuyResultPayload(BuyResultPayload.Code code) implements CustomPacketPayload {
    public enum Code { OK, INVALID_ENTRY, NOT_ENOUGH_COINS, NO_INVENTORY_SPACE, LOCKED_CATEGORY }

    public static final Type<BuyResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "buy_result"));

    /** Manual codec: write enum ordinal as varint, read it back safely. */
    public static final StreamCodec<FriendlyByteBuf, BuyResultPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull BuyResultPayload decode(final @NotNull FriendlyByteBuf buf) {
                    int i = buf.readVarInt();
                    Code[] values = Code.values();
                    Code c = (i >= 0 && i < values.length) ? values[i] : Code.OK;
                    return new BuyResultPayload(c);
                }

                @Override
                public void encode(final @NotNull FriendlyByteBuf buf, final @NotNull BuyResultPayload value) {
                    buf.writeVarInt(value.code().ordinal());
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}

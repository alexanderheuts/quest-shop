package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopCategory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * S2C snapshot:
 *  - All known categories (id, display, unlocked_by_default, order)
 *  - The player's current effectively-unlocked set (resolved on server)
 */
public record CategoriesSnapshotPayload(
        Map<ResourceLocation, ShopCategory> categories,
        Set<ResourceLocation> unlocked
) implements CustomPacketPayload {

    public static final Type<CategoriesSnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "categories_snapshot"));

    public static final StreamCodec<FriendlyByteBuf, CategoriesSnapshotPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull CategoriesSnapshotPayload decode(@NotNull FriendlyByteBuf buf) {
                    int nCats = buf.readVarInt();
                    Map<ResourceLocation, ShopCategory> cats = new HashMap<>(nCats);
                    for (int i = 0; i < nCats; i++) {
                        ResourceLocation id = buf.readResourceLocation();
                        String display = buf.readUtf();
                        boolean defUnlocked = buf.readBoolean();
                        int order = buf.readVarInt();
                        cats.put(id, new ShopCategory(id, display, defUnlocked, order));
                    }
                    int nUnl = buf.readVarInt();
                    Set<ResourceLocation> unlocked = new HashSet<>(nUnl);
                    for (int i = 0; i < nUnl; i++) unlocked.add(buf.readResourceLocation());
                    return new CategoriesSnapshotPayload(cats, unlocked);
                }

                @Override
                public void encode(@NotNull FriendlyByteBuf buf, @NotNull CategoriesSnapshotPayload v) {
                    buf.writeVarInt(v.categories.size());
                    for (Map.Entry<ResourceLocation, ShopCategory> e : v.categories.entrySet()) {
                        buf.writeResourceLocation(e.getKey());
                        ShopCategory c = e.getValue();
                        buf.writeUtf(c.display());
                        buf.writeBoolean(c.unlockedByDefault());
                        buf.writeVarInt(c.order());
                    }
                    buf.writeVarInt(v.unlocked.size());
                    for (ResourceLocation id : v.unlocked) buf.writeResourceLocation(id);
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

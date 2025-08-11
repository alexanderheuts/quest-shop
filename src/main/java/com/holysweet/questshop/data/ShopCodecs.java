// src/main/java/com/holysweet/questshop/data/ShopCodecs.java
package com.holysweet.questshop.data;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/** Mojang codecs for datapack JSON (public model: entries only; categories handled in reloader). */
public final class ShopCodecs {
    private ShopCodecs() {}

    static final ResourceLocation DEFAULT_CATEGORY =
            ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "general");

    /** Accepts either "ns:path" or bare "path" (defaults to <modid>:path). */
    static final Codec<ResourceLocation> CATEGORY_ID_CODEC = Codec.STRING.comapFlatMap(s -> {
        if (s.indexOf(':') >= 0) {
            ResourceLocation rl = ResourceLocation.tryParse(s);
            return rl != null ? DataResult.success(rl) : DataResult.error(() -> "Invalid id: " + s);
        }
        return DataResult.success(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, s));
    }, ResourceLocation::toString);

    /** Codec for purchasable entries. Category defaults to <modid>:general. */
    static final Codec<ShopEntry> ENTRY_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(ShopEntry::itemId),
            Codec.INT.optionalFieldOf("amount", 1).forGetter(ShopEntry::amount),
            Codec.INT.optionalFieldOf("cost", 0).forGetter(ShopEntry::cost),
            CATEGORY_ID_CODEC.optionalFieldOf("category", DEFAULT_CATEGORY).forGetter(ShopEntry::category)
    ).apply(i, ShopEntry::new));
}

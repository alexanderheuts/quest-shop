package com.holysweet.questshop.integrations.ftbquests.reward;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.service.CategoriesService;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FTB Quests reward that (un)locks a single QuestShop category for the claimer.
 * Default behavior: unlock = true.
 */
public class CategoryReward extends Reward {
    public static final RewardType TYPE = RewardTypes.register(
            ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "category"),
            CategoryReward::new,
            () -> Icons.LOCK_OPEN
    );

    /** Single category to (un)lock (canonical ns:path). */
    private ResourceLocation category = ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "general");
    /** true = unlock, false = lock */
    private boolean unlock = true;

    public CategoryReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    // ---------------- persistence (quests file) ----------------

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putString("category", category.toString());
        if (!unlock) nbt.putBoolean("unlock", false); // default true; only write when false
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        String s = nbt.getString("category");
        if (!s.isEmpty()) {
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl != null) category = rl;
        }
        unlock = !nbt.contains("unlock") || nbt.getBoolean("unlock");
    }

    // ---------------- network (editor/client sync) ----------------

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buf) {
        super.writeNetData(buf);
        buf.writeResourceLocation(category);
        buf.writeBoolean(unlock);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buf) {
        super.readNetData(buf);
        category = buf.readResourceLocation();
        unlock = buf.readBoolean();
    }

    // ---------------- editor UI ----------------

    @Override
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);

        // Build the selectable options from currently loaded categories
        Map<ResourceLocation, ?> cats = ShopCatalog.INSTANCE.categories();
        List<ResourceLocation> options = new ArrayList<>(cats.keySet());
        options.sort(Comparator.comparing(ResourceLocation::toString));

        // Ensure we always have at least a sensible default in the list
        ResourceLocation defaultCat = category;
        if (options.isEmpty()) {
            options.add(defaultCat);
        } else if (!cats.containsKey(defaultCat)) {
            // if current category no longer exists, pick the first available as default
            defaultCat = options.getFirst();
        }

        NameMap<ResourceLocation> nameMap =
                NameMap.of(defaultCat, options.toArray(ResourceLocation[]::new))
                        .create();

        group.addEnum(
                "category",
                category,
                v -> { if (v != null) category = v; },
                nameMap,
                defaultCat
        ).setNameKey("reward.questshop.category.category");

        group.addBool("unlock", unlock, v -> unlock = v, true)
                .setNameKey("reward.questshop.category.unlock");
    }

    // ---------------- claim (server) ----------------

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        Objects.requireNonNull(player, "player");
        if (!ShopCatalog.INSTANCE.categories().containsKey(category)) {
            return; // ignore stale ids
        }
        CategoriesService.setUnlocked(player, category, unlock);
    }

    // ---------------- client-facing labels/icons ----------------

    @Override
    public Component getAltTitle() {
        // "Unlock category questshop:building" or "Lock category questshop:building"
        return Component.translatable(
                unlock ? "reward.questshop.category.title.unlock" : "reward.questshop.category.title.lock",
                category.toString()
        );
    }

    @Override
    public Icon getAltIcon() {
        return unlock ? Icons.LOCK_OPEN : Icons.LOCK;
    }

    /** Touching TYPE ensures registration via static initializer. */
    public static void bootstrap() { /* no-op */ }
}

package com.holysweet.questshop.integrations.ftbquests.reward;

import com.holysweet.questshop.integrations.ftbteams.TeamCoins;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class CoinsReward extends Reward {
    private int amount = 10;

    public static final RewardType TYPE = RewardTypes.register(
            ResourceLocation.fromNamespaceAndPath("questshop", "coins"),
            CoinsReward::new,
            () -> Icons.MONEY_BAG
    );

    public CoinsReward(long id, Quest quest) { super(id, quest); }
    @Override public RewardType getType() { return TYPE; }

    // --- persistence (quests file)
    @Override public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        if (amount != 10) nbt.putInt("amount", amount);
    }
    @Override public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        if (nbt.contains("amount")) amount = Math.max(0, nbt.getInt("amount"));
    }

    // --- network (editor/client sync)
    @Override public void writeNetData(RegistryFriendlyByteBuf buf) {
        super.writeNetData(buf);
        buf.writeVarInt(amount);
    }
    @Override public void readNetData(RegistryFriendlyByteBuf buf) {
        super.readNetData(buf);
        amount = buf.readVarInt();
    }

    // --- editor UI
    @Override public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);
        group.addInt("amount", amount, v -> amount = Math.max(0, v), 10, 0, Integer.MAX_VALUE)
                .setNameKey("reward.questshop.coins.amount");
    }

    // --- claim (server)
    @Override public void claim(ServerPlayer player, boolean notify) {
        var api = FTBTeamsAPI.api();
        if (!api.isManagerLoaded()) return;
        Optional<Team> teamOpt = api.getManager().getTeamForPlayer(player);
        if (teamOpt.isPresent() && amount > 0) {
            TeamCoins.add(teamOpt.get(), amount);
        }
    }

    // --- client-facing labels/icons (called client-side by FTBQ)
    @Override public Component getAltTitle() { return Component.translatable("reward.questshop.coins.title", amount); }
    @Override public Icon getAltIcon() { return Icons.MONEY_BAG; }

    public static void bootstrap() {} // touching TYPE ensures registration
}

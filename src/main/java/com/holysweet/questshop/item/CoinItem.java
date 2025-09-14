package com.holysweet.questshop.item;

import com.holysweet.questshop.service.CoinsService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CoinItem extends Item {

    public CoinItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack itemInHand = player.getItemInHand(usedHand);

        if(level.isClientSide()) {
            level.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, .5f, 1);
            return InteractionResultHolder.consume(itemInHand);
        }

        int amountConsumed = player.isShiftKeyDown() ? itemInHand.getCount() : 1;
        itemInHand.shrink(amountConsumed);

        CoinsService.add((ServerLevel) level, (ServerPlayer) player, amountConsumed);
        
        if(!itemInHand.isEmpty())
            return InteractionResultHolder.success(itemInHand);

        player.setItemInHand(usedHand, ItemStack.EMPTY);
        return InteractionResultHolder.consume(itemInHand);
    }
}

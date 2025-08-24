// src/main/java/com/holysweet/questshop/client/ClientFX.java
package com.holysweet.questshop.client;

import com.holysweet.questshop.client.toast.PurchaseToast;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ClientFX {
    private ClientFX() {}

    public static void purchaseOk(@NotNull ResourceLocation itemId, int amount, int cost) {
        Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(itemId);
        ItemStack stack = new ItemStack(opt.orElse(Items.BARRIER), Math.max(1, amount));

        String name = opt.map(i -> Component.translatable(i.getDescriptionId()).getString())
                .orElse(itemId.toString());

        Component title = Component.translatable("questshop.buy.ok");
        Component desc  = Component.translatable("questshop.buy.toast.detail", amount, name, cost);

        Minecraft.getInstance().getToasts().addToast(new PurchaseToast(stack, title, desc));
    }

    public static void purchaseError(@NotNull Component detail) {
        // Keep using a simple system toast alternative: reuse our PurchaseToast styling without icon
        ItemStack empty = new ItemStack(Items.BARRIER, 1);
        Component title = Component.translatable("questshop.buy.failed");
        Minecraft.getInstance().getToasts().addToast(new PurchaseToast(empty, title, detail));
    }
}

package com.possible_triangle.create_jetpack.mixins;

import com.possible_triangle.create_jetpack.item.Jetpack;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.StreamSupport;

@Mixin(BackTankUtil.class)
public class BackTankUtilMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "get(Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;")
    private static void getTankByTag(LivingEntity entity, CallbackInfoReturnable<ItemStack> callback) {
        StreamSupport.stream(entity.getArmorInventoryList().spliterator(), false)
                .filter(it -> it.getItem() instanceof Jetpack)
                .findAny()
                .ifPresent(callback::setReturnValue);
    }

}

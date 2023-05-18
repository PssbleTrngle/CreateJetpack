package com.possible_triangle.create_jetpack.mixins;

import com.possible_triangle.create_jetpack.Content;
import com.possible_triangle.create_jetpack.compat.CuriosCompat;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BackTankUtil.class, remap = false)
public class BackTankUtilMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "get(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;")
    private static void provideCuriosAir(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        var stacks = CuriosCompat.INSTANCE.getCuriosStacksSafe(entity);
        stacks.stream()
                .filter(it -> it.is(Content.INSTANCE.getPRESSURIZED_AIR_SOURCES()))
                .findFirst().ifPresent(cir::setReturnValue);
    }

}

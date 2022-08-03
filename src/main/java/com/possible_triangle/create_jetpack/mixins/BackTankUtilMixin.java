package com.possible_triangle.create_jetpack.mixins;

import com.possible_triangle.create_jetpack.Content;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * Will be removed next create version, since create#3319 got merged
 */
@Mixin(value = BackTankUtil.class, remap = false)
public class BackTankUtilMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "get(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;")
    private static void getTankByTag(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        for (ItemStack itemStack : entity.getArmorSlots())
            if (itemStack.is(Content.INSTANCE.getPRESSURIZED_AIR_SOURCES()))
                cir.setReturnValue(itemStack);
    }

}

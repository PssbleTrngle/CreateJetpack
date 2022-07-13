package com.possible_triangle.create_jetpack.mixins;

import com.possible_triangle.create_jetpack.capability.JetpackLogic;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BackTankUtil.class)
public class BackTankUtilMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "get(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;")
    private static void getTankByTag(LivingEntity entity, CallbackInfoReturnable<ItemStack> callback) {
        //StreamSupport.stream(entity.getArmorSlots().spliterator(), false)
        //        .filter(it -> it.getItem() instanceof Jetpack)
        //        .findAny()
        //        .ifPresent(callback::setReturnValue);
        var jetpack = Optional.ofNullable(JetpackLogic.INSTANCE.getJetpack(entity));
        jetpack.ifPresent(pair -> callback.setReturnValue(pair.component1().getStack()));
    }

}

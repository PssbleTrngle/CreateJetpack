package com.possible_triangle.create_jetpack.compat

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fml.ModList
import top.theillusivec4.curios.api.CuriosCapability

object CuriosCompat {

    fun getCuriosStacksSafe(entity: LivingEntity): List<ItemStack> {
        if (!ModList.get().isLoaded("curios")) return emptyList()

        val curios = entity.getCapability(CuriosCapability.INVENTORY)
        return curios.map {
            it.curios.entries.flatMap { (slot, handler) ->
                val slots = 0 until handler.slots
                slots.map { index ->
                    handler.stacks.getStackInSlot(index)
                }
            }
        }.orElseGet(::emptyList)
    }


}
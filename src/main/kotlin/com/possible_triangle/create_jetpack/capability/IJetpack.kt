package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.item.Jetpack.ControlType
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.item.ItemStack
import net.minecraft.world.World

interface IJetpack {

    data class Context(
        val entity: LivingEntity,
        val world: World,
        val stack: ItemStack? = null,
        val slot: EquipmentSlotType? = null
    )

    fun activeType(context: Context): ControlType {
        return ControlType.ALWAYS
    }

    fun horizontalSpeed(context: Context): Double
    fun verticalSpeed(context: Context): Double
    fun acceleration(context: Context): Double

    fun hoverType(context: Context): ControlType
    fun hoverSpeed(context: Context): Double
    fun hoverVerticalSpeed(context: Context): Double {
        return verticalSpeed(context) * 0.8
    }
    fun hoverHorizontalSpeed(context: Context): Double {
        return horizontalSpeed(context) * 0.8
    }

    fun isUsable(context: Context): Boolean

    fun onUse(context: Context) {}

}
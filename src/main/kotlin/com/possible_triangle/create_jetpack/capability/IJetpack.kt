package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.item.Jetpack.ControlType
import com.possible_triangle.create_jetpack.network.ControlManager
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface IJetpack {

    data class Context(
        val entity: LivingEntity,
        val world: Level,
        val stack: ItemStack? = null,
        val slot: EquipmentSlot? = null,
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

    fun isThrusting(context: Context): Boolean {
        val entity = context.entity
        if (JetpackLogic.active(hoverType(context), ControlManager.Key.TOGGLE_HOVER, entity)) return true
        return entity is Player && ControlManager.isPressed(entity, ControlManager.Key.UP)
    }

}
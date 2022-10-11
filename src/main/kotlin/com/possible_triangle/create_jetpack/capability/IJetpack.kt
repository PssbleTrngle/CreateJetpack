package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.capability.JetpackLogic.FlyingPose
import com.possible_triangle.create_jetpack.capability.sources.ISource
import com.possible_triangle.create_jetpack.capability.JetpackLogic.ControlType
import com.possible_triangle.create_jetpack.network.ControlManager
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

interface IJetpack {

    data class Context(
        val jetpack: IJetpack,
        val entity: LivingEntity,
        val world: Level,
        val pose: FlyingPose,
        val source: ISource,
    ) {
        companion object {
            fun builder(
                entity: LivingEntity,
                world: Level,
                pose: FlyingPose,
                source: ISource,
            ): (IJetpack) -> Context {
                return { Context(it, entity, world, pose, source) }
            }
        }
    }

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

    fun swimModifier(context: Context): Double

    fun isValid(context: Context): Boolean
    fun isUsable(context: Context): Boolean

    fun onUse(context: Context) {}

    /**
     * Used to display the particles
     * Return `null` if you don't want particles to be added
     */
    fun getThrusters(context: Context): List<Vec3>?

    fun isHovering(context: Context): Boolean {
        return JetpackLogic.active(hoverType(context), ControlManager.Key.TOGGLE_HOVER, context.entity)
    }

    fun isThrusting(context: Context): Boolean {
        val entity = context.entity
        if (!JetpackLogic.active(
                context.jetpack.activeType(context),
                ControlManager.Key.TOGGLE_ACTIVE,
                entity
            )
        ) return false
        if (context.pose == FlyingPose.SUPERMAN && entity.deltaMovement.length() > 0.1) return true
        if (isHovering(context) && !entity.isOnGround) return true
        return ControlManager.Key.UP.isPressed(entity)
    }

}
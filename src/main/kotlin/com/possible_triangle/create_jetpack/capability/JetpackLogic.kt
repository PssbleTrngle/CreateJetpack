package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType.*
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import kotlin.math.max
import kotlin.math.min

@Mod.EventBusSubscriber
object JetpackLogic {

    private val DIRECTIONS = listOf(
        Key.BACKWARD to Vector3d(0.0, 0.0, -1.0).scale(0.8),
        Key.FORWARD to Vector3d(0.0, 0.0, 1.0).scale(1.2),
        Key.LEFT to Vector3d(1.0, 0.0, 0.0),
        Key.RIGHT to Vector3d(-1.0, 0.0, 0.0),
    )

    private fun active(type: ControlType, key: Key, entity: LivingEntity): Boolean {
        return when (type) {
            ALWAYS -> true
            NEVER -> false
            TOGGLE -> entity is PlayerEntity && ControlManager.isPressed(entity, key)
        }
    }

    fun getJetpack(entity: LivingEntity): Pair<Context, IJetpack>? {
        val world = entity.world ?: return null

        val equipment = EquipmentSlotType.values().map {
            val stack = entity.getItemStackFromSlot(it)
            Context(entity, world, stack, it) to stack
        }

        val sources = listOf<List<Pair<Context, ICapabilityProvider>>>(
            equipment,
            listOf(Context(entity, world) to entity),
        ).flatten()

        return sources.asSequence()
            .map { it.first to it.second.getCapability(JETPACK_CAPABILITY) }
            .filter { it.second.isPresent }
            .map { it.first to it.second.resolve().get() }
            .filter { active(it.second.activeType(it.first), Key.TOGGLE_ACTIVE, entity) }
            .firstOrNull { it.second.isUsable(it.first) }

    }

    private fun tick(entity: LivingEntity) {
        val (context, jetpack) = getJetpack(entity) ?: return

        val buttonUp = entity is PlayerEntity && ControlManager.isPressed(entity, Key.UP)
        val buttonDown = entity.isSneaking
        val hovering = active(jetpack.hoverType(context), Key.TOGGLE_HOVER, entity)

        if (entity.isOnGround && !buttonUp) return

        jetpack.onUse(context)

        val verticalSpeed = if (hovering) jetpack.hoverVerticalSpeed(context) else jetpack.verticalSpeed(context)
        val horizontalSpeed =
            if (hovering) jetpack.hoverHorizontalSpeed(context) else jetpack.horizontalSpeed(context)
        val acceleration = jetpack.acceleration(context)

        val speed = when {
            buttonUp && !buttonDown -> verticalSpeed
            buttonDown && !buttonUp -> -verticalSpeed
            hovering -> jetpack.hoverSpeed(context)
            else -> null
        }

        if (speed != null) {

            if (entity is PlayerEntity) {
                DIRECTIONS.filter { ControlManager.isPressed(entity, it.first) }.forEach {
                    val vec = Vector3d(it.second.x, 0.0, it.second.z).scale(horizontalSpeed)
                    entity.moveRelative(1F, vec)
                }
            }

            val motion = entity.motion

            val motionY = if (speed <= 0) max(motion.y, speed)
            else min(motion.y + acceleration, speed)

            entity.setMotion(motion.x, motionY, motion.z)

            if (entity is ServerPlayerEntity) {
                entity.fallDistance = 0F
                entity.connection.floatingTickCount = 0
            }
        }

    }

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        tick(event.player)
    }

}
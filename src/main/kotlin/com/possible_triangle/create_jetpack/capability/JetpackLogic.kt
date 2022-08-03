package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType.*
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.common.Mod
import kotlin.math.max
import kotlin.math.min

@Mod.EventBusSubscriber
object JetpackLogic {

    private val DIRECTIONS = listOf(
        Key.BACKWARD to Vec3(0.0, 0.0, -1.0).scale(0.8),
        Key.FORWARD to Vec3(0.0, 0.0, 1.0).scale(1.2),
        Key.LEFT to Vec3(1.0, 0.0, 0.0),
        Key.RIGHT to Vec3(-1.0, 0.0, 0.0),
    )

    fun active(type: ControlType, key: Key, entity: LivingEntity): Boolean {
        return when (type) {
            ALWAYS -> true
            NEVER -> false
            TOGGLE -> entity is Player && ControlManager.isPressed(entity, key)
        }
    }

    fun getActiveJetpack(entity: LivingEntity): Pair<Context, IJetpack>? {
        if(entity is Player && entity.abilities.flying) return null
        return getJetpack(entity)
            ?.takeIf { active(it.second.activeType(it.first), Key.TOGGLE_ACTIVE, entity) }
            ?.takeIf { it.second.isUsable(it.first) }
    }

    fun getJetpack(entity: LivingEntity): Pair<Context, IJetpack>? {
        val world = entity.level ?: return null

        val equipment = EquipmentSlot.values().map {
            val stack = entity.getItemBySlot(it)
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
            .firstOrNull()
    }

    fun tick(event: TickEvent.PlayerTickEvent) {
        val entity = event.player
        val (context, jetpack) = getActiveJetpack(entity) ?: return

        val buttonUp = entity is Player && ControlManager.isPressed(entity, Key.UP)
        // TODO check if this == sneaking
        val buttonDown = entity.isShiftKeyDown
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

            if (entity is Player) {
                DIRECTIONS.filter { ControlManager.isPressed(entity, it.first) }.forEach {
                    val vec = Vec3(it.second.x, 0.0, it.second.z).scale(horizontalSpeed)
                    entity.moveRelative(1F, vec)
                }
            }

            val motion = entity.deltaMovement

            val motionY = if (speed <= 0) max(motion.y, speed)
            else min(motion.y + acceleration, speed)

            entity.setDeltaMovement(motion.x, motionY, motion.z)

            if (entity is ServerPlayer) {
                entity.fallDistance = 0F
                entity.connection.aboveGroundTickCount = 0
            }
        }

    }

}
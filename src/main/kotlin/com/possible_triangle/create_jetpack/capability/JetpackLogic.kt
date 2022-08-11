package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.Content
import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.CreateJetpackMod
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.item.BronzeJetpack.ControlType
import com.possible_triangle.create_jetpack.item.BronzeJetpack.ControlType.*
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import com.simibubi.create.content.contraptions.particle.AirParticleData
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.common.Mod
import java.util.*
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

    fun getActiveJetpack(entity: LivingEntity): Context? {
        if (entity is Player && entity.abilities.flying) return null
        return getJetpack(entity)
            ?.takeIf { active(it.jetpack.activeType(it), Key.TOGGLE_ACTIVE, entity) }
            ?.takeIf { it.jetpack.isUsable(it) }
    }

    fun getJetpack(entity: LivingEntity): Context? {
        val world = entity.level ?: return null

        val pose = FlyingPose.get(entity)

        val equipment = EquipmentSlot.values().map {
            val stack = entity.getItemBySlot(it)
            Context.builder(entity, world, pose, stack, it) to stack
        }

        val sources = listOf<List<Pair<(IJetpack) -> Context, ICapabilityProvider>>>(
            equipment,
            listOf(Context.builder(entity, world, pose) to entity),
        ).flatten()

        return sources.asSequence()
            .map { it.first to it.second.getCapability(JETPACK_CAPABILITY) }
            .filter { it.second.isPresent }
            .map { it.first to it.second.resolve().get() }
            .map { it.first(it.second) }
            .firstOrNull()
    }

    private val ATTRIBUTE_ID = UUID.fromString("f4f2d961-fac9-42c2-93b8-69abd884d386")

    enum class FlyingPose {
        UPRIGHT, SUPERMAN;

        companion object {
            fun get(entity: LivingEntity): FlyingPose {
                return if (entity.isFallFlying) SUPERMAN
                else if (entity.isVisuallySwimming && entity.isSprinting && (entity !is Player || entity.isAffectedByFluids)) SUPERMAN
                else UPRIGHT
            }
        }
    }

    private fun handleSwimModifier(entity: LivingEntity, context: Context?) {
        val attribute = entity.getAttribute(ForgeMod.SWIM_SPEED.get()) ?: return

        val hasModifier = attribute.getModifier(ATTRIBUTE_ID) != null
        val shouldHaveModifier = context?.pose == FlyingPose.SUPERMAN && entity.isUnderWater

        if (!shouldHaveModifier && hasModifier) attribute.removeModifier(ATTRIBUTE_ID)
        else if (shouldHaveModifier && !hasModifier) {
            val modifier = context!!.jetpack.swimModifier(context)
            if (modifier > 0) attribute.addPermanentModifier(
                AttributeModifier(
                    ATTRIBUTE_ID,
                    "${CreateJetpackMod.MOD_ID}:boost",
                    modifier,
                    Operation.MULTIPLY_TOTAL
                )
            )
        }
    }

    fun tick(event: TickEvent.PlayerTickEvent) {
        val player = event.player
        val context = getActiveJetpack(player)
        handleSwimModifier(player, context)

        if (context == null) return

        val isUsed = when (context.pose) {
            FlyingPose.SUPERMAN -> elytraBoost(context)
            FlyingPose.UPRIGHT -> uprightMovement(context)
        }

        if (isUsed) {
            spawnParticles(context)

            Content.SOUND_WHOOSH.play(context.world,player, player.x, player.y, player.z , 1F, 1F)

            context.jetpack.onUse(context)
        }
    }

    private fun elytraBoost(ctx: Context): Boolean {
        val entity = ctx.entity
        if (!entity.isFallFlying) return true
        if (entity !is Player || !ControlManager.isPressed(entity, Key.UP)) return false

        if (entity.level.gameTime % 15 == 0L) {
            val look = entity.lookAngle
            val factor = { i: Double -> i * 0.1 + (i * 1.1 - i) * 0.5 }
            entity.deltaMovement = entity.deltaMovement.add(
                factor(look.x),
                factor(look.y),
                factor(look.z),
            )

        }

        return true
    }

    private fun uprightMovement(ctx: Context): Boolean {
        val entity = ctx.entity
        val buttonUp = entity is Player && ControlManager.isPressed(entity, Key.UP)
        val buttonDown = entity.isShiftKeyDown
        val hovering = active(ctx.jetpack.hoverType(ctx), Key.TOGGLE_HOVER, entity)

        if (ctx.entity.isOnGround && !buttonUp) return false

        val verticalSpeed =
            if (hovering) ctx.jetpack.hoverVerticalSpeed(ctx)
            else ctx.jetpack.verticalSpeed(ctx)

        val horizontalSpeed =
            if (hovering) {
                if (entity.isUnderWater) 0.0
                else ctx.jetpack.hoverHorizontalSpeed(ctx)
            } else ctx.jetpack.horizontalSpeed(ctx)
        val acceleration = ctx.jetpack.acceleration(ctx)

        val speed = when {
            buttonUp && !buttonDown -> verticalSpeed
            buttonDown && !buttonUp -> -verticalSpeed
            hovering && entity.isUnderWater -> 0.0
            hovering -> ctx.jetpack.hoverSpeed(ctx)
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

        return true
    }

    private fun spawnParticles(context: Context) {
        val thrusters = context.jetpack.getThrusters(context) ?: return
        val yaw = (context.entity.yBodyRot / 180 * -Math.PI).toFloat()
        thrusters.map { it.yRot(yaw) }.forEach { pos ->
            val particle = if (context.entity.isUnderWater) ParticleTypes.BUBBLE
            else AirParticleData(0F, 0.01F)
            context.world.addParticle(
                particle,
                context.entity.x + pos.x,
                context.entity.y + pos.y,
                context.entity.z + pos.z,
                0.0,
                -1.0,
                0.0
            )
        }
    }

}
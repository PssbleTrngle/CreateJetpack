package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.Content
import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.CreateJetpackMod
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.capability.JetpackLogic.ControlType.*
import com.possible_triangle.create_jetpack.capability.sources.EntitySource
import com.possible_triangle.create_jetpack.capability.sources.EquipmentSource
import com.possible_triangle.create_jetpack.capability.sources.ISource
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import com.simibubi.create.content.contraptions.particle.AirParticleData
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.event.TickEvent
import java.util.*
import kotlin.math.max
import kotlin.math.min

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
            TOGGLE -> key.isPressed(entity)
        }
    }

    fun getActiveJetpack(entity: LivingEntity): Context? {
        if (entity is Player && entity.abilities.flying) return null
        return getJetpack(entity)?.takeIf { active(it.jetpack.activeType(it), Key.TOGGLE_ACTIVE, entity) }
            ?.takeIf { it.jetpack.isUsable(it) }
    }

    init {
        ISource.addSource { player -> listOf(player to EntitySource) }
        ISource.addSource { player ->
            EquipmentSlot.values().map {
                val stack = player.getItemBySlot(it)
                stack to EquipmentSource(it)
            }
        }
    }

    fun getJetpack(entity: LivingEntity): Context? {
        val world = entity.level ?: return null

        val pose = FlyingPose.get(entity)

        val sources = ISource.get(entity)

        return sources.asSequence().map { (provider, source) -> source to provider.getCapability(JETPACK_CAPABILITY) }
            .filter { (_, capability) -> capability.isPresent }
            .map { (source, capability) -> Context.builder(entity, world, pose, source) to capability }
            .map { (builder, capability) -> builder to capability.resolve().get() }
            .map { (builder, capability) -> builder(capability) }.filter { it.jetpack.isValid(it) }.firstOrNull()
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
                    ATTRIBUTE_ID, "${CreateJetpackMod.MOD_ID}:boost", modifier, Operation.MULTIPLY_TOTAL
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
            playSound(context)
            context.jetpack.onUse(context)
        }
    }

    private fun playSound(context: Context) {
        val pos = context.entity.blockPosition()

        val volume = if (Key.UP.isPressed(context.entity)) 2F else 1F
        val pitch = context.world.random.nextFloat() * 0.4F + 1F

        fun SoundEvent.play(volume: Float = 1F, pitch: Float = 1F) {
            context.world.playSound(
                null, pos, this, SoundSource.PLAYERS, volume, pitch
            )
        }

        if (context.entity.isUnderWater) {

            if (context.world.gameTime % 10 != 0L) return

            val (sound, volumeModifier) = when (context.pose) {
                FlyingPose.SUPERMAN -> SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE to -0.5F
                else -> SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT to 0F
            }

            sound.play(volume + volumeModifier, pitch - 0.5F)
        } else {
            if (context.world.gameTime % 5 != 0L) return
            Content.SOUND_WHOOSH.get().play(volume, pitch)
        }
    }

    private fun elytraBoost(ctx: Context): Boolean {
        val entity = ctx.entity
        if (!entity.isFallFlying) return true
        if (entity !is Player || !Key.UP.isPressed(entity)) return false

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
        val buttonUp = Key.UP.isPressed(entity)
        val buttonDown = entity.isShiftKeyDown
        val hovering = active(ctx.jetpack.hoverType(ctx), Key.TOGGLE_HOVER, entity)

        if (ctx.entity.isOnGround && !buttonUp) return false

        val verticalSpeed = if (hovering) ctx.jetpack.hoverVerticalSpeed(ctx)
        else ctx.jetpack.verticalSpeed(ctx)

        val horizontalSpeed = if (hovering) {
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
                DIRECTIONS.filter { it.first.isPressed(entity) }.forEach {
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
        val pitch = (context.entity.xRot / 180 * -Math.PI).toFloat()
        val xRot = when (context.pose) {
            FlyingPose.SUPERMAN -> pitch
            FlyingPose.UPRIGHT -> 0F
        }
        thrusters.map { it.xRot(xRot) }.map { it.yRot(yaw) }.forEach { pos ->
            val particle = if (context.entity.isUnderWater) ParticleTypes.BUBBLE
            else AirParticleData(0F, 0.01F)
            context.world.addParticle(
                particle, context.entity.x + pos.x, context.entity.y + pos.y, context.entity.z + pos.z, 0.0, -1.0, 0.0
            )
        }
    }

    enum class ControlType {
        ALWAYS, NEVER, TOGGLE
    }

}
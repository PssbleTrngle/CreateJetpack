package com.possible_triangle.create_jetpack.item

import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.simibubi.create.content.curiosities.armor.BackTankUtil
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem
import com.simibubi.create.repack.registrate.util.entry.ItemEntry
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.Rarity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import kotlin.math.cos
import kotlin.math.sin

class Jetpack(properties: Properties, blockItem: ItemEntry<CopperBacktankBlockItem>) :
    CopperBacktankItem(properties.rarity(Rarity.RARE), blockItem),
    IJetpack,
    ICapabilityProvider {
    private val capability = LazyOptional.of<IJetpack> { this }

    override fun hoverSpeed(context: Context): Double {
        return -0.03
    }

    override fun verticalSpeed(context: Context): Double {
        return 1.0
    }

    override fun activeType(context: Context): ControlType {
        return ControlType.TOGGLE
    }

    override fun hoverType(context: Context): ControlType {
        return ControlType.TOGGLE
    }

    override fun horizontalSpeed(context: Context): Double {
        return 0.02
    }

    override fun acceleration(context: Context): Double {
        return 0.6
    }

    override fun onUse(context: Context) {
        val yaw = (context.entity.rotationVector.x / 180 * -Math.PI).toFloat()
        val rotationX = sin(yaw) * 0.5
        val rotationZ = cos(yaw) * 0.5
        context.world.addParticle(
            ParticleTypes.SMOKE,
            context.entity.x - rotationX,
            context.entity.y + 0.6,
            context.entity.z - rotationZ,
            0.0,
            -0.1,
            0.0
        )
    }

    override fun isUsable(context: Context): Boolean {
        return BackTankUtil.canAbsorbDamage(context.entity, 1000)
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == JETPACK_CAPABILITY) return capability.cast()
        return LazyOptional.empty()
    }

    enum class ControlType {
        ALWAYS, NEVER, TOGGLE
    }

}
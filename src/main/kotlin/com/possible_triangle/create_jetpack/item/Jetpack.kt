package com.possible_triangle.create_jetpack.item

import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.simibubi.create.content.contraptions.particle.AirParticleData
import com.simibubi.create.content.curiosities.armor.BackTankUtil
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem
import com.simibubi.create.repack.registrate.util.entry.ItemEntry
import net.minecraft.core.Direction
import net.minecraft.world.item.Rarity
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class Jetpack(properties: Properties, blockItem: ItemEntry<CopperBacktankBlockItem>) :
    CopperBacktankItem(properties.rarity(Rarity.RARE), blockItem),
    IJetpack,
    ICapabilityProvider {
    private val capability = LazyOptional.of<IJetpack> { this }

    override fun hoverSpeed(context: Context): Double {
        return -0.03
    }

    override fun verticalSpeed(context: Context): Double {
        return 0.4
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
        if(!isThrusting(context)) return
        val yaw = (context.entity.yBodyRot / 180 * -Math.PI).toFloat()
        listOf(-0.35, 0.35).forEach { offset ->
            val pos = Vec3(offset, 0.7, -0.5).yRot(yaw)
            context.world.addParticle(
                AirParticleData(0F, 0.01F),
                context.entity.x + pos.x,
                context.entity.y + pos.y,
                context.entity.z + pos.z,
                0.0,
                -1.0,
                0.0
            )
        }
    }

    override fun isUsable(context: Context): Boolean {
        return BackTankUtil.canAbsorbDamage(context.entity, 10)
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == JETPACK_CAPABILITY) return capability.cast()
        return LazyOptional.empty()
    }

    enum class ControlType {
        ALWAYS, NEVER, TOGGLE
    }

}
package com.possible_triangle.create_jetpack.item

import com.possible_triangle.create_jetpack.Content
import com.possible_triangle.create_jetpack.Content.JETPACK_CAPABILITY
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.simibubi.create.content.curiosities.armor.BackTankUtil
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.item.Rarity
import net.minecraft.particles.ParticleTypes
import net.minecraft.util.Direction
import net.minecraft.util.math.MathHelper
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class Jetpack :
    CopperBacktankItem(PROPERTIES, BlockItem(Content.JETPACK_BLOCK, PROPERTIES)),
    IJetpack,
    ICapabilityProvider {
    private val capability = LazyOptional.of<IJetpack> { this }

    companion object {
        val PROPERTIES = Properties().rarity(Rarity.RARE).group(ItemGroup.SEARCH)
    }

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
        val yaw = (context.entity.rotationYaw / 180 * -Math.PI).toFloat()
        val rotationX = MathHelper.sin(yaw) * 0.5
        val rotationZ = MathHelper.cos(yaw) * 0.5
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
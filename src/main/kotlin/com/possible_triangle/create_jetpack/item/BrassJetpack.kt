package com.possible_triangle.create_jetpack.item

import com.possible_triangle.create_jetpack.config.Configs
import com.possible_triangle.flightlib.api.ControlType
import com.possible_triangle.flightlib.api.IJetpack
import com.possible_triangle.flightlib.api.IJetpack.Context
import com.possible_triangle.flightlib.api.sources.CuriosSource
import com.possible_triangle.flightlib.api.sources.EquipmentSource
import com.possible_triangle.flightlib.forge.api.ForgeFlightLib.JETPACK_CAPABILITY
import com.simibubi.create.content.contraptions.particle.AirParticleData
import com.simibubi.create.content.curiosities.armor.BackTankUtil
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem
import com.tterrag.registrate.util.entry.ItemEntry
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class BrassJetpack(properties: Properties, blockItem: ItemEntry<CopperBacktankBlockItem>) :
    CopperBacktankItem(properties.rarity(Rarity.RARE), blockItem), IJetpack, ICapabilityProvider {
    private val capability = LazyOptional.of<IJetpack> { this }

    override fun hoverSpeed(context: Context): Double {
        return Configs.SERVER.hoverSpeed
    }

    override fun verticalSpeed(context: Context): Double {
        return Configs.SERVER.verticalSpeed
    }

    override fun activeType(context: Context): ControlType {
        return ControlType.TOGGLE
    }

    override fun hoverType(context: Context): ControlType {
        return ControlType.TOGGLE
    }

    override fun horizontalSpeed(context: Context): Double {
        return Configs.SERVER.horizontalSpeed
    }

    override fun acceleration(context: Context): Double {
        return Configs.SERVER.acceleration
    }

    override fun swimModifier(context: Context): Double {
        return Configs.SERVER.swimModifier
    }

    override fun boostsElytra(): Boolean {
        return Configs.SERVER.elytraBoostEnabled
    }

    private val thrusters = listOf(-0.35, 0.35).map { offset ->
        Vec3(offset, 0.7, -0.5)
    }

    override fun getThrusters(context: Context) = thrusters

    override fun onUse(context: Context) {
        if (!isThrusting(context)) return
        BackTankUtil.canAbsorbDamage(context.entity, usesPerTank(context))
    }

    private fun usesPerTank(context: Context): Int {
        return if (isHovering(context)) Configs.SERVER.usesPerTankHover
        else Configs.SERVER.usesPerTank
    }

    override fun isValid(context: Context): Boolean {
        return when (val source = context.source) {
            is EquipmentSource -> source.slot == EquipmentSlot.CHEST
            is CuriosSource -> source.slot == "back"
            else -> false
        }
    }

    override fun isUsable(context: Context): Boolean {
        val tank = BackTankUtil.get(context.entity)
        if (tank.isEmpty) return false
        val air = BackTankUtil.getAir(tank)
        if (air <= 0F) return false
        val cost = BackTankUtil.maxAirWithoutEnchants().toFloat() / usesPerTank(context)
        return air >= cost
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == JETPACK_CAPABILITY) return capability.cast()
        return LazyOptional.empty()
    }

    override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean {
        return super.canApplyAtEnchantingTable(stack, enchantment) && Configs.SERVER.isAllowed(enchantment)
    }

    override fun createParticles(): ParticleOptions {
        return AirParticleData(0F, 0.01F)
    }
}
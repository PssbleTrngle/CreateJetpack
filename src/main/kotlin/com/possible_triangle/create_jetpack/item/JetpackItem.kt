package com.possible_triangle.create_jetpack.item

import com.possible_triangle.create_jetpack.config.Configs
import com.possible_triangle.flightlib.api.ControlType
import com.possible_triangle.flightlib.api.IJetpack
import com.possible_triangle.flightlib.api.IJetpack.Context
import com.possible_triangle.flightlib.api.sources.CuriosSource
import com.possible_triangle.flightlib.api.sources.EquipmentSource
import com.simibubi.create.content.equipment.armor.BacktankItem
import com.simibubi.create.content.equipment.armor.BacktankUtil
import com.simibubi.create.foundation.item.LayeredArmorItem
import com.simibubi.create.foundation.particle.AirParticleData
import com.tterrag.registrate.util.entry.ItemEntry
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.phys.Vec3
import java.util.*

open class JetpackItem(
    properties: Properties,
    material: Holder<ArmorMaterial>,
    texture: ResourceLocation,
    blockItem: ItemEntry<BacktankBlockItem>,
) :
    BacktankItem(
        material,
        properties,
        texture,
        blockItem
    ), IJetpack {

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

    override fun elytraBoost(): Double {
        return Configs.SERVER.elytraBoost
    }

    private val thrusters = listOf(-0.35, 0.35).map { offset ->
        Vec3(offset, 0.7, -0.5)
    }

    override fun getThrusters(context: Context) = thrusters

    override fun onUse(context: Context) {
        if (context.world.gameTime % 20 != 0L) return
        BacktankUtil.canAbsorbDamage(context.entity, secondsPerTank(context))
    }

    private fun secondsPerTank(context: Context): Int {
        return if (isHovering(context)) Configs.SERVER.secondsPerTankHover
        else Configs.SERVER.secondsPerTank
    }

    override fun isValid(context: Context): Boolean {
        return when (val source = context.source) {
            is EquipmentSource -> source.slot == EquipmentSlot.CHEST
            is CuriosSource -> true
            else -> false
        }
    }

    override fun isUsable(context: Context): Boolean {
        val tank = BacktankUtil.getAllWithAir(context.entity).firstOrNull() ?: return false
        val air = BacktankUtil.getAir(tank)
        val cost = BacktankUtil.maxAirWithoutEnchants().toFloat() / secondsPerTank(context)
        return air >= cost
    }

    override fun supportsEnchantment(stack: ItemStack, enchantment: Holder<Enchantment>): Boolean {
        return super.supportsEnchantment(stack, enchantment) && Configs.SERVER.isAllowed(enchantment)
    }

    override fun createParticles(): ParticleOptions {
        return if (Configs.CLIENT.spawnSnowParticles)
            ParticleTypes.SNOWFLAKE
        else
            AirParticleData(0F, 0.01F)
    }

    class Layered(
        properties: Properties,
        material: Holder<ArmorMaterial>,
        texture: ResourceLocation,
        blockItem: ItemEntry<BacktankBlockItem>
    ) : JetpackItem(properties, material, texture, blockItem), LayeredArmorItem {

        override fun getArmorTextureLocation(
            entity: LivingEntity?,
            slot: EquipmentSlot?,
            stack: ItemStack?,
            layer: Int,
        ): String {
            return String.format(
                Locale.ROOT, "%s:textures/models/armor/%s_layer_%d.png",
                textureLoc.namespace, textureLoc.path, layer
            )
        }
    }
}

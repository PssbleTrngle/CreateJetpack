package com.possible_triangle.create_jetpack.config

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.enchantment.Enchantment
import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.collections.mapNotNull

interface IServerConfig {
    val secondsPerTank: Int
    val secondsPerTankHover: Int
    val horizontalSpeed: Double
    val verticalSpeed: Double
    val acceleration: Double
    val hoverSpeed: Double
    val swimModifier: Double
    val elytraBoost: Double
    val heightAboveGroundLimit: Int?
    val enchantments: EnchantmentConfig
}

data class SyncedConfig(
    override val secondsPerTank: Int,
    override val secondsPerTankHover: Int,
    override val horizontalSpeed: Double,
    override val verticalSpeed: Double,
    override val acceleration: Double,
    override val hoverSpeed: Double,
    override val swimModifier: Double,
    override val elytraBoost: Double,
    override val heightAboveGroundLimit: Int?,
    override val enchantments: EnchantmentConfig,
) : IServerConfig

class ServerConfig(
    builder: ModConfigSpec.Builder,
) : IServerConfig {
    private val secondsPerTankValue = builder.defineInRange("air.seconds_per_tank", 450, 1, Integer.MAX_VALUE)
    override val secondsPerTank get() = secondsPerTankValue.get()

    private val secondsPerTankHoverValue =
        builder.defineInRange("air.seconds_per_tank_hover", 900, 1, Integer.MAX_VALUE)
    override val secondsPerTankHover get() = secondsPerTankHoverValue.get()

    private val horizontalSpeedValue = builder.defineInRange("speed.horizontal", 0.02, 0.01, 100.0)
    override val horizontalSpeed get() = horizontalSpeedValue.get()

    private val verticalSpeedValue = builder.defineInRange("speed.vertical", 0.4, 0.01, 100.0)
    override val verticalSpeed get() = verticalSpeedValue.get()

    private val accelerationValue = builder.defineInRange("speed.acceleration", 0.6, 0.01, 100.0)
    override val acceleration get() = accelerationValue.get()

    private val hoverSpeedValue = builder.defineInRange("speed.hover_descend", -0.03, -100.0, 0.0)
    override val hoverSpeed get() = hoverSpeedValue.get()

    private val swimModifierValue = builder.defineInRange("speed.swim_modifier", 1.8, 0.0, 100.0)
    override val swimModifier get() = swimModifierValue.get()

    private val elytraBoostValue = builder.defineInRange("features.elytra_boost", 1.25, 1.0, 100.0)
    override val elytraBoost get() = elytraBoostValue.get()

    private val enchantmentsList = builder.defineList("enchantments.list", emptyList<String>()) { true }
    private val enchantmentsIsBlacklist = builder.define("enchantments.is_blacklist", true)

    private val heightAboveGroundLimitValue = builder.define("heightAboveGroundLimit", -1)

    override val heightAboveGroundLimit: Int?
        get() = heightAboveGroundLimitValue.get().takeUnless { it < 0 }

    override val enchantments: EnchantmentConfig
        get() =
            EnchantmentConfig(
                ids = enchantmentsList.get(),
                isBlacklist = enchantmentsIsBlacklist.get(),
            )
}

data class EnchantmentConfig(
    val ids: List<String>,
    val isBlacklist: Boolean,
) {
    fun isAllowed(enchantment: Holder<Enchantment>): Boolean {
        val key = enchantment.key?.location() ?: return false
        val contained = ids.mapNotNull(ResourceLocation::tryParse).contains(key)
        return contained != isBlacklist
    }
}

package com.possible_triangle.create_jetpack.config

import net.minecraftforge.common.ForgeConfigSpec
import java.util.*

class ClientConfig(builder: ForgeConfigSpec.Builder) {

    companion object {
        private val calendar = Calendar.getInstance()
        private val isChristmas = (calendar.get(2) + 1 == 12) && (calendar.get(5) <= 26)
    }

    val SHOW_OVERLAY = builder.define("overlay.enabled", true)

    init {
        builder.comment("Use negative values to position relative to the right/bottom of the screen")
    }

    val OVERLAY_DISTANCE_X = builder.defineInRange("overlay.position.x", 6, Int.MIN_VALUE, Int.MAX_VALUE)
    val OVERLAY_DISTANCE_Y = builder.defineInRange("overlay.position.y", 6, Int.MIN_VALUE, Int.MAX_VALUE)
    val OVERLAY_DISTANCE_SCALE = builder.defineInRange("overlay.scale", 1.0, 0.0, Double.MAX_VALUE)

    private val SEASONAL_EFFECTS = builder.define("effects.seasonal", true)

    val spawnSnowParticles
        get() = isChristmas && SEASONAL_EFFECTS.get()

}
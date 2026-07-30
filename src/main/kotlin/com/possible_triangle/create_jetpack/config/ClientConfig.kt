package com.possible_triangle.create_jetpack.config

import net.neoforged.neoforge.common.ModConfigSpec
import java.util.*

class ClientConfig(
    builder: ModConfigSpec.Builder,
) {
    companion object {
        private val calendar = Calendar.getInstance()
        private val isChristmas = (calendar.get(2) + 1 == 12) && (calendar.get(5) <= 26)
    }

    val showOverlay = builder.define("overlay.enabled", true)

    init {
        builder.comment("Use negative values to position relative to the right/bottom of the screen")
    }

    val overlayX = builder.defineInRange("overlay.position.x", 6, Int.MIN_VALUE, Int.MAX_VALUE)
    val overlayY = builder.defineInRange("overlay.position.y", 6, Int.MIN_VALUE, Int.MAX_VALUE)
    val overlayScale = builder.defineInRange("overlay.scale", 1.0, 0.0, Double.MAX_VALUE)

    private val seasonalEffects = builder.define("effects.seasonal", true)

    val spawnSnowParticles
        get() = isChristmas && seasonalEffects.get()
}

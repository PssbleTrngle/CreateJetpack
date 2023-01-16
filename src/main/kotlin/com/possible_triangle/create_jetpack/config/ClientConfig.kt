package com.possible_triangle.create_jetpack.config

import net.minecraftforge.common.ForgeConfigSpec

class ClientConfig(builder: ForgeConfigSpec.Builder) {

    enum class OverlayPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    val SHOW_OVERLAY = builder.define("overlay.enabled", true)

    init {
        builder.comment("Use negative values to position relative to the right/bottom of the screen")
    }

    val OVERLAY_DISTANCE_X = builder.defineInRange("overlay.position.x", 6, Int.MIN_VALUE, Int.MAX_VALUE)
    val OVERLAY_DISTANCE_Y = builder.defineInRange("overlay.position.y", 6, Int.MIN_VALUE, Int.MAX_VALUE)
    val OVERLAY_DISTANCE_SCALE = builder.defineInRange("overlay.scale", 1.0, 0.0, Double.MAX_VALUE)

}
package com.possible_triangle.create_jetpack.config

import net.minecraftforge.common.ForgeConfigSpec

class ServerConfig(builder: ForgeConfigSpec.Builder) {

    val USES_PER_TANK = builder.defineInRange("air.uses_per_tank", 2048, 1, Integer.MAX_VALUE)
    val USES_PER_TANK_HOVER = builder.defineInRange("air.uses_per_tank_hover", 2048 * 10, 1, Integer.MAX_VALUE)

    val HORIZONTAL_SPEED = builder.defineInRange("speed.horizontal", 0.02, 0.01, 100.0)
    val VERTICAL_SPEED = builder.defineInRange("speed.vertical", 0.4, 0.01, 100.0)
    val ACCELERATION = builder.defineInRange("speed.acceleration", 0.6, 0.01, 100.0)
    val HOVER_SPEED = builder.defineInRange("speed.hover_descend", -0.03, -100.0, 0.0)
    val SWIM_MODIFIER = builder.defineInRange("speed.swim_modifier", 1.8, 0.0, 100.0)

}
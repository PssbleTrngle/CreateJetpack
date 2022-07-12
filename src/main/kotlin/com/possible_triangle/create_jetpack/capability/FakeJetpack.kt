package com.possible_triangle.create_jetpack.capability

import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType

object FakeJetpack : IJetpack {

    override fun verticalSpeed(context: Context): Double {
        return 0.0
    }

    override fun hoverSpeed(context: Context): Double {
        return 0.0
    }

    override fun isUsable(context: Context): Boolean {
        return false
    }

    override fun hoverType(context: Context): ControlType {
        return ControlType.NEVER
    }

    override fun horizontalSpeed(context: Context): Double {
        return 0.0
    }

    override fun acceleration(context: Context): Double {
        return 0.0
    }

}
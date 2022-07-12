package com.possible_triangle.create_jetpack.network

import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.network.NetworkRegistry

object ModNetwork {

    private val VERSION = "1.0"
    val CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation(MOD_ID, "network"),
        { VERSION },
        VERSION::equals,
        VERSION::equals,
    )

    fun init() {
        CHANNEL.registerMessage(0, KeyEvent::class.java, KeyEvent::encode, KeyEvent::decode, KeyEvent::handle)
    }

}
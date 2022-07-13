package com.possible_triangle.create_jetpack.network

import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkRegistry

object ModNetwork {

    private val VERSION = "1.0"
    val CHANNEL = NetworkRegistry.ChannelBuilder.named(ResourceLocation(MOD_ID, "network"))
        .networkProtocolVersion { VERSION }
        .clientAcceptedVersions(VERSION::equals)
        .serverAcceptedVersions(VERSION::equals)
        .simpleChannel()

    fun init() {
        CHANNEL.registerMessage(0, KeyEvent::class.java, KeyEvent::encode, KeyEvent::decode, KeyEvent::handle)
    }

}
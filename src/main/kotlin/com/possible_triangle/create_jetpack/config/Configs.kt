package com.possible_triangle.create_jetpack.config

import com.possible_triangle.create_jetpack.CreateJetpackMod
import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor

object Configs {

    var SERVER_SPEC: ForgeConfigSpec
        private set

    private var LOCAL_SERVER: ServerConfig
    internal var SYNCED_SERVER: IServerConfig? = null

    val SERVER: IServerConfig
        get() = SYNCED_SERVER ?: LOCAL_SERVER

    var CLIENT_SPEC: ForgeConfigSpec
        private set
    var CLIENT: ClientConfig
        private set

    init {
        with(ForgeConfigSpec.Builder().configure { ServerConfig(it) }) {
            LOCAL_SERVER = left
            SERVER_SPEC = right
        }

        with(ForgeConfigSpec.Builder().configure { ClientConfig(it) }) {
            CLIENT = left
            CLIENT_SPEC = right
        }
    }

    fun syncConfig(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.player
        if (player !is ServerPlayer) return
        CreateJetpackMod.LOGGER.debug("Sending server config to ${player.scoreboardName}")
        Network.CHANNEL.send(PacketDistributor.PLAYER.with { player }, SyncConfigMessage(LOCAL_SERVER))
    }

    object Network {
        private const val version = "1.0"
        internal val CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation(MOD_ID, "configs"),
            { version },
            version::equals,
            version::equals
        )

        fun register() {
            CHANNEL.registerMessage(
                0,
                SyncConfigMessage::class.java,
                SyncConfigMessage::encode,
                SyncConfigMessage::decode,
                SyncConfigMessage::handle
            )
        }
    }

}
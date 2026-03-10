package com.possible_triangle.create_jetpack.config

import com.possible_triangle.create_jetpack.CreateJetpackMod
import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.common.ForgeConfigSpec

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

    fun syncConfig(player: ServerPlayer) {
        CreateJetpackMod.LOGGER.debug("Sending server config to ${player.scoreboardName}")
        val message = SyncConfigMessage(LOCAL_SERVER)
        Network.send(message, player)
    }

    object Network {

        private val PACKET_ID = ResourceLocation(MOD_ID, "config_sync")

        fun send(message: SyncConfigMessage, player: ServerPlayer) {
            val buffer = PacketByteBufs.create()
            message.encode(buffer)
            ServerPlayNetworking.send(player, PACKET_ID, buffer)
        }

        fun registerReceiver() {
            ClientPlayNetworking.registerGlobalReceiver(PACKET_ID) { client, _, buffer, _ ->
                val event = SyncConfigMessage.decode(buffer)
                client.execute {
                    event.handle()
                }
            }
        }
    }

}
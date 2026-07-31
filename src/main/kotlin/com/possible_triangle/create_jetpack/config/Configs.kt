package com.possible_triangle.create_jetpack.config

import com.possible_triangle.create_jetpack.Constants
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.common.ModConfigSpec
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object Configs {
    @Suppress("ktlint:standard:property-naming")
    var SERVER_SPEC: ModConfigSpec
        private set

    @Suppress("ktlint:standard:property-naming")
    private var LOCAL_SERVER: ServerConfig

    @Suppress("ktlint:standard:property-naming")
    internal var SYNCED_SERVER: IServerConfig? = null

    val SERVER: IServerConfig
        get() = SYNCED_SERVER ?: LOCAL_SERVER

    @Suppress("ktlint:standard:property-naming")
    var CLIENT_SPEC: ModConfigSpec
        private set

    @Suppress("ktlint:standard:property-naming")
    var CLIENT: ClientConfig
        private set

    init {
        with(ModConfigSpec.Builder().configure { ServerConfig(it) }) {
            LOCAL_SERVER = left
            SERVER_SPEC = right
        }

        with(ModConfigSpec.Builder().configure { ClientConfig(it) }) {
            CLIENT = left
            CLIENT_SPEC = right
        }
    }

    fun syncConfig() {
        Constants.LOGGER.debug("Sending server config all players")
        PacketDistributor.sendToAllPlayers(SyncConfigMessage(LOCAL_SERVER))
    }

    fun syncConfig(player: ServerPlayer) {
        Constants.LOGGER.debug("Sending server config to ${player.scoreboardName}")
        PacketDistributor.sendToPlayer(player, SyncConfigMessage(LOCAL_SERVER))
    }

    object Network {
        fun register(event: RegisterPayloadHandlersEvent) {
            val registrar = event.registrar("2.0")

            registrar.playToClient(SyncConfigMessage.TYPE.type(), SyncConfigMessage.TYPE.codec()) { message, _ ->
                message.handle()
            }
        }
    }
}

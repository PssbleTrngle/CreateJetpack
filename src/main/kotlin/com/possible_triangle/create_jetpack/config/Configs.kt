package com.possible_triangle.create_jetpack.config

import com.possible_triangle.create_jetpack.CJConstants
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS

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

    private fun syncConfig() {
        CJConstants.LOGGER.debug("Sending server config all players")
        PacketDistributor.sendToAllPlayers(SyncConfigMessage(LOCAL_SERVER))
    }

    private fun syncConfig(player: ServerPlayer) {
        CJConstants.LOGGER.debug("Sending server config to ${player.scoreboardName}")
        PacketDistributor.sendToPlayer(player, SyncConfigMessage(LOCAL_SERVER))
    }

    @JvmStatic
    fun register(
        container: ModContainer,
        modBus: IEventBus,
    ) {
        container.registerConfig(ModConfig.Type.COMMON, SERVER_SPEC)
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC)

        modBus.addListener(Configs.Network::register)

        FORGE_BUS.addListener { event: PlayerEvent.PlayerLoggedInEvent ->
            val player = event.entity
            if (player is ServerPlayer) syncConfig(player)
        }

        modBus.addListener { event: ModConfigEvent.Reloading ->
            if (event.config.type == ModConfig.Type.COMMON) {
                syncConfig()
            }
        }
    }

    object Network {
        internal fun register(event: RegisterPayloadHandlersEvent) {
            val registrar = event.registrar("2.0")

            registrar.playToClient(SyncConfigMessage.TYPE.type(), SyncConfigMessage.TYPE.codec()) { message, _ ->
                message.handle()
            }
        }
    }
}

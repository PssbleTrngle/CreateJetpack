package com.possible_triangle.create_jetpack.config

import com.possible_triangle.create_jetpack.CreateJetpackMod
import com.possible_triangle.flightlib.api.Constants
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class SyncConfigMessage(private val config: IServerConfig) : CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<SyncConfigMessage> = TYPE.type()

    companion object {
        val TYPE = CustomPacketPayload.TypeAndCodec(
            CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_config")),
            StreamCodec.of(SyncConfigMessage::encode, SyncConfigMessage::decode),
        )

        private fun decode(buf: FriendlyByteBuf): SyncConfigMessage {
            val config = SyncedConfig(
                secondsPerTank = buf.readInt(),
                secondsPerTankHover = buf.readInt(),
                horizontalSpeed = buf.readDouble(),
                verticalSpeed = buf.readDouble(),
                acceleration = buf.readDouble(),
                hoverSpeed = buf.readDouble(),
                swimModifier = buf.readDouble(),
                elytraBoost = buf.readDouble(),
            )
            return SyncConfigMessage(config)
        }

        private fun encode(buf: FriendlyByteBuf, message: SyncConfigMessage) = with(message) {
            buf.writeInt(config.secondsPerTank)
            buf.writeInt(config.secondsPerTankHover)
            buf.writeDouble(config.horizontalSpeed)
            buf.writeDouble(config.verticalSpeed)
            buf.writeDouble(config.acceleration)
            buf.writeDouble(config.hoverSpeed)
            buf.writeDouble(config.swimModifier)
            buf.writeDouble(config.elytraBoost)
        }
    }

    fun handle() {
        CreateJetpackMod.LOGGER.debug("Hover speed: ${config.hoverSpeed}")
        Configs.SYNCED_SERVER = config
    }

}

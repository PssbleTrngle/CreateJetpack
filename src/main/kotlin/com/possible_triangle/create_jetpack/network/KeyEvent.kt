package com.possible_triangle.create_jetpack.network

import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class KeyEvent(val key: Key, val pressed: Boolean, val notify: Boolean = false) {

    companion object {

        fun handle(event: KeyEvent, supplier: Supplier<NetworkEvent.Context>) {
            val context = supplier.get()

            val player = context.sender
            if (context.direction == NetworkDirection.PLAY_TO_SERVER && player != null) {
                context.enqueueWork {
                    if (event.notify) player.sendSystemMessage(
                        Component.translatable(
                            "message.$MOD_ID.control.${event.key.name.lowercase()}",
                            Component.translatable("message.$MOD_ID.control.${if (event.pressed) "on" else "off"}")
                        ), true,
                    )
                    ControlManager.handle(player, event)
                }
            }

            context.packetHandled = true
        }

        fun encode(event: KeyEvent, packet: FriendlyByteBuf) {
            packet.writeInt(event.key.ordinal)
            packet.writeBoolean(event.pressed)
            packet.writeBoolean(event.notify)
        }

        fun decode(packet: FriendlyByteBuf): KeyEvent {
            val key = Key.values()[packet.readInt()]
            return KeyEvent(key, packet.readBoolean(), packet.readBoolean())
        }

    }

}
package com.possible_triangle.create_jetpack.network

import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.network.PacketBuffer
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class KeyEvent(val key: Key, val pressed: Boolean, val notify: Boolean = false) {

    companion object {

        fun handle(event: KeyEvent, supplier: Supplier<NetworkEvent.Context>) {
            val context = supplier.get()

            val player = context.sender
            if (context.direction == NetworkDirection.PLAY_TO_SERVER && player != null) {
                context.enqueueWork {
                    if (event.notify) player.sendStatusMessage(
                        TranslationTextComponent(
                            "message.$MOD_ID.control.${event.key.name.toLowerCase()}",
                            TranslationTextComponent("message.$MOD_ID.control.${if (event.pressed) "on" else "off"}")
                        ), true
                    )
                    ControlManager.handle(player, event)
                }
            }

            context.packetHandled = true
        }

        fun encode(event: KeyEvent, packet: PacketBuffer) {
            packet.writeInt(event.key.ordinal)
            packet.writeBoolean(event.pressed)
            packet.writeBoolean(event.notify)
        }

        fun decode(packet: PacketBuffer): KeyEvent {
            val key = Key.values()[packet.readInt()]
            return KeyEvent(key, packet.readBoolean(), packet.readBoolean())
        }

    }

}
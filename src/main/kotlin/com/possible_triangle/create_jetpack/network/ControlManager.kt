package com.possible_triangle.create_jetpack.network

import com.mojang.blaze3d.platform.InputConstants
import com.possible_triangle.create_jetpack.capability.JetpackLogic
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.player.Player
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.ClientRegistry
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.minecraftforge.fml.common.Mod
import org.lwjgl.glfw.GLFW
import java.util.*

@Mod.EventBusSubscriber
object ControlManager {

    enum class Key(val toggle: Boolean, val defaultKey: Int? = null, val default: Boolean = false) {
        UP(false),
        LEFT(false),
        RIGHT(false),
        FORWARD(false),
        BACKWARD(false),
        TOGGLE_ACTIVE(true, default = true, defaultKey = GLFW.GLFW_KEY_G),
        TOGGLE_HOVER(true, defaultKey = GLFW.GLFW_KEY_H);

        @OnlyIn(Dist.CLIENT)
        lateinit var binding: Optional<KeyMapping>

    }

    private val KEYS = mutableMapOf<Player, MutableMap<Key, Boolean>>()

    private fun setKey(player: Player, key: Key, pressed: Boolean) {
        val keys = KEYS.getOrPut(player) { mutableMapOf() }
        keys[key] = pressed
    }

    fun isPressed(player: Player, key: Key): Boolean {
        return KEYS[player]?.get(key) ?: key.default
    }

    @OnlyIn(Dist.CLIENT)
    fun registerKeybinds() {
        Key.values().forEach { key ->
            key.binding = Optional.ofNullable(key.defaultKey).map {
                KeyMapping(
                    "key.jetpack.${key.name.lowercase()}.description",
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    it,
                    "jetpack"
                )
            }
            key.binding.ifPresent {
                ClientRegistry.registerKeyBinding(it)
            }
        }
    }

    internal fun handle(player: Player, event: KeyEvent) {
        setKey(player, event.key, event.pressed)
    }

    private fun reset(player: Player) {
        KEYS[player]?.apply {
            Key.values()
                .filterNot { it.toggle }
                .forEach { remove(it) }
        }
    }

    fun onDimensionChange(event: PlayerEvent.PlayerChangedDimensionEvent) {
        reset(event.player)
    }

    fun onLogout(event: PlayerLoggedOutEvent) {
        reset(event.player)
    }

    @OnlyIn(Dist.CLIENT)
    internal fun sync(event: KeyEvent) {
        val player = Minecraft.getInstance().player ?: return
        ModNetwork.CHANNEL.sendToServer(event)
        handle(player, event)
    }

    fun onTick(event: TickEvent.PlayerTickEvent) {
        val player = event.player
        if (player !is LocalPlayer) return

        Key.values().filter { !it.toggle && it.binding.isPresent }.forEach {
            sync(KeyEvent(it, it.binding.get().isDown))
        }

        sync(KeyEvent(Key.UP, player.input.jumping))
        sync(KeyEvent(Key.LEFT, player.input.left))
        sync(KeyEvent(Key.RIGHT, player.input.right))
        sync(KeyEvent(Key.FORWARD, player.input.forwardImpulse > 0))
        sync(KeyEvent(Key.BACKWARD, player.input.forwardImpulse < 0))

    }

    @Suppress("UNUSED_PARAMETER")
    fun onKey(event: InputEvent.KeyInputEvent) {
        val player = Minecraft.getInstance().player ?: return
        JetpackLogic.getJetpack(player) ?: return

        val key = Key.values()
            .filter { it.toggle }
            .firstOrNull { it.binding.get().isDown } ?: return

        sync(KeyEvent(key, !isPressed(player, key), true))

    }

}
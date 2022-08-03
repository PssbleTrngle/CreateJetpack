package com.possible_triangle.create_jetpack.network

import com.mojang.blaze3d.platform.InputConstants
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

@Mod.EventBusSubscriber
object ControlManager {

    enum class Key(val toggle: Boolean, defaultKey: Int? = null, val default: Boolean = false) {
        UP(false),
        LEFT(false),
        RIGHT(false),
        FORWARD(false),
        BACKWARD(false),
        TOGGLE_ACTIVE(true, default = true, defaultKey = GLFW.GLFW_KEY_G),
        TOGGLE_HOVER(true, defaultKey = GLFW.GLFW_KEY_H);

        @OnlyIn(Dist.CLIENT)
        val binding: KeyMapping? = if (defaultKey != null) KeyMapping(
            "key.jetpack.${name.lowercase()}.description",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            defaultKey,
            "jetpack"
        ) else null

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
        Key.values().filter { it.binding != null }.forEach {
            ClientRegistry.registerKeyBinding(it.binding)
        }
    }

    internal fun handle(player: Player, event: KeyEvent) {
        setKey(player, event.key, event.pressed)
    }

    fun onDimensionChange(event: PlayerEvent.PlayerChangedDimensionEvent) {
        KEYS.remove(event.player)
    }

    fun onLogout(event: PlayerLoggedOutEvent) {
        KEYS.remove(event.player)
    }

    @OnlyIn(Dist.CLIENT)
    internal fun sync(event: KeyEvent) {
        val player = Minecraft.getInstance().player ?: return
        ModNetwork.CHANNEL.sendToServer(event)
        handle(player, event)
    }

    fun onTick(event: TickEvent.PlayerTickEvent) {
        if (!event.player.level.isClientSide) return
        val player = event.player as LocalPlayer

        Key.values().filter { !it.toggle && it.binding != null }.forEach {
            sync(KeyEvent(it, it.binding!!.isDown))
        }

        sync(KeyEvent(Key.UP, player.input.jumping))
        sync(KeyEvent(Key.LEFT, player.input.left))
        sync(KeyEvent(Key.RIGHT, player.input.right))
        sync(KeyEvent(Key.FORWARD, player.input.forwardImpulse > 0))
        sync(KeyEvent(Key.BACKWARD, player.input.forwardImpulse < 0))

    }

    fun onKey(event: InputEvent.KeyInputEvent) {
        val player = Minecraft.getInstance().player ?: return

        Key.values().filter { it.toggle && it.binding?.isDown == true }.forEach {
            sync(KeyEvent(it, !isPressed(player, it), true))
        }

    }

}
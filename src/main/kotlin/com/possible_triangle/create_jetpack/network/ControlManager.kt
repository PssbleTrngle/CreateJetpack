package com.possible_triangle.create_jetpack.network

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.client.settings.KeyBinding
import net.minecraft.client.util.InputMappings
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber
object ControlManager {

    enum class Key(val toggle: Boolean, createKeybind: Boolean = true) {
        UP(false, false),
        LEFT(false, false),
        RIGHT(false, false),
        FORWARD(false, false),
        BACKWARD(false, false),
        TOGGLE_ACTIVE(true),
        TOGGLE_HOVER(true);

        @OnlyIn(Dist.CLIENT)
        val binding: KeyBinding? = if (createKeybind) KeyBinding(
            "key.jetpack.${name.toLowerCase()}.description",
            KeyConflictContext.IN_GAME,
            InputMappings.Type.KEYSYM,
            12 + ordinal,
            "jetpack"
        ) else null

    }

    private val KEYS = mutableMapOf<PlayerEntity, MutableMap<Key, Boolean>>()

    private fun setKey(player: PlayerEntity, key: Key, pressed: Boolean) {
        val keys = KEYS.getOrPut(player) { mutableMapOf() }
        keys[key] = pressed
    }

    fun isPressed(player: PlayerEntity, key: Key): Boolean {
        return KEYS[player]?.get(key) ?: false
    }

    @OnlyIn(Dist.CLIENT)
    fun registerKeybinds() {
        Key.values().filter { it.binding != null }.forEach {
            ClientRegistry.registerKeyBinding(it.binding)
        }
    }

    internal fun handle(player: PlayerEntity, event: KeyEvent) {
        setKey(player, event.key, event.pressed)
    }

    @SubscribeEvent
    fun onDimensionChange(event: PlayerEvent.PlayerChangedDimensionEvent) {
        KEYS.remove(event.player)
    }

    @SubscribeEvent
    fun onLogout(event: PlayerLoggedOutEvent) {
        KEYS.remove(event.player)
    }

    @OnlyIn(Dist.CLIENT)
    internal fun sync(event: KeyEvent) {
        val player = Minecraft.getInstance().player ?: return
        ModNetwork.CHANNEL.sendToServer(event)
        handle(player, event)
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onTick(event: TickEvent.PlayerTickEvent) {
        if(!event.player.world.isRemote) return
        val player = event.player as ClientPlayerEntity

        Key.values().filter { !it.toggle && it.binding != null }.forEach {
            sync(KeyEvent(it, it.binding!!.isKeyDown))
        }

        sync(KeyEvent(Key.UP, player.movementInput.jump))
        sync(KeyEvent(Key.LEFT, player.movementInput.leftKeyDown))
        sync(KeyEvent(Key.RIGHT, player.movementInput.rightKeyDown))
        sync(KeyEvent(Key.FORWARD, player.movementInput.forwardKeyDown))
        sync(KeyEvent(Key.BACKWARD, player.movementInput.backKeyDown))

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onKey(event: InputEvent.KeyInputEvent) {
        val player = Minecraft.getInstance().player ?: return

        Key.values().filter { it.toggle && it.binding?.isPressed == true }.forEach {
            sync(KeyEvent(it, !isPressed(player, it), true))
        }

    }

}
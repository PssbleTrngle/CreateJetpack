package com.possible_triangle.create_jetpack.client

import com.jozufozu.flywheel.repack.joml.Vector2f
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.JetpackLogic
import com.possible_triangle.create_jetpack.config.Configs
import com.possible_triangle.create_jetpack.item.BronzeJetpack
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.gui.ForgeIngameGui
import net.minecraftforge.client.gui.IIngameOverlay
import net.minecraftforge.client.gui.OverlayRegistry


object ControlsDisplay : IIngameOverlay {

    private val texture = ResourceLocation(MOD_ID, "textures/gui/controls.png")
    private const val textureSize = 32

    private fun spritePos(index: Int): Vector2f {
        val spritePerRow = textureSize / 16
        return Vector2f(
            index % spritePerRow * 16F,
            index / spritePerRow * 16F,
        )
    }

    private val ICONS = mapOf<Key, (IJetpack.Context) -> BronzeJetpack.ControlType>(
        Key.TOGGLE_ACTIVE to { it.jetpack.activeType(it) },
        Key.TOGGLE_HOVER to { it.jetpack.hoverType(it) },
    )

    fun register() {
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Jetpack Controls", this)
    }

    override fun render(gui: ForgeIngameGui, poseStack: PoseStack, partialTick: Float, width: Int, height: Int) {
        val mc = Minecraft.getInstance()
        if(!Configs.CLIENT.SHOW_OVERLAY.get()) return
        if (mc.options.hideGui) return
        val player = mc.player ?: return
        val context = JetpackLogic.getJetpack(player) ?: return

        val padding = 4
        val margin = 6
        val scale = 1.0F
        val spriteWidth = 16 + margin

        fun renderSprite(index: Int, x: Int, y: Int) {
            val sprite = spritePos(index)
            RenderSystem.setShaderTexture(0, texture)
            poseStack.scale(scale, scale, scale)
            GuiComponent.blit(poseStack, x, y, 0, sprite.x, sprite.y, 16, 16, 32, 32)
        }

        val engineActive = ControlManager.isPressed(player, Key.TOGGLE_ACTIVE)

        ICONS.filterKeys { it == Key.TOGGLE_ACTIVE || engineActive }
            .filterValues { getType -> getType(context) == BronzeJetpack.ControlType.TOGGLE }
            .keys.forEachIndexed { index, key ->
                poseStack.pushPose()

                val active = ControlManager.isPressed(player, key)
                renderSprite(index + if (active) 0 else 2, padding + spriteWidth * index, padding)

                val textScale = 0.5F
                poseStack.scale(textScale, textScale, textScale)
                val textMargin = (padding + 8 + spriteWidth * index) * (1 / textScale)
                val text = TranslatableComponent("overlay.create_jetpack.control.${key.name.lowercase()}")
                val color = if (active) 0xFFFFFF else 0xBBBBBB
                GuiComponent.drawCenteredString(
                    poseStack, gui.font, text, textMargin.toInt(), (22 / textScale).toInt(), color
                )
                poseStack.popPose()

            }
    }
}
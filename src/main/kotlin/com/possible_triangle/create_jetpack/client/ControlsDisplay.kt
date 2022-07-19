package com.possible_triangle.create_jetpack.client

import com.jozufozu.flywheel.repack.joml.Vector2f
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.JetpackLogic
import com.possible_triangle.create_jetpack.item.Jetpack
import com.possible_triangle.create_jetpack.network.ControlManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.gui.ForgeIngameGui
import net.minecraftforge.client.gui.IIngameOverlay
import net.minecraftforge.client.gui.OverlayRegistry


object ControlsDisplay : IIngameOverlay {

    private val texture = ResourceLocation(MOD_ID, "textures/gui/controls.png")
    const val textureSize = 32
    private fun spritePos(index: Int): Vector2f {
        val spritePerRow = textureSize / 16
        return Vector2f(
            index % spritePerRow * 16F,
            index / spritePerRow * 16F,
        )
    }

    private val ICONS = mapOf<ControlManager.Key, (IJetpack, IJetpack.Context) -> Jetpack.ControlType>(
        ControlManager.Key.TOGGLE_ACTIVE to { j, c -> j.activeType(c) },
        ControlManager.Key.TOGGLE_HOVER to { j, c -> j.hoverType(c) },
    )

    fun register() {
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "Jetpack Control's", this)
    }

    override fun render(gui: ForgeIngameGui, poseStack: PoseStack, partialTick: Float, width: Int, height: Int) {
        val mc = Minecraft.getInstance()
        if (mc.options.hideGui) return
        val player = mc.player ?: return
        val (context, jetpack) = JetpackLogic.getJetpack(player) ?: return

        val margin = 4

        fun renderSprite(index: Int, x: Int, y: Int) {
            val sprite = spritePos(index)
            RenderSystem.setShaderTexture(0, texture)
            GuiComponent.blit(poseStack, x, y, 0, sprite.x, sprite.y, 16, 16, 32, 32)
        }

        ICONS.onEachIndexed { index, (key, getType) ->
            if (getType(jetpack, context) == Jetpack.ControlType.TOGGLE) {
                val active = ControlManager.isPressed(player, key)
                renderSprite(index + if (active) 0 else 2, margin + 18 * index, margin)
            }
        }
    }
}
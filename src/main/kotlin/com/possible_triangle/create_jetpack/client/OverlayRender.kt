package com.possible_triangle.create_jetpack.client

import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Matrix4f
import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.capability.JetpackLogic
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
object OverlayRender {

    private val ICONS = mutableMapOf<Key, TextureAtlasSprite>()
    private val ICON_PROVIDERS = mapOf<Key, (IJetpack, Context) -> ControlType>(
        Key.TOGGLE_ACTIVE to { j, c -> j.activeType(c) },
        Key.TOGGLE_HOVER to { j, c -> j.hoverType(c) },
    )

    private fun texture(key: Key): ResourceLocation {
        return ResourceLocation(MOD_ID, "container/overlay/${key.name.toLowerCase()}")
    }

    @SubscribeEvent
    fun textureStitch(event: TextureStitchEvent.Pre) {
        if (event.atlas.location() != InventoryMenu.BLOCK_ATLAS) return
        ICON_PROVIDERS.forEach { (key) ->
            event.addSprite(texture(key))
        }
    }

    @SubscribeEvent
    fun textureStitch(event: TextureStitchEvent.Post) {
        if (event.atlas.location() != InventoryMenu.BLOCK_ATLAS) return
        ICON_PROVIDERS.forEach { (key) ->
            ICONS[key] = event.atlas.getSprite(texture(key))
        }
    }

    fun onRender(event: RenderGameOverlayEvent) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val (context, jetpack) = JetpackLogic.getJetpack(player) ?: return

        val matrix = event.matrixStack
        matrix.pushPose()

        mc.textureManager.bindForSetup(InventoryMenu.BLOCK_ATLAS)

        val size = 10F
        ICON_PROVIDERS.filter { it.value(jetpack, context) == ControlType.TOGGLE }.onEachIndexed { i, (key) ->
            val texture = ICONS[key] ?: return
            draw(matrix.last().pose(), 10F, 10F + (size + 1) * i, size, texture)
        }

        matrix.popPose()

    }

    private fun draw(
        matrix: Matrix4f,
        x: Float,
        y: Float,
        size: Float,
        texture: TextureAtlasSprite,
    ) {
        val buffer = Tesselator.getInstance().builder
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        buffer.vertex(matrix, x, 0F, y).uv(texture.u1, texture.v0).endVertex()
        buffer.vertex(matrix, x, 0F, y + size).uv(texture.u1, texture.v1).endVertex()
        buffer.vertex(matrix, x + size, 0F, y + size).uv(texture.u0, texture.v1).endVertex()
        buffer.vertex(matrix, x + size, 0F, y).uv(texture.u0, texture.v0).endVertex()
        buffer.end()
        BufferUploader.end(buffer)
    }

}
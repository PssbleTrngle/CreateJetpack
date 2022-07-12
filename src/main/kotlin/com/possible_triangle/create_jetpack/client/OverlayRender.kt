package com.possible_triangle.create_jetpack.client

import com.mojang.blaze3d.systems.RenderSystem
import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.IJetpack.Context
import com.possible_triangle.create_jetpack.capability.JetpackLogic
import com.possible_triangle.create_jetpack.item.Jetpack.ControlType
import com.possible_triangle.create_jetpack.network.ControlManager.Key
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldVertexBufferUploader
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Matrix4f
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
        if (event.map.id != PlayerContainer.BLOCK_ATLAS_TEXTURE) return
        ICON_PROVIDERS.forEach { (key) ->
            event.addSprite(texture(key))
        }
    }

    @SubscribeEvent
    fun textureStitch(event: TextureStitchEvent.Post) {
        if (event.map.id != PlayerContainer.BLOCK_ATLAS_TEXTURE) return
        ICON_PROVIDERS.forEach { (key) ->
            ICONS[key] = event.map.getSprite(texture(key))
        }
    }

    fun onRender(event: RenderGameOverlayEvent) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val (context, jetpack) = JetpackLogic.getJetpack(player) ?: return

        val matrix = event.matrixStack
        matrix.push()

        mc.textureManager.bindTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE)

        val size = 10F
        ICON_PROVIDERS.filter { it.value(jetpack, context) == ControlType.TOGGLE }.onEachIndexed { i, (key) ->
            val texture = ICONS[key] ?: return
            draw(matrix.peek().model, 10F, 10F + (size + 1) * i, size, texture)
        }

        matrix.pop()

    }


    private fun draw(
        matrix: Matrix4f,
        x: Float,
        y: Float,
        size: Float,
        texture: TextureAtlasSprite
    ) {
        val buffer = Tessellator.getInstance().buffer
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX)
        buffer.vertex(matrix, x, 0F, y).texture(texture.maxU, texture.minV).endVertex()
        buffer.vertex(matrix, x, 0F, y + size).texture(texture.maxU, texture.maxV).endVertex()
        buffer.vertex(matrix, x + size, 0F, y + size).texture(texture.minU, texture.maxV).endVertex()
        buffer.vertex(matrix, x + size, 0F, y).texture(texture.minU, texture.minV).endVertex()
        buffer.finishDrawing()
        RenderSystem.enableAlphaTest()
        WorldVertexBufferUploader.draw(buffer)
    }

}
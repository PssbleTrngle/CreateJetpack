package com.possible_triangle.create_jetpack.client

import com.jozufozu.flywheel.util.AngleHelper
import com.mojang.blaze3d.matrix.MatrixStack
import com.possible_triangle.create_jetpack.Content
import com.simibubi.create.AllBlockPartials
import com.simibubi.create.AllBlocks
import com.simibubi.create.CreateClient
import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock
import com.simibubi.create.foundation.utility.AnimationTickHolder
import net.minecraft.client.renderer.Atlases
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.LivingRenderer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.client.renderer.entity.model.BipedModel
import net.minecraft.client.renderer.entity.model.EntityModel
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.Pose
import net.minecraft.util.Direction


class JetpackArmorLayer<T : LivingEntity, M : EntityModel<T>>(private val renderer: LivingRenderer<T, M>) :
    LayerRenderer<T, M>(renderer) {

    companion object {
        fun <T : LivingEntity, M : EntityModel<T>> register(renderer: LivingRenderer<T, M>) {
            val layer = JetpackArmorLayer(renderer)
            renderer.addLayer(layer)
        }
    }

    override fun render(
        ms: MatrixStack,
        buffer: IRenderTypeBuffer,
        light: Int,
        entity: T,
        yaw: Float,
        pitch: Float,
        pt: Float,
        f1: Float,
        f2: Float,
        f3: Float
    ) {
        if (entity.pose === Pose.SLEEPING) return
        if (!Content.JETPACK.isWornBy(entity)) return

        val entityModel: M = renderer.entityModel
        if (entityModel !is BipedModel<*>) return

        ms.push()
        val renderedState = AllBlocks.COPPER_BACKTANK.defaultState
            .with(CopperBacktankBlock.HORIZONTAL_FACING, Direction.SOUTH)
        val renderType = Atlases.getEntityCutout()

        val backtank = CreateClient.BUFFER_CACHE.renderBlock(renderedState)
        val cogs = CreateClient.BUFFER_CACHE.renderPartial(AllBlockPartials.COPPER_BACKTANK_COGS, renderedState)

        entityModel.bipedBody.rotate(ms)
        ms.translate(-1 / 2.0, 10 / 16.0, 1.0)
        ms.scale(1F, -1F, -1F)
        backtank.forEntityRender()
            .light(light)
            .renderInto(ms, buffer.getBuffer(renderType))

        cogs.matrixStacker()
            .centre()
            .rotateY(180.0)
            .unCentre()
            .translate(0.0, (6.5f / 16).toDouble(), (11f / 16).toDouble())
            .rotate(Direction.EAST, AngleHelper.rad(2.0 * AnimationTickHolder.getRenderTime(entity.world) % 360))
            .translate(0.0, (-6.5f / 16).toDouble(), (-11f / 16).toDouble())

        cogs.forEntityRender()
            .light(light)
            .renderInto(ms, buffer.getBuffer(renderType))

        ms.pop()

    }

}
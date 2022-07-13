package com.possible_triangle.create_jetpack.client

import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.create_jetpack.Content
import com.simibubi.create.AllBlockPartials
import com.simibubi.create.AllBlocks
import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock
import com.simibubi.create.foundation.render.CachedBufferer
import com.simibubi.create.foundation.utility.AngleHelper
import com.simibubi.create.foundation.utility.AnimationTickHolder
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.core.Direction
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose


class JetpackArmorLayer<T : LivingEntity, M : EntityModel<T>>(private val renderer: LivingEntityRenderer<T, M>) :
    RenderLayer<T, M>(renderer) {

    companion object {
        fun registerOnAll(dispatcher: EntityRenderDispatcher) {

            dispatcher.skinMap.values.forEach(::registerOn)
            dispatcher.renderers.values.forEach(::registerOn)
        }

        private fun registerOn(renderer: EntityRenderer<*>?) {
            fun <T : LivingEntity, M : EntityModel<T>> register(renderer: LivingEntityRenderer<T, M>) {
                val layer = JetpackArmorLayer(renderer)
                renderer.addLayer(layer)
            }

            if (renderer is LivingEntityRenderer<*, *>) {
                if (renderer.model is HumanoidModel) {
                    register(renderer)
                }
            }
        }
    }

    override fun render(
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        entity: T,
        yaw: Float,
        pitch: Float,
        pt: Float,
        f1: Float,
        f2: Float,
        f3: Float,
    ) {
        if (entity.pose === Pose.SLEEPING) return
        if (!Content.JETPACK.get().isWornBy(entity)) return

        val entityModel: M = renderer.model
        if (entityModel !is HumanoidModel<*>) return

        val renderedState =
            AllBlocks.COPPER_BACKTANK.defaultState.setValue(CopperBacktankBlock.HORIZONTAL_FACING, Direction.SOUTH)
        val renderType = Sheets.cutoutBlockSheet()

        val backtank = CachedBufferer.block(renderedState)
        val cogs = CachedBufferer.partial(AllBlockPartials.COPPER_BACKTANK_COGS, renderedState)

        ms.pushPose()

        entityModel.body.translateAndRotate(ms)
        ms.translate(-1 / 2.0, 10 / 16.0, 1.0)
        ms.scale(1F, -1F, -1F)
        backtank.forEntityRender().light(light).renderInto(ms, buffer.getBuffer(renderType))

        cogs.centre().rotateY(180.0).unCentre()
            .translate(0.0, (6.5f / 16).toDouble(), (11f / 16).toDouble())
            .rotate(Direction.EAST, AngleHelper.rad(2.0 * AnimationTickHolder.getRenderTime(entity.level) % 360))
            .translate(0.0, (-6.5f / 16).toDouble(), (-11f / 16).toDouble())

        cogs.forEntityRender().light(light).renderInto(ms, buffer.getBuffer(renderType))

        ms.popPose()

    }

}
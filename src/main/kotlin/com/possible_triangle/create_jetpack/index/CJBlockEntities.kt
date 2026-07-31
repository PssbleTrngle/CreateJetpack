package com.possible_triangle.create_jetpack.index

import com.possible_triangle.create_jetpack.CJContent.REGISTRATE
import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity
import com.simibubi.create.content.equipment.armor.BacktankRenderer
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
import com.tterrag.registrate.builders.BlockEntityBuilder
import com.tterrag.registrate.util.nullness.NonNullFunction
import dev.engine_room.flywheel.lib.model.Models
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer

object CJBlockEntities {
    val JETPACK_BLOCK_ENTITY =
        REGISTRATE
            .blockEntity("jetpack", BlockEntityBuilder.BlockEntityFactory(::BacktankBlockEntity))
            .visual {
                SimpleBlockEntityVisualizer.Factory { ctx, be, f ->
                    val model =
                        Models.partial(
                            if (be.blockState.`is`(CJBlocks.NETHERITE_JETPACK)) {
                                AllPartialModels.NETHERITE_BACKTANK_SHAFT
                            } else {
                                AllPartialModels.COPPER_BACKTANK_SHAFT
                            },
                        )
                    SingleAxisRotatingVisual(ctx, be, f, model)
                }
            }.validBlocks(CJBlocks.JETPACK, CJBlocks.NETHERITE_JETPACK)
            .renderer { NonNullFunction { BacktankRenderer(it) } }
            .register()

    internal fun register() {
        // Load this class
    }
}

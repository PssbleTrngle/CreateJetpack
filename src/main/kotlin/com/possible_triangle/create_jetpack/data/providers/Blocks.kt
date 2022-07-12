package com.possible_triangle.create_jetpack.data.providers

import com.possible_triangle.create_jetpack.Content
import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock
import net.minecraft.data.DataGenerator
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ConfiguredModel
import net.minecraftforge.common.data.ExistingFileHelper

class Blocks(gen: DataGenerator, existing: ExistingFileHelper) :
    BlockStateProvider(gen, MOD_ID, existing) {

    override fun registerStatesAndModels() {


        with(Content.JETPACK_BLOCK) {
            val model = models().getExistingFile(blockTexture(this))

            getVariantBuilder(this).forAllStates { state ->

                val facing = state.get(CopperBacktankBlock.HORIZONTAL_FACING)

                ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(facing.horizontalIndex * 90 + 180)
                    .build()
            }
        }

    }

}
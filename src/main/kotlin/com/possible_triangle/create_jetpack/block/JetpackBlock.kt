package com.possible_triangle.create_jetpack.block

import com.possible_triangle.create_jetpack.Content
import com.simibubi.create.content.equipment.armor.BacktankBlock
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

class JetpackBlock(
    properties: Properties,
) : BacktankBlock(properties) {
    override fun getBlockEntityType(): BlockEntityType<out BacktankBlockEntity> = Content.JETPACK_BLOCK_ENTITY.get()
}

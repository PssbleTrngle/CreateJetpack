package com.possible_triangle.create_jetpack.world.block

import com.possible_triangle.create_jetpack.index.CJBlockEntities
import com.simibubi.create.content.equipment.armor.BacktankBlock
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

class JetpackBlock(
    properties: Properties,
) : BacktankBlock(properties) {
    override fun getBlockEntityType(): BlockEntityType<out BacktankBlockEntity> = CJBlockEntities.JETPACK_BLOCK_ENTITY.get()
}
